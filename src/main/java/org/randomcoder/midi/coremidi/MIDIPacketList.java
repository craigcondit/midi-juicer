package org.randomcoder.midi.coremidi;

import com.sun.jna.Pointer;

public class MIDIPacketList {
	/**
	 * The number of MIDIPackets in the list.
	 */
	public int numPackets;

	/**
	 * An open-ended array of variable-length MIDIPackets.
	 */
	public MIDIPacket packet[];

	public MIDIPacketList(Pointer pointer, int offset) {
		read(pointer, offset);
	}

	public void read(Pointer p, int offset) {
		numPackets = p.getInt(offset);
		offset += 4;
		packet = new MIDIPacket[numPackets];
		for (int i = 0; i < numPackets; i++) {
			long timeStamp = p.getLong(offset);
			offset += 8;
			int length = p.getShort(offset) & 0xffff;
			offset += 2;
			int arraySize = (length <= 256) ? 256 : length;
			byte[] data = p.getByteArray(offset, arraySize);
			packet[i] = new MIDIPacket(timeStamp, length, data);
		}
	}

	@Override
	public String toString() {
		return String.format("MIDIPacketList { numPackets: %d }", numPackets);
	}
}
