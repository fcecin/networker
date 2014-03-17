package org.sneer.networker.messenger;

import org.sneer.networker.NetId;

/**
 * Implemented by whoever is using a Messenger to send and receive 
 * messages.
 */
public interface MessengerListener {
	
	/**
	 * Notification that a message has been sent successfully.
	 * @param request The request that completed successfully.
	 */
	public void sendCompleted(Object request);

	/**
	 * Messenger has given up delivering a message. The message may 
	 *   actually have gone through but the Messenger gave up waiting for
	 *   receipt confirmations in any case, so we consider it not delivered.
	 * @param request The request that failed.
	 */
	public void sendFailed(Object request);
	
	/**
	 * Application receives a guaranteed-delivery-or-timeout-notification
	 *    and duplication-free message from a remote host. 
	 * @param sender Who sent it.
	 * @param message What was sent to us.
	 */
	public void receive(NetId sender, byte[] message);
}
