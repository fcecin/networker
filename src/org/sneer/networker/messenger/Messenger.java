package org.sneer.networker.messenger;

import org.sneer.networker.NetId;

/**
 * Uses a Networker to provide a messaging service where you can send 
 *   datagrams of limited size to a destination (NetId), with either 
 *   guaranteed delivery of the whole thing or a timeout notification.
 * 
 * The datagram size limit is determined by the Networker and Device 
 *   implementations being used by the implementor of this interface. If you
 *   try to send something that's larger than the magic/unknown (to these 
 *   interfaces) limit, the implementor should just silently discard what you're
 *   trying to send.
 * 
 * Okay, the size limit is really the size of an UDP packet because this is 
 *   going over the Internet/UDP in any realistic scenario. So if you don't 
 *   want your UDP datagram to be fragmented into a hundred IP fragments then 
 *   you shouldn't be sending things over e.g. 1,024 bytes with this thing, 
 *   or perhaps 1,280 bytes for the IPv6 guaranteed minimum MTU, or slightly 
 *   less than that probably, let's say, 1,100 bytes?
 *
 * NOTE: There can be a layer on top of this, such as MultipartMessenger,
 *   that knows how to send large messages by sending each fragment as a 
 *   Messenger message (or you can rename this interface into UselessSingle
 *   FragmentMessenger and the multipart one just Messenger, or you could 
 *   just fix this interface and make it support multipart messaging as part
 *   of its own job, any one will do).
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
	 * @return A value that represents this individual attempt at getting
	 *   something delivered. If this delivery fails or succeeds, you're going 
	 *   to see the same value (equals()) appear in MessengerListener.sendFailed()
	 *   or MessengerListener.sendCompleted(), respectively.
	 */
	public Object send(NetId receiver, byte[] message);
	
	/**
	 * See who's the listener.
	 * @return The MessengerListener that receives things from the Messenger.
	 */
	public MessengerListener getListener();
}
