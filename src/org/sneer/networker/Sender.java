package org.sneer.networker;

/*
 * Bag of sender methods.
 */
public interface Sender {
	
	/*
	 * Send an unreliable, unordered, non-duplicate-protected datagram.
	 */
	public void send(NetId receiver, byte[] data);
}

