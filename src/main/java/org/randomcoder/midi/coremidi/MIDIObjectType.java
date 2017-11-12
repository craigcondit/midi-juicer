package org.randomcoder.midi.coremidi;

import java.util.Arrays;

public enum MIDIObjectType {
	kMIDIObjectType_Other(-1),
	kMIDIObjectType_Device(0),
	kMIDIObjectType_Entity(1),
	kMIDIObjectType_Source(2),
	kMIDIObjectType_Destination(3),
	kMIDIObjectType_ExternalDevice(0x10),
	kMIDIObjectType_ExternalEntity(0x11),
	kMIDIObjectType_ExternalSource(0x12),
	kMIDIObjectType_ExternalDestination(0x13);

	public static final int kMIDIObjectType_ExternalMask = 0x10;

	private final int value;

	private MIDIObjectType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public boolean external() {
		return (value > 0) &&
				((value & kMIDIObjectType_ExternalMask) == kMIDIObjectType_ExternalMask);
	}

	public static MIDIObjectType byValue(final int value) {
		return Arrays.stream(MIDIObjectType.values())
				.filter(e -> e.value == value)
				.findFirst()
				.orElse(kMIDIObjectType_Other);
	}
}
