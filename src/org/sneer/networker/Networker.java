package org.sneer.networker;

import java.util.ArrayList;

/*
 * The Networker interface is implemented by classes that know how to try to 
 *   send datagrams to another Networker.
 *   
 * A Networker has one NetId which is the application container or device 
 *   logical address. It is a way to reach an endpoint. It is analogous to
 *   IP address to reach applications on one machine. The NetId should be 
 *   set upon construction and then never change.
 *   
 * A Networker has zero or more Devices, which know how to actually deliver
 *   datagrams to another Networker. One Device may rout over a centralized
 *   overlay on top of UDP/IP, another may rout over a DHT over UDP/IP, 
 *   another may use smoke signals, etc.
 *
 */
public interface Networker {
	
	/**
	 * The immutable NetId of this Networker. All Devices are using it.
	 * @return Implementor should return a copy of the Networker's current 
	 *   NetId that the caller can freely screw with to no effect.
	 */
	public NetId getId();

	/**
	 * Get the networker listener.
	 * @return Whoever is listening for this Networker's incoming packets.
 	 */
	public NetworkerListener getListener();
	
	/**
	 * Inspect the Networker's overlay devices list.
	 * The returned array should be either unmodifiable or a clone/copy of the 
	 *   "real" device list used by the Networker. Sloppy implementors may 
	 *   be returning the real device list so users shouldn't screw with it.
	 * @return The devices (not a copy; beware). 
	 */
	public ArrayList<Device> getDevices();
	
	/**
	 * Sends an unreliable, unordered, non-duplicate-protected datagram.
	 * @param receiver Networker that should get it.
	 * @param data What should be sent to it.
	 */
	public void send(NetId receiver, byte[] data);
	
	/**
	 * Kill this Networker. A dead Networker does nothing forevermore. 
	 * All underlying Devices, sockets, threads, etc. have to be wiped out
	 *   when kill() is called for the first time, and 
	 *   NetworkerListener.killed() must be called at that time (and just once).
	 * Calling kill() more than once has no effect.
	 */
	public void kill();
	
	/**
	 * Check if kill() has been already invoked.
	 * @return true if this Networker is dead.
	 */
	public boolean isDead();
}
