package org.randomcoder.midi.coremidi;

public class MIDIPacket {
	/**
	 * The time at which the events occurred, if receiving MIDI, or, if sending
	 * MIDI, the time at which the events are to be played. Zero means "now."
	 * The time stamp applies to the first MIDI byte in the packet.
	 */
	public long timeStamp;

	/**
	 * The number of valid MIDI bytes which follow, in data. (It may be larger
	 * than 256 bytes if the packet is dynamically allocated.)
	 */
	public int length; // uint16

	/**
	 * A variable-length stream of MIDI messages. Running status is not allowed.
	 * In the case of system-exclusive messages, a packet may only contain a
	 * single message, or portion of one, with no other MIDI events.
	 */
	public byte[] data;

	public MIDIPacket(long timeStamp, int length, byte[] data) {
		this.timeStamp = timeStamp;
		this.length = length;
		this.data = data;
	}

	@Override
	public String toString() {
		return String.format("MIDIPacket { timeStamp: %d, length: %d }", timeStamp, length);
	}
}
