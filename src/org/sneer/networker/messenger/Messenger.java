package org.sneer.networker.messenger;

import java.nio.ByteBuffer;
import org.sneer.networker.NetId;

/**
 * Uses a Networker to provide a messaging service where you can send 
 *   datagrams of any size to a destination (NetId), with either 
 *   guaranteed delivery of the whole thing or a timeout notification.
 * 
 * The message must fit in RAM as you give the whole thing as a buffer for 
 *   the messenger to deliver.
 * 
 * NOTE: There will be another "Messenger" which will be implemented on top
 *   of MINX (the extensible protocol). MINX will be implemented on top of 
 *   a Networker as well. This "Messenger" was developed to help test the 
 *   Networker; we actually want everyone to use MINX in the future.
 * 
 */
public interface Messenger {
	
	/**
 	 * Files a request to have a message delivered.
	 * @param receiver The destination.
	 * @param message The message to deliver.
	 * @return An object that represents this individual attempt at getting
	 *   something delivered. If this delivery fails, you're going to see 
	 *   the exact same object appear in MessengerListener.sendFailed().
	 */
	public MessagingRequest send(NetId receiver, ByteBuffer message);
	
	/**
	 * See who's the listener.
	 * @return The MessengerListener that receives things from the Messenger.
	 */
	public MessengerListener getListener();
}
