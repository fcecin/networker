package org.sneer.networker;

/*
 * A "NetworkerDevice" is like a network interface card (NIC) to an overlay (logical) network.
 * 
 * E.g. you can have an implementor of NetworkerDevice which provides a client-server topology, 
 *   and another that implements a peer-to-peer DHT network, and another that implements a 
 *   routing-by-flooding (crapshoot) network.
 *   
 * An implementor of Networker is essentially a bag of NetworkerDevice objects, and when you
 *   send() to a Networker, it just send()s to zero or more of its NetworkerDevices, which 
 *   are the guys who actually do the work.
 *   
 * Or simply, "it's where the open socket is."
 */
public interface NetworkerDevice extends Sender {

	/*
	 * The NetId of this device. 
	 * If you write to it you MAY be changing the ID of the interface.
	 */
	public NetId getId();
	
	/*
	 * Set the NetId of this device.
	 */
	public void setId(NetId newId);
}
