package org.sneer.networker;

/*
 * Bag of receiver callbacks. See Sender.
 */
public interface Receiver {
	
	/*
	 * Receive an unreliable, unordered, non-duplicate-protected datagram.
	 */
	public void receive(NetId sender, byte[] data);
}
