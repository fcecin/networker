package org.sneer.minx;

import org.sneer.networker.NetId;

/**
 * 
 *  FIXME: WIP/draft/incomplete
 * 
 *  FIXME: this has to be rethought.
 *  we must do the singleton Networker/NetworkerListener class, which is
 *   the MINX networker before we can do the Extension class.
 * 
 * 
 * A component that extends a Networker's protocol.
 * See ExtensionId. The thing that extends this class is an actual protocol 
 *   extension that has its own (hopefully) unique ExtensionId.  
 */
public class Extension {
	
	private final ExtensionId eid;
	
	/**
	 * Create an extension with its unique id.
	 * @param eid The mandatory unique id.
	 */
	public Extension(ExtensionId eid) {
		this.eid = eid;
	}
	
	/**
	 * Get the unique protocol extension id.
	 * @return The (hopefully) unique ExtensionId of this Extension. A proper
	 *   implementation always returns the same non-null value here.
	 */
	private ExtensionId getId() {
		return eid;
	}
	
	/**
	 * Receive a network datagram addressed to this Extension's Id.
	 * Subclass (the actual extension) should override.
	 * @param sender Who sent it.
	 * @param data What was sent.
	 */
	protected void receive(NetId sender, byte[] data) {
		// By default we just discard it.
	}
	
	/**
	 * Send a network datagram through the underlying Networker.
	 * Subclass should NOT override.
	 */
	protected void send(NetId receiver, byte[] data) {
		//networker.send()
		
	}
}
