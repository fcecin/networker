package org.sneer.minx;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 
 *  FIXME: this is WIP/draft/incomplete
 * 
 * Unique global identification of a service/protocol running on top of 
 *   a shared Networker interface.
 * 
 * A Networker can be understood as a kind of socket (e.g. UDP/IP socket) that 
 *   can be shared by multiple distinct applications that don't know about each
 *   other. It does the same (or sufficiently similar) job that the port number
 *   does on a machine's operating system for routing an incoming UDP packet 
 *   to a specific application/endpoint on that machine/OS.
 * 
 * When you have a socket running for a single application, that application
 *   knows beforehand what are the "sub protocols" being spoken within its 
 *   protocol so it can create something like a "message type" field of some
 *   length within the socket messages' body, and it knows beforehand what the 
 *   different type bit combinations mean because it fully controls the whole 
 *   socket by owning the local port number it got for itself, and so does the 
 *   other endpoint.
 * 
 * A Networker however is a thing that's like a network socket, however it 
 *   serves multiple components or applications that don't really know about
 *   each other. The ExtensionId is, therefore, a standard way within a 
 *   single Networker to determine what are the "message types" that the 
 *   several applications sharing that Networker have "registered" with it.
 * 
 * ExtensionIds are binary strings of arbitrary length. The idea is that 
 *   applications/components/"sub protocols" of the Networker socket will
 *   choose random or sufficiently unique ExtensionId strings such that 
 *   collision with others' choices will be extremely unlikely. 128-bit UUIDs
 *   work very well as ExtensionIds, but you can also use shorter numbers or 
 *   UTF-8 representations of human-readable string names you don't mind the 
 *   increased risk of collision or the need to publish your choice of 
 *   ExtensionId in some centralized registry of some sort (akin to IANA ports).
 */
public class ExtensionId {
	
	// The default value of an ExtensionId
	public static final byte[] emptyExtensionId = new byte[0];
	
	// How an extensionId is stored.
	private final byte[] bytes;

	public ExtensionId() {
		 this.bytes = emptyExtensionId;
	}
	
	public ExtensionId(byte[] bytes) {
		this.bytes = bytes;
	}
	
	/**
	 * Sets it to the UTF-8 representation of the given string.
	 * I hope the UTF-8 we get is at least deterministic.
	 * @param name The string to make into an UTF-8 stream.
	 */
	public ExtensionId(String name) {
		try {
			this.bytes = name.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			// Should never happen. 
			// Java implementations are forced by spec to support UTF-8.
			throw new RuntimeException(ex);
		}
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + Arrays.hashCode(this.bytes);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ExtensionId other = (ExtensionId) obj;
		if (!Arrays.equals(this.bytes, other.bytes)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ExtensionId{" + "bytes=" + bytes + '}';
	}
}
