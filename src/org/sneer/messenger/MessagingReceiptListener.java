package org.sneer.messenger;

import java.nio.ByteBuffer;
import org.sneer.networker.NetId;

/**
 * Implemented by an application object that will be called by a 
 *   Messenger when something interesting happens to a MessagingReceipt.
 * 
 * Note that all methods get the MessagingReceipt, so you can have a single
 *   application object that deals with all receipts if you want, instead of 
 *   managing arrays of listeners. 
 */
public interface MessagingReceiptListener {
	
	/**	
	 * We have finished downloading a large message. 
	 * @param receipt The object that was created by a Messenger to represent 
	 *   the message download... that just completed successfully.
	 */
	public void receiveCompleted(MessagingReceipt receipt);
	
	/**
	 * The receipt has failed, we have timed out/given up.
	 * @param receipt The object that was created by a Messenger to represent 
	 *   the message download... that just failed.
	 */
	public void receiveFailed(MessagingReceipt receipt);
}
