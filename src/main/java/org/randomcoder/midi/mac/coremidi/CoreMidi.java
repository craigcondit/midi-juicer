package org.randomcoder.midi.mac.coremidi;

import java.util.function.Consumer;

import javax.sound.midi.MidiMessage;

import org.randomcoder.midi.mac.corefoundation.CFStringRef;
import org.randomcoder.midi.mac.corefoundation.CoreFoundationPeer;
import org.randomcoder.midi.mac.corefoundation.CoreFoundationServiceFactory;
import org.randomcoder.midi.mac.spi.MacRunLoopThread;
import org.randomcoder.midi.mac.system.SystemPeer;
import org.randomcoder.midi.mac.system.SystemServiceFactory;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class CoreMidi {

	private static CoreMidi INSTANCE = new CoreMidi();

	public static CoreMidi getInstance() {
		return INSTANCE;
	}

	static void setInstance(CoreMidi instance) {
		INSTANCE = instance;
	}

	CoreMidi() {

	}

	CoreFoundationPeer cf() {
		return CoreFoundationServiceFactory.getPeer();
	}

	CoreMidiPeer peer() {
		return CoreMidiServiceFactory.getPeer();
	}

	SystemPeer sp() {
		return SystemServiceFactory.getPeer();
	}

	CoreMidiPropertyResolver resolver() {
		return CoreMidiServiceFactory.getPropertyResolver();
	}

	public void closeOutputPort(int outputPortId) {
		peer().MIDIPortDispose(outputPortId);
	}

	public void closeClient(int clientId) {
		peer().MIDIClientDispose(clientId);
	}

	public int getDeviceRefByUniqueID(int uniqueId) {
		IntByReference sourceRef = new IntByReference();
		IntByReference sourceTypeRef = new IntByReference();
		int result = peer().MIDIObjectFindByUniqueID(uniqueId, sourceRef, sourceTypeRef);

		if (result != 0) {
			throw new RuntimeException(String.format("Unable to find MIDI device (error %d)", result));
		}
		return sourceRef.getValue();
	}

	public int createOutputPort(String name, int clientId) {
		CoreMidiPeer peer = peer();
		CoreFoundationPeer cf = cf();

		CFStringRef outputName = CFStringRef.createNative(name);
		try {
			IntByReference outputPortRef = new IntByReference();

			int result = peer.MIDIOutputPortCreate(clientId, outputName, outputPortRef);
			if (result != 0) {
				throw new RuntimeException(String.format("Unable to create MIDI output port (error %d)", result));
			}
			return outputPortRef.getValue();
		} finally {
			cf.CFRelease(outputName.getPointer());
		}
	}

	public int createClient(String name) {
		return createClient(name, null, null);
	}

	public int createClient(String name,
			MacRunLoopThread runLoopThread,
			Consumer<MIDINotification> midiNotificationHandler) {

		if (midiNotificationHandler != null && runLoopThread == null) {
			throw new IllegalArgumentException(
					"runLoopThread must be specified if midiNotificationHandler is specified");
		}

		CoreMidiPeer peer = peer();
		CoreFoundationPeer cf = cf();

		CFStringRef nameRef = CFStringRef.createNative(name);

		try {
			IntByReference clientPtr = new IntByReference();

			MIDINotifyProc proc = (message, refCon) -> {
				if (midiNotificationHandler != null) {
					MIDINotification notify = MIDINotification.fromNative(message, 0);
					midiNotificationHandler.accept(notify);
				}
			};

			int result;
			try {
				if (runLoopThread == null) {
					result = peer.MIDIClientCreate(nameRef, null, null, clientPtr);
				} else {
					result = runLoopThread.execute(() -> peer.MIDIClientCreate(nameRef, proc, null, clientPtr));
				}
			} catch (Exception e) {
				throw new RuntimeException("Unable to create MIDI client", e);
			}
			if (result != 0) {
				throw new RuntimeException(String.format("Unable to create MIDI client (error %d)", result));
			}

			return clientPtr.getValue();
		} finally {
			cf.CFRelease(nameRef.getPointer());
		}
	}

	public MacRunLoopThread createRunLoopThread(String name) {
		return new MacRunLoopThread(name, cf());
	}

	public void sendMidi(MidiMessage message, long timestamp, int outputPortId, int destinationId) {

		CoreMidiPeer peer = peer();
		SystemPeer sp = sp();

		long ts = sp.mach_absolute_time();

		MIDIPacketList pl = new MIDIPacketList();
		pl.getPackets().add(new MIDIPacket(ts, message.getMessage()));
		Pointer p = pl.write();
		int result = peer.MIDISend(outputPortId, destinationId, p);

		if (result != 0) {
			throw new RuntimeException(String.format("Error during MIDISend (%d)", result));
		}
	}

	public int getNumberOfDevices() {
		return peer().MIDIGetNumberOfDevices();
	}

	public int getNumberOfSources() {
		return peer().MIDIGetNumberOfSources();
	}

	public int getNumberOfDestinations() {
		return peer().MIDIGetNumberOfDestinations();
	}

	public int getSource(int sourceIndex0) {
		return peer().MIDIGetSource(sourceIndex0);
	}

	public int getDestination(int destIndex0) {
		return peer().MIDIGetDestination(destIndex0);
	}

	public String getStringProperty(CoreMidiProperty prop, int handle) {
		return stringProperty(cf(), peer(), resolver(), handle, prop);
	}

	public Integer getIntegerProperty(CoreMidiProperty prop, int handle) {
		return intProperty(peer(), resolver(), handle, prop);
	}

	protected Integer intProperty(
			CoreMidiPeer peer,
			CoreMidiPropertyResolver resolver,
			int device,
			CoreMidiProperty property) {

		CFStringRef propRef = property.resolve(resolver);

		Memory m = new Memory(4);
		int status = peer.MIDIObjectGetIntegerProperty(device, propRef, m);
		CoreMidiError error = CoreMidiError.byErrorCode(status);
		if (error == CoreMidiError.kMIDIUnknownProperty) {
			return null;
		} else if (error != CoreMidiError.kNoError) {
			throw CoreMidiException.fromError(error.errorCode());
		}
		return m.getInt(0);
	}

	protected String stringProperty(
			CoreFoundationPeer cf,
			CoreMidiPeer peer,
			CoreMidiPropertyResolver resolver,
			int device,
			CoreMidiProperty property) {
		CFStringRef propRef = property.resolve(resolver);

		Memory m = new Memory(8);
		int status = peer.MIDIObjectGetStringProperty(device, propRef, m);
		CoreMidiError error = CoreMidiError.byErrorCode(status);
		if (error == CoreMidiError.kMIDIUnknownProperty) {
			return null;
		} else if (error != CoreMidiError.kNoError) {
			throw CoreMidiException.fromError(error.errorCode());
		}

		Pointer ptr = m.getPointer(0);
		if (Pointer.nativeValue(ptr) == 0L) {
			return null;
		}
		CFStringRef r = new CFStringRef(ptr);
		String result = r.toString();
		cf.CFRelease(r.getPointer());
		return result;
	}

}
