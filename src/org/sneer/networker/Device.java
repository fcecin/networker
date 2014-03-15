package org.sneer.networker;

/*
 * A Networker Device is like a network interface card (NIC) to an overlay 
 *   (logical) network of some sort.
 * 
 * E.g. you can have an implementor of Device which provides a client-server 
 *   topology, and another that implements a peer-to-peer DHT network, and 
 *   another that implements a routing-by-flooding (crapshoot) network.
 *   
 * An implementor of Networker is essentially a bag of Device objects, and when 
 *   you send() to a Networker, it just send()s to zero or more of its Devices, 
 *   which are the guys who actually do the work.
 *   
 * Or simply, "it's where the open socket is."
 */
public interface Device {
	
	/**
	 * Get the parent networker. You should set it on the constructor of 
	 *   your Device implementation and never change it.
	 * @return The networker that has this.
	 */
	public Networker getNetworker();
	
	/**
	 * Get the device listener. You should set it on the constructor of 
	 *   your Device implementation and never change it.
	 * @return Whoever is listening to this device's callbacks.
	 */
	public DeviceListener getListener();

	/**
	 * Sends an unreliable, unordered, non-duplicate-protected, 
	 *  non-integrity-checked (that is, no typical transport protocol
	 *  "guarantee") datagram through the overlay/network provided
	 *  by this Device. Or, more simply, "send something through something 
	 *  like UDP/IP."
	 * @param receiver Networker that should get it.
	 * @param data What should be sent to it.
	 */
	public void send(NetId receiver, byte[] data);
}
