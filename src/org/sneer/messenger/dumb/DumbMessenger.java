package org.sneer.messenger.dumb;

import java.nio.ByteBuffer;
import org.sneer.messenger.*;
import org.sneer.networker.*;

/**
 * An egregiously dumb implementation of the Messenger interface.
 */
public class DumbMessenger implements Messenger, NetworkerListener {
	
	// Who can send unreliable unordered and non-duplicate-protected network
	//   datagrams to us.
	Networker networker; 
	
	// The application object that listens to messenger events.
	MessengerListener listener;
	
	public DumbMessenger(Networker networker, MessengerListener listener) {
		this.networker = networker;
		this.listener = listener;
	}

	/**
	 * Starts a reliable message sending request.
	 * @param receiver Who's going to get it.
	 * @param message What it is going to get.
	 * @return An object that can be used to control the pending request.
	 */
	@Override
	public MessagingRequest send(NetId receiver, ByteBuffer message) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

		// The dumb messenger assumes the MTU is 1024 bytes.
		// That's more or less the case for IPv4 and IPv6 which are 
		//   essentially everything that exists. For dumbness purposes it
		//   is enough. 
		// TODO: Perhaps the Networker should return its MTU instead?
		// In any case, we'll chunk all messages into 1K network datagram
		//   payloads (plus the messenger's overhead).
		
		// But the messenger will take over this networker. Is that what
		//   we want? Hmm.. MINX --> Messenger is a service or one of 
		//   the protocols spoken within a single Networker!
	}

	/**
	 * Implementation of Messenger.
	 * @return Get the MessengerListener.
	 */
	@Override
	public MessengerListener getListener() {
		return listener;
	}
	
	/**
	 * Implementation of NetworkerListener.
	 * @param sender Who just sent us a network datagram.
	 * @param data The datagram sent to us.
	 */
	@Override
	public void receive(NetId sender, byte[] data) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
