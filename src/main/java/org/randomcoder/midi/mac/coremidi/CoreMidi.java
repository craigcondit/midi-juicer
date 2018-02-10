package org.randomcoder.midi.mac.coremidi;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.sound.midi.MidiMessage;

import org.randomcoder.midi.mac.corefoundation.CFRunLoop;
import org.randomcoder.midi.mac.corefoundation.CFStringRef;
import org.randomcoder.midi.mac.corefoundation.CoreFoundationPeer;
import org.randomcoder.midi.mac.corefoundation.CoreFoundationServiceFactory;
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

	private final Memory notifyRefCon;

	private final Map<Integer, MIDINotifyProc> notifyProcs = new ConcurrentHashMap<>();
	private final Map<Integer, MIDIReadProc> readProcs = new ConcurrentHashMap<>();

	CoreMidi() {
		notifyRefCon = new Memory(8L);
		notifyRefCon.setLong(0L, 0L);
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

	public void closeInputPort(int inputPortId) {
		peer().MIDIPortDispose(inputPortId);
	}

	public void closeClient(int clientId) {
		peer().MIDIClientDispose(clientId);
		notifyProcs.remove(Integer.valueOf(clientId));
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

			final AtomicInteger result = new AtomicInteger();
			try {
				CFRunLoop.invokeAndWait(() -> {
					result.set(peer.MIDIOutputPortCreate(clientId, outputName, outputPortRef));
				});
			} catch (InterruptedException e) {
				throw new IllegalStateException("CFRunLoop shutting down");
			}
			if (result.get() != 0) {
				throw new RuntimeException(String.format("Unable to create MIDI output port (error %d)",
						result.get()));
			}
			return outputPortRef.getValue();
		} finally {
			cf.CFRelease(outputName.getPointer());
		}
	}

	public int createInputPort(String name, int clientId, MIDIReadProc handler) {
		Objects.requireNonNull(handler);

		CoreMidiPeer peer = peer();
		CoreFoundationPeer cf = cf();

		CFStringRef inputName = CFStringRef.createNative(name);
		try {
			IntByReference inputPortRef = new IntByReference();

			final AtomicInteger result = new AtomicInteger();
			try {
				CFRunLoop.invokeAndWait(() -> {
					result.set(peer.MIDIInputPortCreate(clientId, inputName, handler, null, inputPortRef));
				});
			} catch (InterruptedException e) {
				throw new IllegalStateException("CFRunLoop shutting down");
			}

			if (result.get() != 0) {
				throw new RuntimeException(String.format(
						"Unable to create MIDI input port (error %d)", result.get()));
			}
			int inputPortId = inputPortRef.getValue();
			readProcs.put(Integer.valueOf(inputPortId), handler);
			return inputPortId;
		} finally {
			cf.CFRelease(inputName.getPointer());
		}
	}

	public Pointer connectSource(int port, int source) {
		CoreMidiPeer peer = peer();

		Memory connRefCon = new Memory(8);
		connRefCon.setLong(0L, 0L);

		final AtomicInteger result = new AtomicInteger();

		try {
			CFRunLoop.invokeAndWait(() -> {
				result.set(peer.MIDIPortConnectSource(port, source, connRefCon));
			});
		} catch (InterruptedException e) {
			throw new IllegalStateException("CFRunLoop shutting down");
		}
		if (result.get() != 0) {
			throw new RuntimeException(String.format("Unable to create MIDI output port (error %d)",
					result.get()));
		}

		return connRefCon;
	}

	public void disconnectSource(int port, int source, Pointer connRef) {
		CoreMidiPeer peer = peer();
		peer.MIDIPortDisconnectSource(port, source);
	}

	public int createClient(String name) {
		return createClient(name, null);
	}

	public int createClient(String name, Consumer<MIDINotification> midiNotificationHandler) {

		CoreMidiPeer peer = peer();
		CoreFoundationPeer cf = cf();

		CFStringRef nameRef = CFStringRef.createNative(name);
		try {
			IntByReference clientPtr = new IntByReference();

			final AtomicInteger result = new AtomicInteger();
			final AtomicReference<MIDINotifyProc> notifyProc = new AtomicReference<>();

			try {
				CFRunLoop.invokeAndWait(() -> {
					System.out.printf("Creating MIDI client with ID %s%n", name);
					if (midiNotificationHandler == null) {
						result.set(peer.MIDIClientCreate(nameRef, null, notifyRefCon, clientPtr));
						System.out.printf("Creating MIDI client with ID %s and no listener%n", name);
					} else {
						MIDINotifyProc proc = (message, refCon) -> {
							MIDINotification notify = MIDINotification.fromNative(message, 0);
							midiNotificationHandler.accept(notify);
						};
						notifyProc.set(proc);
						result.set(peer.MIDIClientCreate(nameRef, proc, notifyRefCon, clientPtr));
						System.out.printf("Creating MIDI client with ID %s and a listener%n", name);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException("Unable to create MIDI client", e);
			}
			if (result.get() != 0) {
				throw new RuntimeException(String.format("Unable to create MIDI client (error %d)", result.get()));
			}

			int clientId = clientPtr.getValue();
			if (notifyProc.get() != null) {
				notifyProcs.put(Integer.valueOf(clientId), notifyProc.get());
			}
			return clientId;
		} finally {
			cf.CFRelease(nameRef.getPointer());
		}
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
