package org.randomcoder.midi.mac.coremidi;

import java.util.Arrays;

import org.randomcoder.midi.mac.corefoundation.CFStringRef;

import com.sun.jna.Pointer;

public enum MIDINotificationMessageID {
	kMIDIMsgUnknown(-1, (p, o, l) -> new MIDINotification.Unknown()),
	kMIDIMsgSetupChanged(1, (p, o, l) -> new MIDINotification.SetupChanged()),
	kMIDIMsgObjectAdded(2, (p, o, l) -> {
		if (l < 16) {
			// invalid size, just ignore it
			return new MIDINotification.Unknown();
		}
		return new MIDINotification.ObjectAdded(
				p.getInt(o),
				MIDIObjectType.byValue(p.getInt(o + 4)),
				p.getInt(o + 8),
				MIDIObjectType.byValue(p.getInt(o + 12)));
	}),
	kMIDIMsgObjectRemoved(3, (p, o, l) -> {
		if (l < 16) {
			// invalid size, just ignore it
			return new MIDINotification.Unknown();
		}
		return new MIDINotification.ObjectRemoved(
				p.getInt(o),
				MIDIObjectType.byValue(p.getInt(o + 4)),
				p.getInt(o + 8),
				MIDIObjectType.byValue(p.getInt(o + 12)));
	}),
	kMIDIMsgPropertyChanged(4, (p, o, l) -> {
		if (l < 16) {
			// invalid size, just ignore it
			return new MIDINotification.Unknown();
		}
		CFStringRef cfstr = new CFStringRef(p.getPointer(o + 8));
		return new MIDINotification.PropertyChanged(
				p.getInt(o),
				MIDIObjectType.byValue(p.getInt(o + 4)),
				cfstr.toString());
	}),
	kMIDIMsgThruConnectionsChanged(5, (p, o, l) -> new MIDINotification.ThruConnectionsChanged()),
	kMIDIMsgSerialPortOwnerChanged(6, (p, o, l) -> new MIDINotification.SerialPortOwnerChanged()),
	kMIDIMsgIOError(7, (p, o, l) -> {
		if (l < 8) {
			// invalid size, just ignore it
			return new MIDINotification.Unknown();
		}
		int driverDevice = p.getInt(0);
		int errorCode = p.getInt(o + 4);
		CoreMidiError error = CoreMidiError.byErrorCode(errorCode);
		return new MIDINotification.IOError(driverDevice, error, errorCode);
	});

	private final int value;
	private final MIDINotificationCreator creator;

	private interface MIDINotificationCreator {
		public MIDINotification create(Pointer p, int offset, int length);
	}

	private MIDINotificationMessageID(int value, MIDINotificationCreator creator) {
		this.value = value;
		this.creator = creator;
	}

	public MIDINotification fromNative(Pointer p, int offset, int length) {
		return creator == null ? null : creator.create(p, offset, length);
	}

	public static MIDINotificationMessageID byValue(final int value) {
		return Arrays.stream(MIDINotificationMessageID.values())
				.filter(e -> e.value == value)
				.findFirst()
				.orElse(kMIDIMsgUnknown);
	}
}
