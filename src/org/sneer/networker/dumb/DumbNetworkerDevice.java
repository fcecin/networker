package org.sneer.networker.dumb;

import org.sneer.networker.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

public class DumbNetworkerDevice implements Device, Runnable {
	
	// all zeroes "Router ping/pong" NetId
	private static final NetId pingNetId = new NetId();
	
	// Networker and DeviceListener
	DumbNetworker networker; 
	
	DatagramChannel channel;
	InetSocketAddress serverSocketAddr;
	
	Thread networkThread;
	
	ByteBuffer sendbuf = ByteBuffer.allocate(65536);
	
	volatile boolean connectedGuess;
		
	public DumbNetworkerDevice(DumbNetworker networker) {
		this.networker = networker;
	}

	public Networker getNetworker() {
		return networker;
	}

	public DeviceListener getListener() {
		return networker;
	}
	
	/*
	 * This is the main API of this "dumb" device (how you can us it).
	 * This is NOT on the Device interface. This is implementation-specific.
	 * This device implements a "fake" (non-P2P) overlay network. It needs
	 *  a central "router" which by being a single dude can trivially
	 *  route between any two devices that "connect" to it.
	 * For example, connect() means "connect this overlay network device to its 
	 *  overlay network" which, for the DumbNetworker overlay, means connecting 
	 *  to a central "router" UDP process (DumbNetworkerRouter).
	 */
	
	/**
	 * Activate this DumbNetworkerDevice, making it start trying to work 
	 *   with the given central router.
	 * @param serverAddr IP address where the central router is supposed to be.
	 * @param serverPort UDP port where the central router is supposed to be.
	 * @return true if we succeeded in activating the device (start threads,
	 *   open sockets, etc.) or false if some lame local error occurred.
	 */
	public boolean connect(String serverAddr, int serverPort) {
		serverSocketAddr = InetSocketAddress.createUnresolved(serverAddr, serverPort);
		connectedGuess = false;
		return isActive();
	}
	
	/**
	 * Check whether this Device is active.
	 * @return true if connect() has been called and if either we succeeded in
	 *   starting threads, opening sockets etc. when connect() was called or if
	 *   we succeeded in doing so just now. false if some lame local error is 
	 *   preventing us from starting threads and/or opening sockets.
	 */
	public boolean isActive() {
		return open();
	}
	
	/**
	 * Guesses whether there's an active DumbNetworkerRouter at the
	 *   address we supplied in a previous connect() call or not.
	 * @return true if isActive() and heard from the router recently, false
	 *   if not active or if router hasn't been heard from in a while.
	 */
	public boolean isConnected() {
		if (! open())
			return false;
		return connectedGuess;
	}
	
	/**
	 * Deactivates this Device. This closes the socket and stops the thread
	 *   if we haven't done so already.
	 */
	public void disconnect() {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException ex) {
			}
			while (networkThread.isAlive()) {
				try {
					networkThread.join();
				} catch (InterruptedException ex) {
				}
			}
			networkThread = null;
			channel = null;
			
			// Redundant/not needed because isOpen()==false now.
			connectedGuess = false; 
		}
	}
	
	/*
	 * NetworkerDevice / Sender
	 */
	
	@Override
	public void send(NetId receiver, byte[] data) {
		if (open()) {

			sendbuf.clear();
			// Header: 64 bytes
			sendbuf.put(networker.getId().getBytes()); // Sender 256-bit ID
			sendbuf.put(receiver.getBytes()); // Receiver 256-bit ID
			// Body -- make sure sendbuf doesn't overflow
			int amount = Math.min(data.length, sendbuf.remaining());
			sendbuf.put(data, 0, amount);

			// Send it
			sendbuf.flip();
			try {
				channel.write(sendbuf);
			} catch (IOException ex) {
			}
		}
	}

	/*
	 * These are the internals. 
	 * open() takes care of (trying to) open a socket and start a thread
	 *   to read from it (and it sends stuff too).
	 * run() is what the thread uses to do its stuff.
	 */
	
	private boolean open() {
		if (channel == null || !networkThread.isAlive()) {
			try {
				channel = DatagramChannel.open().connect(serverSocketAddr);
			} catch (IOException ex) {
				return false; // cannot open, cannot connect, unresolved addr, etc.
			}
			networkThread = new Thread(this);
			networkThread.start();
		}
		return true;
	}
	
	@Override
	public void run() {
		
		// this receives (blocking) but it also has to ping every 10 minutes, 
		//   so the receive must time out every e.g. 2 seconds or so.
		// datagrampacket sucks ass, so we'll bow down to using selectors
		//   instead of going to the datagramsocket setsotimeout.
		
		// how ping-ponging with the router works:
		// we send a ping when nextPingTime expires. at first connecting/starting
		//   the thread, that means immediately.
		// from there we continue to ping every N seconds, where N is 4, 8, 16,
		//   32, 64 ... seconds, until we get a pong back from the server.
		// when we get any packet back from the router, including pongs, we 
		//   leave the router alone for 10 minutes.
		// after 10 minutes we start bothering it again.
		
		long nextPingTime = 0;
		int pingTimeDelta = 4; // starts at +4s and doubles after every ping
		ByteBuffer pingbuf = ByteBuffer.allocate(64);
						
		Selector selector;
		try {
			selector = Selector.open();
			channel.register(selector, SelectionKey.OP_READ);
		} catch (IOException ex) {
			// Wow, really? Then we give up.
			// A subsequent open() call will then reopen the channel and 
			//   re-start the thread because the thread will be dead after 
			//   we return.
			return;
		}
		
		ByteBuffer rcvbuf = ByteBuffer.allocate(65536);

		// While channel open (not closed) and connected...
		while (channel.isConnected()) {
			
			// Try to read something with a timeout
			try {
				if (selector.select(2000) > 0) {
				
					// This doesn't block because it is ready to read.
					rcvbuf.clear();
					channel.receive(rcvbuf);
					rcvbuf.flip();
					
					// Is it valid? If not, ignore it.
					if (rcvbuf.remaining() >= 64) {
						
						// read header
						NetId sender = new NetId();
						NetId receiver = new NetId();
						rcvbuf.get(sender.getBytes());
						rcvbuf.get(receiver.getBytes());
						
						// make sure we're the intended recipient, otherwise
						//  ignore it.
						if (networker.getId().equals(receiver)) {
							
							// if the sender is the all-zeroes NetId, this
							//   means it is a "pong" from the server. we 
							//   don't forward that to the app.
							// yes, this is ugly: we have polluted the 
							//   address space with a "special address" that
							//   can't be used by apps.
							if (! sender.equals(pingNetId)) {
							
								// actual valid sender, so forward it.
								byte[] data = new byte[rcvbuf.remaining()];
								rcvbuf.get(data);
								networker.receive(sender, data);
							}
							
							// reset the pinger to +10 minutes in any case
							//  (pongs or successfully routed messages, they 
							//   are the same thing as far as knowing the 
							//   router has got our address right -- both 
							//   push the pinging to 10min in the future).
							nextPingTime = System.currentTimeMillis() + 10 * 60 * 1000;
							pingTimeDelta = 4; // reset to 4 second interval between pings
							
							// we got something so we are being seen
							connectedGuess = true;
						}
					}
				}
			} catch (IOException ex) {
				// We don't care. If it is something serious the
				//   channel will have been closed and we quit.
				// Also the selector can't/shoudn't have been closed
				//   so we don't really have to care about that.
			}
			
			// Check if it is time to ping the central router -- every 10 mins
			long now = System.currentTimeMillis();
			if (now > nextPingTime) {
				
				// If we're having to ping, it means we might have been
				//   forgotten. But let's not be hasty: let's wait for a few
				//   pings to go unanswered.
				// The easiest way to accomplish this is to take our 4,8,16,32
				//   64,128,256,512,600,600,600... series and plug into it at
				//   some point. Say the 32 point... which will give us three
				//   unanswered packets within 28 seconds to consider the
				//   router gone.
				if (pingTimeDelta >= 32)
					connectedGuess = false;
				
				// Ping a lot at the start but increase interval as we continue 
				//   to ping without getting a response.
				nextPingTime = now + pingTimeDelta * 1000;
				pingTimeDelta *= 2;
				if (pingTimeDelta > 600) // cap to 10 minutes maximum ping interval
					pingTimeDelta = 600;
				
				// Send the ping
				pingbuf.clear();
				pingbuf.put(networker.getId().getBytes());
				pingbuf.put(pingNetId.getBytes()); // all zeroes
				pingbuf.flip();
				try {
					channel.send(pingbuf, serverSocketAddr);
				} catch (IOException ex) {
					// We don't care.
				}
			}
		}
	}

}
