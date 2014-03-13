package org.sneer.networker.dumb;

import org.sneer.networker.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

public class DumbNetworkerDevice implements NetworkerDevice, Runnable {

	NetId netId = new NetId();
	
	NetworkerDeviceListener listener; // this will be the DumbNetworker
	DatagramChannel channel;
	InetSocketAddress serverSocketAddr;
	
	Thread networkThread;
	
	ByteBuffer sendbuf = ByteBuffer.allocate(65536);
		
	public DumbNetworkerDevice(NetworkerDeviceListener listener) {
		this.listener = listener;
		
		// dumb peer gets a new overlay ID every time unless you set it 
		//   to some value explicitly (e.g. if you're doing a server).
		netId.randomize(); 
	}
	
	/*
	 * This is the main API of the device (how you use it).
	 * This device implements a "fake" (non-P2P) overlay network. It needs
	 *  a central "router" which by being a single dude can trivially
	 *  route between any two devices that "connect" to it.
	 * For example, connect() means "connect this overlay network device to its 
	 *  overlay network" which, for the DumbNetworker overlay, means connecting 
	 *  to a central "router" UDP process (DumbNetworkerRouter).
	 */
	
	public boolean connect(String serverAddr, int serverPort) {
		serverSocketAddr = InetSocketAddress.createUnresolved(serverAddr, serverPort);
		return isConnected();
	}
	
	public boolean isConnected() {
		return open();
	}
	
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
		}
	}
	
	/*
	 * NetworkerDevice / Sender
	 */
	
	@Override
	public NetId getId() {
		return netId;
	}

	@Override
	public void setId(NetId newId) {
		netId = newId;
	}

	@Override
	public void send(NetId receiver, byte[] data) {
		if (open()) {

			sendbuf.clear();
			// Header: 64 bytes
			sendbuf.put(netId.getBytes()); // Sender 256-bit ID
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
		
		long lastPingTime = 0;
		ByteBuffer pingbuf = ByteBuffer.allocate(64);
		NetId dummyNetId = new NetId();
		
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
						if (netId.equals(receiver)) {
							byte[] data = new byte[rcvbuf.remaining()];
							rcvbuf.get(data);
							listener.receive(sender, data);
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
			if (now > lastPingTime + 10 * 60 * 1000) {
				lastPingTime = now;
				pingbuf.clear();
				pingbuf.put(netId.getBytes());
				pingbuf.put(dummyNetId.getBytes()); // all zeroes / doesn't matter
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
