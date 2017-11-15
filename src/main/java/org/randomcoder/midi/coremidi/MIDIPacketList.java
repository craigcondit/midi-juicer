package org.randomcoder.midi.coremidi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

public class MIDIPacketList {

    private final List<MIDIPacket> packets = new ArrayList<>();

    public MIDIPacketList() {

    }

    public MIDIPacketList(Pointer pointer, int offset) {
	read(pointer, offset);
    }

    public int getLength() {
	return packets.size();
    }

    public int getRequiredBufferSize() {
	int size = 4;
	for (MIDIPacket packet : packets) {
	    size += packet.getRequiredBufferSize();
	}
	return size;
    }

    public List<MIDIPacket> getPackets() {
	return packets;
    }

    public void read(Pointer p, int offset) {
	int numPackets = p.getInt(offset);
	offset += 4;
	packets.clear();
	for (int i = 0; i < numPackets; i++) {
	    long timeStamp = p.getLong(offset);
	    offset += 8;
	    int length = p.getShort(offset) & 0xffff;
	    offset += 2;
	    int arraySize = length;
	    byte[] data = p.getByteArray(offset, arraySize);
	    offset += arraySize;
	    packets.add(new MIDIPacket(timeStamp, data));
	}
    }

    public Pointer write() {
	Memory m = new Memory(getRequiredBufferSize());
	long offset = 0L;
	m.setInt(offset, packets.size());
	offset += 4;
	for (MIDIPacket packet : packets) {
	    m.setLong(offset, packet.getTimeStamp());
	    offset += 8L;
	    m.setShort(offset, (short) packet.getLength());
	    offset += 2L;
	    m.write(offset, packet.getData(), 0, packet.getLength());
	    offset += packet.getLength();
	}

	System.out.printf("Wrote MIDIPacketList of size %d (expected %d)%n",
		offset, getRequiredBufferSize());

	return m;
    }

    @Override
    public String toString() {
	return String.format("MIDIPacketList { size: %d }", packets.size());
    }
}
