package org.sneer.networker.messenger.dumb;

import org.sneer.networker.NetId;

/**
 * An outgoing message pending remote acknowledgement.
 * 
 * Internal helper for this package.
 */
class DumbMessage {
	
	// A DumbMessenger assigns an 'unique' (within a 2^32 rotating range) sequence 
	//   number to every outgoing message (same sequencer for all receivers).
	// The sequence number is used by the receiver side to know how to ack 
	//   what it is receiving.
	// In the event you're sending or receiving more than 2^32 messages 
	//   (not packets) within e.g. 10 minutes, this will stop working.
	private static Integer seqGen = 0;
	
	// the remote receiver that should ack us and the local seqid we have
	//   chosen for the message.
	DumbMessageId messageId;
	
	// the message payload as given by the user
	byte[] message;
	
	// (re)try counter ("max tries" left for DumbMessenger to determine)
	int tryCount = 0;
	
	// (re)try System.currentTimeMillis() timer (exact semantics left for 
	//    DumbMessenger to determine).
	long tryTime = 0;
	
	public DumbMessage(NetId receiver, byte[] message) {
		this.messageId = new DumbMessageId(seqGen++, receiver);
		this.message = message;		
	}

	public byte[] getMessage() {
		return message;
	}
	
	public NetId getReceiver() {
		return messageId.getAddr();
	}
	
	public int getSequence() {
		return messageId.getSequence();
	}
	
	public DumbMessageId getMessageId() {
		return messageId;
	}

	public int getTryCount() {
		return tryCount;
	}

	public void incrementTryCount() {
		++this.tryCount;
	}

	public long getTryTime() {
		return tryTime;
	}

	public void setTryTime(long tryTime) {
		this.tryTime = tryTime;
	}
}
