package org.sneer.networker.messenger.dumb;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sneer.networker.*;
import org.sneer.networker.messenger.*;

/**
 * A significantly dumb implementation of the Messenger interface. It 
 *   implements a hard-coded (re-)sending strategy that's completely blind to
 *   both flow control and congestion control, i.e. is rather aggressive and 
 *   wasteful.
 * Another thing that adds to the dumbness is that it doesn't know how to 
 *   combine multiple tiny messages into Networker packets.
 * It is also not smart about latency and priorization in any way mainly because 
 *   it doesn't know what kind of application it is serving, nor is it asking.
 * There's no ordering between Messenger requests; that's also "dumb" but 
 *   that's inherited from the interface.
 * 
 * The actual protocol overhead (header) added to Networker packets is the
 *   following 5-byte mandatory header):
 *      byte type; // 0 == send   1 == ack  (all others discarded)
 *      int seqid; // on send, the seqid the sender generated
 *                 // on ack, the seqid is an echo of the one from the send
 * 
 * on send a packet, send it and fix the data structures yourself, no need
 *   to wake up the sender/resender thread; doesn't change the scheduled crap 
 *   b/c you can't schedule something earlier than what already is by definition
 *   of how our limited protocol works.
 * on receive a send, check for dupe, receive if no dupe and send an ack back.
 * on receive an ack, check pendingAckTable. if found, remove from both 
 *   pendingAckTable and sendTable and markComplete() the relevant request in 
 *   the sendTable and call back the app.
 * on timeout on sendTable, markFailed() request and call back the app.
 * 
 */
public class DumbMessenger implements Messenger, NetworkerListener, Runnable {
	
	// Our network packet/datagram types.
	static final byte TYPE_SEND = 0; // "I'm trying to get a message across."
	static final byte TYPE_ACK = 1; // "I'm acknowledging a message you sent."
	static final int HEADER_SIZE = 5; // size in bytes of byte type + int seqid header
	
	// Our retry interval in seconds.
	static final int RETRY_INTERVAL_SECS = 3;

	// Who's sending and receiving datagrams for us.
	Networker networker; 
	
	// The application object that listens to messenger events.
	MessengerListener listener;
	
	// The thread we use to send and re-send message datagrams. It also 
	//   flips the duplcate-receive-protection tables.
	Thread networkThread;
	
	// Data structures used to manage our dumb receives.
	// This protects us from taking e.g. five UDP sends and resends of the same 
	//   message and calling the application 10 times with the same thing 
	//   because to top it off every one of the 5 UDP packets was duplicated 
	//   once by the network.
	// A receipt is completely forgotten in between 10 and 20 minutes elapsed 
	//   (10  minutes is the interval for wiping out one of them).
	Set<DumbMessageId>[] receiptTable = new HashSet[2];
	int receiptPrim = 0;
	int receiptSec = 1;
	long nextReceiptTableFlip = System.currentTimeMillis() + 10 * 60 * 1000;
	
	// The data structures used to manage our dumb sends and resends.
	// The map:
	// This is shared between networkThread and the caller/app thread so it 
	//   is synchronized.
	// It sorts based on the key which is the time when the next CHECK should
	//   be made. A check is either a resend (usual) or if it is the last one
	//   it causes the message to fail delivery.
	// Whenever we reschedule a DumbMessagingRequest to the future, we 
	//   must remove it from the map and re-add it because it is sorted,
	//   so when checking for re-sends we can scan the map until we hit 
	//   a request in the future.
	// When scheduling two requests to the same timestamp, we actually cheat
	//   and keep incrementing by +1 millisecond until we find an empty slot.
	Map<Long, DumbMessage> sendTable = Collections.synchronizedMap(new TreeMap());
	
	// The data structures used to manage our dumb sends and resends.
	// This is shared between networkThread and the caller/app thread so it 
	//   is synchronized.
	// This must be consistent with sendTable at all times. The Long value
	//   must map 1:1 to the correct Long key in sendTable at all times.
	// This makes it easy to receive an ack and remove any scheduled 
	//   resend from the sendTable.
	// Also private to the networkThread.
	Map<DumbMessageId, Long> pendingAckTable = Collections.synchronizedMap(new HashMap());
	
	// ========================================================================
	
	// A DumbMessenger is a facehugger alien critter that latches onto a 
	//   Networker's face.
	// The caller still has to "start" the Networker in some way after 
	//   this constructor returns since the Networker interface doesn't allow 
	//   us at present to do so, assuming the concrete Networker implementor 
	//   needs something like that.
	public DumbMessenger(Networker networker, MessengerListener listener) {
		
		// We want to remember you because we want to send() net packets.
		this.networker = networker;  
		
		// It's us (this), for sure! We want to receive() all your net packets!
		networker.setListener(this); 
		
		// And this is OUR client that wants to receive our reliable-delivery-
		//  or-neato-timeout-notification "Messages."
		this.listener = listener;
		
		// And we're starting the thread that runs untill the networker 
		//  reports its own demise through Networker.isDead().
		networkThread = new Thread(this);
		networkThread.start();
	}

	/*
	 * ---------------------------------------------------------------------
	 * Messenger interface implementation.
	 * ---------------------------------------------------------------------
	 */
	
	@Override
	public MessengerListener getListener() {
		return listener;
	}
	
	// helper: get an OK unique time key to use on sendTable
	private synchronized Long findUnusedTimeKeyOnSendTable(Long t) {
		while (sendTable.containsKey(t)) { ++t; } // +1 millisecond
		return t;
	}
	
	// helper; keep both "send" data structures synced when inserting a send check
	private synchronized void insertSendCheck(Long time, DumbMessage message) {
		sendTable.put(time, message);
		pendingAckTable.put(message.getMessageId(), time);
	}
	
	// helper; delete a send check. you may reinsert shortly after this.
	// be careful to synchronize with networker.receive, which should not be
	//   able to see the intermediary state between removing and rescheduling
	//   (reinserting) a check.
	private synchronized DumbMessage removeSendCheck(Long time) {
		DumbMessage message = sendTable.remove(time);
		pendingAckTable.remove(message.messageId); // if this throws nullexception the code is logically broken
		return message;
	}
		
	@Override
	public Object send(NetId receiver, byte[] message) {

		if (networker.isDead())
			return null;
		
		// Thankfully, Networker and the data structures are all synchronized.
		// Thanks Java!
		
		// A new message (new seqid).
		DumbMessage newMessage = new DumbMessage(receiver, message);
		
		// File the next check (a resend, probably)
		//Long nextCheck = System.currentTimeMillis() + RETRY_INTERVAL_SECS * 1000;
		//
		// actually since we can't send the first try, the network thread 
		// will have to do it for us, ASAP (hence System.currentTime... == send
		// "now").
		// file the first check, which is the first send.
		//
		// can remove findUnused...() if you make sendTable a MultiMap
		Long keyToUse = findUnusedTimeKeyOnSendTable(System.currentTimeMillis());
		
		newMessage.setTryTime(keyToUse);
		//newMessage.incrementTryCount(); // counting actual packet sends 
		  // BUT NOT SENT.

		/*
		// Store the thing to check next
		sendTable.put(keyToUse, newMessage);
		// Sync pendingAckTable
		pendingAckTable.put(newMessage.getMessageId(), keyToUse);
		*/
		// Do it the right way: wrap into helpers that keep both in sync.
		insertSendCheck(keyToUse, newMessage);
		
		// THIS IS WRONG
		// BECAUSE THE NETWORK THREAD RECEIVES AND CAN CALL YOU BACK
		//  BEFORE YOU GET THE SEQUENCE NUMBER BACK.
		//
		// Send it for the first time.
		//netSendMessage(newMessage);
		//********************************************************
		// we have NO choice but to wake up the networker thread
		// to send it to us.
		// what we will do instead is have it sleep every 100ms
		// for a while and rethink when it wakes up, which is
		// CPU-wasteful but can be safely replaced with a wait/
		// notify thingy later.
		//********************************************************
		
		return newMessage.getSequence();
	}

	/*
	 * ---------------------------------------------------------------------
	 * NetworkerListener interface implementation.
	 * ---------------------------------------------------------------------
	 */
	
	@Override
	public void receive(NetId sender, byte[] data) {
		// FIXME: receiver of the reliable scheme that doesn't know how to
		//   send multipart things.
		
		// this has to send an ack back, and register the seq received
		//  in some temp buffer so we protect from duplicates for a while,
		//  so we send as many acks as there are sends, and we receive only
		//  if its not in the received list that lasts quite a while but
		//  way before the seqids wrap around.
		
		// this must synchronize with what the sender does to reschedule
		// checks; it can't see the intermediary state of pendingAckTable
		// where the Long time is changed.
		
		
		
		// TODO: check 5-byte minimum else discard
		
		// TODO: check type 0 or 1 else discard
		
		// TODO: for type 0 (send)
		// -send 1 ack out (immediately/here)
		// -check for already seen and discard if so
		// -else add to already seen to primtable and invoke listener.receive
		
		// TODO: for type 1 (ack)
		// -check pendingAcks to see if there's something there
		//  if not found ignore
		// -if found, 
		//    - block sender
		//    - remove message from sender thread's job list so
		//      it won't fail it 
		//    - unblock sender
		//    - call back completed()
	}

	@Override
	public void killed() {
		// Join with the network thread and dispose of the thread object.
		// The network thread will quit by detecting Networker.isDead() itself.
		try {
			networkThread.interrupt(); // REVIEW: interrupt() might not be needed.
			networkThread.join();
		} catch (InterruptedException ex) {
		}
		networkThread = null;
	}

	/*
	 * ---------------------------------------------------------------------
	 * Private helpers
	 * ---------------------------------------------------------------------
	 */
	
	// Sends one of our packets through the Networkers.
	// Prepends the 5-byte overhead of the DumbMessenger header.
	private void netSendMessage(DumbMessage message) {
		int intendedSize = message.getMessage().length + HEADER_SIZE;
		
		ByteBuffer out = ByteBuffer.allocate(intendedSize);
		out.put(TYPE_SEND);
		out.putInt(message.getSequence());
		out.put(message.getMessage());
		
		// FIXME: remove this later.
		//  this should never trigger in a world that makes sense 
		//  but I want to check.
		if (out.array().length != intendedSize) {
			throw new RuntimeException("Terrible. Redo this code.");
		}
		
		networker.send(message.getReceiver(), out.array());
	}
	
	/*
	 * ---------------------------------------------------------------------
	 * Runnable -- the network send / receive-cleanup thread.
	 * ---------------------------------------------------------------------
	 */

	@Override
	public void run() {
		
		// IMPORTANT: networker.isDead() must be checked periodically or
		//  the operations in which this thread hangs must be interruptible.
		// IMPORTANT: this thread may be interrupt()ed (see killed()) as part
		//  of normal operation. it should handle it as a signal to check 
		//  isDead() immediately (it will be true, probably) and then silently
		//  return from this run() method.
		
		// *********************************************************************
		// *********************************************************************
		// FIXME: we're doing "sleeping for 100ms then I check whether I should
		//   be sending crap." this is 
		//   absolutely UNACCEPTABLE. change to proper 
		//   wait(expected-next-scheduled check)/notify-when-want-to-send-stuff.
		// *********************************************************************
		// *********************************************************************
		
		while (! networker.isDead()) {
			
			// FIXME: HORRIBLE. Rewrite this with wait(long)/notify.
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				// Don't care.
			}
			
			// TODO: Process all expired checks (send tasks).
			// - foreach on map until time in future found
			//   - block receiver
			//   - remove
			//   - if tries left, reinsert reschedule else set tempflag
			//   - unblock receiver
			//   - if no tries were left (tempflag set), call failed()
			
			// TODO: Check for seen-receive flip and do it if needed
			// - if nextflip, flip
		}
	}
}
