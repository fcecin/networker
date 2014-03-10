package org.sneer.networker;

/*
 * Bag of receiver callbacks. See Sender.
 */
public interface Receiver {
	
	/*
	 * Receive a plain datagram (no authenticity of sender and no secrecy).
	 */
	public void receive(NetId sender, byte[] data);

	/*
	 * Receive a signed datagram (authenticity of sender is verified).
	 */
	public void receiveSigned(NetId sender, byte[] data);

	/*
	 * Receive an signed-then-encrypted datagram (authenticity of sender is verified,
	 *   as well as secrecy).
	 */
	public void receiveSignedEncrypted(NetId sender, byte[] data);

	/*
	 * Checks whether this receiver implements signature verification.
	 * For a NetworkerDevice, it answers for itself.
	 * For a Networker this returns true if and only if ALL its devices can do it, 
	 *   and only if there is at least one device. 
	 */
	public boolean canCheckSignatures();
	
	/*
	 * Checks whether this receiver can decrypt incoming messages.
	 * For a NetworkerDevice, it must implement the proper decryption pipeline AND it
	 *   must have the matching private key to its current NetId-as-public-key stored
	 *   somewhere.
	 * For a Networker this returns true if and only if ALL its devices return true 
	 *   and only if there is at least one device. 
	 */
	public boolean canDecrypt();
}
