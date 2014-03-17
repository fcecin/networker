package org.sneer.networker.messenger.dumb;

import java.util.Objects;
import org.sneer.networker.NetId;

/**
 * What "uniquely" (within an heuristic timeframe) identifies a message
 *   send or ack: a sender (or acker) address and a sequence number.
 * 
 * This is used to identify both incoming messages already seen and local 
 *   messages that are pending a remote acknowledgement.
 * 
 * Internal helper for this package.
 */
class DumbMessageId {

	private Integer seq;
	private NetId addr;

	public DumbMessageId(int seq, NetId addr) {
		this.seq = seq;
		this.addr = addr;
	}

	public Integer getSequence() {
		return seq;
	}

	public NetId getAddr() {
		return addr;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + this.seq;
		hash = 71 * hash + Objects.hashCode(this.addr);
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
		final DumbMessageId other = (DumbMessageId) obj;
		if (this.seq != other.seq) {
			return false;
		}
		if (!Objects.equals(this.addr, other.addr)) {
			return false;
		}
		return true;
	}
}
