package org.sneer.networker;

/*
 * Bag of sender methods.
 */
public interface Sender {
	
	/*
	 * Send unsigned and open datagram.
	 */
	public void send(NetId receiver, byte[] data);
	
	/*
	 * Send datagram signed using the sender's keypair. 
	 * The receiver verifies the sender by checking that the signature matches
	 *   the sender's NetId-as-public-key.
	 * If the signature does not match at the receiver, the receiver app doesn't
	 *   see the incoming-datagram callback (the datagram is discarded).
	 * If sender ID isn't cryptographic (i.e. implementor doesn't have the matching 
	 *   private key) then this method does nothing.
	 */
	public void sendSigned(NetId receiver, byte[] data);
	
	/*
	 * Send datagram signed using the sender's keypair and then encrypted using the receiver's 
	 *   NetId-as-public-key.
	 * The receiver can only read the contents if it can produce a private key that
	 *   matches its NetId-as-public-key. The private key is used to open some 
	 *   to-be-determined symmetric key in the payload which is then used to decypher 
	 *   the rest of the datagram.
	 * If the receiver device can't decrypt the packet it is silently discarded (i.e. 
	 *   doesn't reach the listener/app).
	 * After decryption, the receiver verifies the sender by checking that the signature 
	 *   matches the sender's NetId-as-public-key.
	 * If the signature does not match at the receiver, the receiver app doesn't
	 *   see the incoming-datagram callback (the datagram is discarded).
	 * If sender ID isn't cryptographic (i.e. implementor doesn't have the matching 
	 *   private key) then this method does nothing.
	 * If the Sender doesn't have an implementation of the required cyphering pipeline
	 *   then this method does nothing.
	 */
	public void sendSignedEncrypted(NetId receiver, byte[] data);
	
	/*
	 * Checks whether this sender can sign datagrams.
	 * If the sender is a NetworkerDevice, it has the private key to go with the NetId.
	 * If the sender is a Networker, it has at least one NetworkerDevice that can do so.  
	 */
	public boolean canSign();
	
	/*
	 * Checks whether this sender implements cyphering.
	 * If the sender is a NetworkerDevice, it returns true if it knows how to encrypt.
	 * If the sender is a Networker, it returns true only if it has at least one device
	 *   that implements it.
	 */
	public boolean canEncrypt();
}

