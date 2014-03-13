package org.sneer.messenger;

import java.nio.ByteBuffer;
import org.sneer.networker.NetId;

/**
 * This represents a single call to Messenger.send().
 * 
 * A new MessagingRequest is returned by Messenger.send() and the same object
 *   is given by MessengerListener.sendFailed() if that particular attempt to 
 *   send has failed.
 */
public class MessagingRequest {
	
	protected final NetId receiver;
	protected final ByteBuffer message;
	protected final MessagingRequestListener listener;
	
	protected boolean failed = false;
	protected boolean completed = false;
	
	/**
	 * Invoked by a Messenger to create an object that represents an
	 *   attempt to upload a message to some other remote messenger.
	 * @param receiver The thing that should receive what we want to send.
	 * @param message The thing we're trying to get across.
	 * @param listener The application object that wants to know whether this
	 *   attempt succeeds or fails, for example, or how much of it we have sent
	 *   so far, etc.
	 */
	public MessagingRequest(NetId receiver, ByteBuffer message, MessagingRequestListener listener) {
		this.receiver = receiver;
		this.message = message;
		this.listener = listener;
	}

	public NetId getReceiver() {
		return receiver;
	}

	public ByteBuffer getMessage() {
		return message;
	}

	public MessagingRequestListener getListener() {
		return listener;
	}
	
	/**
	 * Check if this request has failed forever (i.e. is garbage)
	 * @return true if failed.
	 */
	public boolean isFailed() {
		return failed;
	}
	
	/**
	 * Check if this request has completed (done).
	 * @return true if completed.
	 */
	public boolean isCompleted() {
		return completed;
	}
	
	/**
	 * Invoked by a Messenger to mark this failed.
	 */
	protected void markFailed() {
		failed = true;
	}
	
	/**
	 * Invoked by a Messenger to mark this completed.
	 */
	protected void markCompleted() {
		completed = true;
	}
}
