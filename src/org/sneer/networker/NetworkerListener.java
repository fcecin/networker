package org.sneer.networker;

/*
 * Implemented by a class that listens for callbacks from an implementor of 
 *   Networker. 
 */
public interface NetworkerListener {

	/**
	 * Receive an unreliable, unordered, non-duplicate-protected datagram
	 *   that just arrived from one of the devices.
	 * @param sender Who sent it.
	 * @param data What was sent to us.
	 */
	public void receive(NetId sender, byte[] data);
	
	/**
	 * Someone called Networker.kill() for the first time on the 
	 *   associated Networker, meaning it is dead now, so there.
	 * This is invoked at most once for a given Networker.
	 */
	public void killed();
}
