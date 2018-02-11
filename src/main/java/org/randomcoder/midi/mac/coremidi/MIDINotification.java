package org.randomcoder.midi.mac.coremidi;

import org.randomcoder.midi.mac.MacMidi;

import com.sun.jna.Pointer;

abstract public class MIDINotification {
	private final MIDINotificationMessageID type;

	MIDINotification(MIDINotificationMessageID type) {
		this.type = type;
	}

	public MIDINotificationMessageID getType() {
		return type;
	}

	public static MIDINotification fromNative(Pointer p, int offset) {
		MacMidi.debug("MIDI notification received");
		
		MIDINotificationMessageID type = MIDINotificationMessageID.byValue(p.getInt(offset));
		offset += 4;
		int size = p.getInt(offset) - 8;
		offset += 4;
		return type.fromNative(p, offset, size);
	}

	@Override
	public String toString() {
		return String.format("MIDINotification { type=%s }", type);
	}

	public static class Unknown extends MIDINotification {
		public Unknown() {
			super(MIDINotificationMessageID.kMIDIMsgUnknown);
		}

		@Override
		public String toString() {
			return "MIDINotification<Unknown>";
		}
	}

	public static class SetupChanged extends MIDINotification {
		public SetupChanged() {
			super(MIDINotificationMessageID.kMIDIMsgSetupChanged);
		}

		@Override
		public String toString() {
			return "MIDINotification<SetupChanged>";
		}
	}

	abstract public static class ObjectAddedOrRemoved extends MIDINotification {
		protected final int parent;
		protected final MIDIObjectType parentType;
		protected final int child;
		protected final MIDIObjectType childType;

		public ObjectAddedOrRemoved(MIDINotificationMessageID type, int parent, MIDIObjectType parentType, int child,
				MIDIObjectType childType) {
			super(type);
			this.parent = parent;
			this.parentType = parentType;
			this.child = child;
			this.childType = childType;
		}

		public int getParent() {
			return parent;
		}

		public MIDIObjectType getParentType() {
			return parentType;
		}

		public int getChild() {
			return child;
		}

		public MIDIObjectType getChildType() {
			return childType;
		}
	}

	public static class ObjectAdded extends MIDINotification.ObjectAddedOrRemoved {
		public ObjectAdded(int parent, MIDIObjectType parentType, int child, MIDIObjectType childType) {
			super(MIDINotificationMessageID.kMIDIMsgObjectAdded, parent, parentType, child, childType);
		}

		@Override
		public String toString() {
			return String.format(
					"MIDINotification<ObjectAdded> { parent=%d, parentType=%s, child=%d, childType=%s }",
					parent, parentType, child, childType);
		}
	}

	public static class ObjectRemoved extends MIDINotification.ObjectAddedOrRemoved {
		public ObjectRemoved(int parent, MIDIObjectType parentType, int child, MIDIObjectType childType) {
			super(MIDINotificationMessageID.kMIDIMsgObjectAdded, parent, parentType, child, childType);
		}

		@Override
		public String toString() {
			return String.format(
					"MIDINotification<ObjectRemoved> { parent=%d, parentType=%s, child=%d, childType=%s }",
					parent, parentType, child, childType);
		}
	}

	public static class PropertyChanged extends MIDINotification {
		private final int object;
		private final MIDIObjectType objectType;
		private final String propertyName;

		public PropertyChanged(int object, MIDIObjectType objectType, String propertyName) {
			super(MIDINotificationMessageID.kMIDIMsgPropertyChanged);
			this.object = object;
			this.objectType = objectType;
			this.propertyName = propertyName;
		}

		@Override
		public String toString() {
			return String.format(
					"MIDINotification<PropertyChanged> { object=%d, objectType=%s, propertyName=%s }",
					object, objectType, propertyName);
		}
	}

	public static class ThruConnectionsChanged extends MIDINotification {
		public ThruConnectionsChanged() {
			super(MIDINotificationMessageID.kMIDIMsgThruConnectionsChanged);
		}

		@Override
		public String toString() {
			return "MIDINotification<ThruConnectionsChanged>";
		}
	}

	public static class SerialPortOwnerChanged extends MIDINotification {
		public SerialPortOwnerChanged() {
			super(MIDINotificationMessageID.kMIDIMsgSerialPortOwnerChanged);
		}

		@Override
		public String toString() {
			return "MIDINotification<SerialPortOwnerChanged>";
		}
	}

	public static class IOError extends MIDINotification {
		private final int driverDevice;
		private final CoreMidiError error;
		private final int errorCode;

		public IOError(int driverDevice, CoreMidiError error, int errorCode) {
			super(MIDINotificationMessageID.kMIDIMsgIOError);
			this.driverDevice = driverDevice;
			this.error = error;
			this.errorCode = errorCode;
		}

		public int getDriverDevice() {
			return driverDevice;
		}

		public CoreMidiError getError() {
			return error;
		}

		public int getErrorCode() {
			return errorCode;
		}

		@Override
		public String toString() {
			return String.format(
					"MIDINotification<IOError> { driverDevice=%d, error=%s, errorCode=%d }",
					driverDevice, error, errorCode);
		}
	}

}
