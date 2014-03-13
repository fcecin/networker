package org.sneer.messenger;

import java.nio.ByteBuffer;
import org.sneer.networker.NetId;

/**
 * Represents an incoming message, as it is uploaded to us.
 */
public class MessagingReceipt {
	
	protected final NetId sender;
	protected final long length;
	protected final MessagingReceiptListener listener;
	
	protected boolean failed = false;
	protected ByteBuffer message = null; // set to non-null by Messenger when it is received in full

	/**
	 * Called by a Messenger implementation to create a thing that 
	 *   represents a message that we're being fed by some remote
	 *   Messenger somewhere else.
	 * @param sender The thing sending us the message.
	 * @param length The length of the message that we're supposed to get.
	 * @param listener The application object that wants to be updated about
	 *   things that happen to this receipt operation, such as it being 
	 *   completed.
	 */
	public MessagingReceipt(NetId sender, long length, MessagingReceiptListener listener) {
		this.sender = sender;
		this.length = length;
		this.listener = listener;
	}

	/**
	 * Check who is sending the message to us.
	 * @return The thing sending us the message.
	 */
	public NetId getSender() {
		return sender;
	}

	/**
	 * Length of the message that was promised to us.
	 * @return Length of the message when and if it is successfully received.
	 */
	public long getLength() {
		return length;
	}
	
	public MessagingReceiptListener getListener() {
		return listener;
	}

	/**
	 * Check whether this receipt is a failed and dead one.
	 * @return true if this receipt failed to complete and will never complete, 
	 *   false otherwise.
	 */
	public boolean isFailed() {
		return failed;
	}
	
	/**
	 * Get the message that we received.
	 * @return The message if it has been completely received or null if we 
	 *   have failed the receipt or if it is still ongoing.
	 */
	public ByteBuffer getMessage() {
		return message;
	}
	
	/**
	 * Messenger sets this receipt as failed.
	 */
	protected void markFailed() {
		failed = true;
	}
	
	/**
	 * Set complete message only. Invoked by a Messenger.
	 * @param message The message the Messenger has completed receiving.
	 */
	protected void setMessage(ByteBuffer message) {
		this.message = message;
	}
}
