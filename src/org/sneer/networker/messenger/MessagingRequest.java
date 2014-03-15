package org.sneer.networker.messenger;

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

	// A Messenger assigns an 'unique' (within a 2^32 rotating range) sequence 
	//   number to every outgoing message (same sequencer for all receivers).
	// The sequence number is used by the receiver side to know how to ack 
	//   what it is receiving.
	// In the event you're sending or receiving more than 2^32 messages 
	//   (not packets) within e.g. 10 minutes, this will stop working.
	private static int seqGen;
	protected final int seq;
	
	protected final NetId receiver;
	protected final ByteBuffer message;
	
	protected boolean failed = false;
	protected boolean completed = false;
	
	/**
	 * Invoked by a Messenger to create an object that represents an
	 *   attempt to upload a NEW message to some other remote messenger.
	 *   The new object will have a new sequence number.
	 * @param receiver The thing that should receive what we want to send.
	 * @param message The thing we're trying to get across.
	 */
	public MessagingRequest(NetId receiver, ByteBuffer message) {
		this.seq = seqGen++;
		this.receiver = receiver;
		this.message = message;
	}

	public NetId getReceiver() {
		return receiver;
	}

	public ByteBuffer getMessage() {
		return message;
	}
	
	/**
	 * Used by Messenger implementation. App shouldn't care about this.
	 * @return The sequence number of this MessagingRequest.
	 */
	public int getSequence() {
		return seq;
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
