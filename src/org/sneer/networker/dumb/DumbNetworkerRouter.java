package org.sneer.networker.dumb;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import org.sneer.networker.NetId;

/**
 * This is a stand-alone router process (notice the main()) for a 
 *   DON (Dumb Overlay Network) instance.
 * 
 * The single command-line argument is the UDP port where it runs. If you 
 *   don't give a port it runs on 65235.
 * 
 * There's no acks of any sort in the overlay protocol itself. Therefore, the 
 *   router requires the client devices to send packets to someone every now
 *   and then so that they remain in the routers' routing table.
 * 
 * The router should probably forget about peers after ... 30 minutes without 
 *   receiving anything from them. This probably means we want the devices 
 *   'pinging' the router every 10 minutes so they remain reachable if the
 *   router can get at least one out of every three ping packets.
 * 
 * There's no ping message. To ping, the client device sends a packet to a 
 *   bogus (random) destination.
 * 
 */
public class DumbNetworkerRouter {
	
	public static final int DEFAULT_UDP_PORT = 65235;
	
	public static void main(String[] args) throws Exception {
		int port = DEFAULT_UDP_PORT;
		if (args.length > 0) {
			port = Integer.valueOf(args[0]);
			if (port < 0)
				port = 0;
			else if (port > 65535)
				port = 65535;
		}
		new DumbNetworkerRouter(port);
	}
	
	// =======================================================================
	
	// The two routing tables.
	// Every 30 minutes we set one of them to be the new primary table, 
	//   at which point that table is erased.
	// Receiving any packet adds or re-adds the peer to the current primary 
	//   table.
	// Lookup is always made on both tables.
	Map<NetId, SocketAddress>[] routingTable = new HashMap[2];
	
	// Primary and secondary tables (either 0 or 1)
	int prim = 0;
	int sec = 1;
	
	// Time until primary routing table pointer changes.
	long lastPrimFlipTime = System.currentTimeMillis();
	
	// Fun statistics
	long routed;

	// The router.
	public DumbNetworkerRouter(int port) throws Exception {
		
		// just do the reading loop in the ctor with foreverblock.
		// this doesn't do anything while there's no input. it can
		//  forget about peers after the unblocking occurs.
		
		ByteBuffer in = ByteBuffer.allocate(65536);
		
		routingTable[0] = new HashMap();
		routingTable[1] = new HashMap();
		
		InetSocketAddress isa = new InetSocketAddress(port);
		DatagramChannel channel = DatagramChannel.open();
		channel.bind(isa);
		
		while (channel.isOpen()) {
			
			// Wait for something, blocking forever
			in.clear();
			SocketAddress senderAddress = channel.receive(in);
			
			// If we received a packet, try to route it by checking
			//   whether we have the destination in any of our 
			//   routing tables.
			boolean validPacket = false;
			NetId sender = new NetId();
			NetId receiver = new NetId();
			if (senderAddress != null) {
				in.rewind();
				if (in.remaining() >= 64) {
					validPacket = true;
					++routed;
					in.get(sender.getBytes());
					in.get(receiver.getBytes());
					in.rewind();
					// look it up first in the primary, since if there's
					//   an entry on both then the secondary may be out of date.
					SocketAddress receiverAddress = routingTable[prim].get(receiver);
					if (receiverAddress == null)
						receiverAddress = routingTable[sec].get(receiver);
					// if we found an address, route to it
					if (receiverAddress != null)
						channel.send(in, receiverAddress);
				}
			}
			
			// Check for routing table flip every half hour
			long now = System.currentTimeMillis();
			if (now > lastPrimFlipTime + 30 * 60 * 1000) {
				lastPrimFlipTime = now; // schedule new swap
				
				int temp = prim; // swap primary and secondary indices
				prim = sec;
				sec = temp;

				System.out.println("Entries: " + (routingTable[0].size() + routingTable[1].size()) + ", Packets: " + routed);
				
				routingTable[prim] = new HashMap(); // wipe the new primary
			}
			
			// If we received a packet, refresh its sender in
			//   the primary routing table.
			if (validPacket) {
				routingTable[prim].put(sender, senderAddress);
			}
		}		
	}	
}
