package org.sneer.messenger;

/**
 * Implemented by an application object that will be called by a 
 *   Messenger when something interesting happens to a MessagingRequest.
 *
 * Note that all methods get the MessagingRequest, so you can have a single
 *   application object that deals with all receipts if you want, instead of 
 *   managing arrays of listeners. 
 */
public interface MessagingRequestListener {
	
	/**
	 * Called by a Messenger when a Messenger.send() operation is 
	 *   successful, that is, when we get all the acknowledgements we 
	 *   need from the other side that the thing went through.
	 * @param request The request that completed successfully.
	 */
	public void sendCompleted(MessagingRequest request);

	/**
	 * Called by a Messenger when a Messenger.send() operation fails.
	 * Note that this doesn't mean the message wasn't delivered/seen by the
	 *   receiver: it just means we didn't get all the required acks back from 
	 *   the receiver.
	 * @param request The request that failed.
	 */
	public void sendFailed(MessagingRequest request);
}
