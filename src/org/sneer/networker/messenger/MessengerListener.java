package org.sneer.networker.messenger;

import java.nio.ByteBuffer;
import org.sneer.networker.NetId;

/**
 * Implemented by the user of Messenger. 
 */
public interface MessengerListener {
	
	/**
	 * Notification that a message has been sent successfully.
	 * @param request The request that completed successfully.
	 */
	public void sendCompleted(MessagingRequest request);

	/**
	 * Messenger has given up delivering a message. The message may 
	 *   actually have gone through but the Messenger gave up waiting for
	 *   receipt confirmations in any case, so we consider it not delivered.
	 * @param request The request that failed.
	 */
	public void sendFailed(MessagingRequest request);
	
	/**
	 * Receive a message.
	 * @param sender Who sent it.
	 * @param message What was sent to us.
	 */
	public void receive(NetId sender, ByteBuffer message);
}
