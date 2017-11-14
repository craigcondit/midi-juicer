package org.randomcoder.midi.coremidi;

public class MIDIPacket {
	/**
	 * The time at which the events occurred, if receiving MIDI, or, if sending
	 * MIDI, the time at which the events are to be played. Zero means "now."
	 * The time stamp applies to the first MIDI byte in the packet.
	 */
	private final long timeStamp;

	/**
	 * A variable-length stream of MIDI messages. Running status is not allowed.
	 * In the case of system-exclusive messages, a packet may only contain a
	 * single message, or portion of one, with no other MIDI events.
	 */
	private final byte[] data;

	public MIDIPacket(long timeStamp, byte[] data) {
		this.timeStamp = timeStamp;
		this.data = data;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public byte[] getData() {
		return data;
	}

	public int getLength() {
		return data.length;
	}

	public int getRequiredBufferSize() {
		return data.length + 10;
	}

	@Override
	public String toString() {
		return String.format("MIDIPacket { timeStamp: %d, length: %d }", timeStamp, data.length);
	}
}
