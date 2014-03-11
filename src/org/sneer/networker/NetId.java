package org.sneer.networker;

import java.security.SecureRandom;
import java.util.Arrays;

/*
 * A "NetId" (we can change this name) is an address in an abstract logical/overlay network.
 * 
 * It is an address in a logical/overlay network because it is a network between "peers." If the IP 
 *   address of the peer machines/processes/users changes, they can find each other again because 
 *   they locate themselves by their NetIds.
 *   
 * It is an address in an _abstract_ such network because how a peer actually routes to other 
 *   peer NetIds is an "implementation detail." E.g. you can have a peer that routes to a central
 *   server which knows the IP address of all current holders of all NetIds, or you can have a peer
 *   that connects to other peers to do a search by flooding, or by gossip, or by using some sort 
 *   of DHT routing, etc.
 *   
 * We could have defined a "NetId" in several ways. Probably the simplest and shortest one that 
 *   preserves all properties we want is to simply make it be a glorified 256-bit number with some
 *   helper methods/operations, which is what we have done here.
 *   
 * The intent is that the 256-bit number be a public key in an Elliptic Curve cryptography scheme.
 *   However, the API allows the number to be a simple random number, as typically is the Node ID 
 *   of a peer in a completely unsecured DHT, for example. There will be services in the networker 
 *   API where peers can communicate without producing proof of possessing the private key that 
 *   matches the public key represented by the NetId. In these situations the peer does not actually
 *   have to have the private key for the chosen "public key," so it does not actually need to know 
 *   how to generate a proper public/private keypair.
 * 
 */
public class NetId {

	// A 256-bit overlay network ID. May be a random number OR an ECC public key of some sort.
	public byte[] bytes = new byte[32];

	// Assign a random value to this NetId
	public void randomize() {
		new SecureRandom().nextBytes(bytes);
	}
	
	// You can use this to change the ID as well.	
	public byte[] getBytes() { 
		return bytes; 
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bytes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetId other = (NetId) obj;
		if (!Arrays.equals(bytes, other.bytes))
			return false;
		return true;
	}
}
