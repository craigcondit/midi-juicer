package org.randomcoder.midi.mac;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.sound.midi.MidiDevice;

import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.CoreMidiServiceFactory;
import org.randomcoder.midi.mac.coremidi.MIDINotification;
import org.randomcoder.midi.mac.spi.MacMidiDeviceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;

public class MacMidi {

	private static final Logger LOG = LoggerFactory.getLogger(MacMidi.class);

	private static final Object lock = new Object();

	private static volatile boolean init = false;
	private static volatile int clientId = Integer.MIN_VALUE;
	private static final Set<Consumer<MIDINotification.SetupChanged>> setupChangedListeners = new LinkedHashSet<>();
	private static final Set<Consumer<MIDINotification.ObjectAdded>> objectAddedListeners = new LinkedHashSet<>();
	private static final Set<Consumer<MIDINotification.ObjectRemoved>> objectRemovedListeners = new LinkedHashSet<>();
	private static final Set<Consumer<MIDINotification.PropertyChanged>> propertyChangedListeners = new LinkedHashSet<>();
	private static final Set<Consumer<MIDINotification.IOError>> ioErrorListeners = new LinkedHashSet<>();
	private static final Set<Consumer<MIDINotification.ThruConnectionsChanged>> thruConnectionsChangedListeners = new LinkedHashSet<>();
	private static final Set<Consumer<MIDINotification.SerialPortOwnerChanged>> serialPortOwnerChangedListeners = new LinkedHashSet<>();

	private static final AtomicReference<Boolean> available = new AtomicReference<>(null);

	public static boolean available() {
		Boolean status = available.get();
		if (status != null) {
			return status.booleanValue();
		}

		try {
			CoreMidiServiceFactory.getPeer();
		} catch (Throwable t) {
			available.set(Boolean.FALSE);
			return false;
		}
		available.set(Boolean.TRUE);
		return true;
	}

	private static void handleSetupChanged(MIDINotification.SetupChanged event) {
		List<Consumer<MIDINotification.SetupChanged>> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(setupChangedListeners);
		}
		for (Consumer<MIDINotification.SetupChanged> handler : handlers) {
			handler.accept(event);
		}
	}

	private static void handleObjectAdded(MIDINotification.ObjectAdded event) {
		List<Consumer<MIDINotification.ObjectAdded>> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(objectAddedListeners);
		}
		for (Consumer<MIDINotification.ObjectAdded> handler : handlers) {
			handler.accept(event);
		}
	}

	private static void handleObjectRemoved(MIDINotification.ObjectRemoved event) {
		List<Consumer<MIDINotification.ObjectRemoved>> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(objectRemovedListeners);
		}
		for (Consumer<MIDINotification.ObjectRemoved> handler : handlers) {
			handler.accept(event);
		}
	}

	private static void handlePropertyChanged(MIDINotification.PropertyChanged event) {
		List<Consumer<MIDINotification.PropertyChanged>> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(propertyChangedListeners);
		}
		for (Consumer<MIDINotification.PropertyChanged> handler : handlers) {
			handler.accept(event);
		}
	}

	private static void handleIOError(MIDINotification.IOError event) {
		List<Consumer<MIDINotification.IOError>> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(ioErrorListeners);
		}
		for (Consumer<MIDINotification.IOError> handler : handlers) {
			handler.accept(event);
		}
	}

	private static void handleThruConnectionsChanged(MIDINotification.ThruConnectionsChanged event) {
		List<Consumer<MIDINotification.ThruConnectionsChanged>> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(thruConnectionsChangedListeners);
		}
		for (Consumer<MIDINotification.ThruConnectionsChanged> handler : handlers) {
			handler.accept(event);
		}
	}

	private static void handleSerialPortOwnerChanged(MIDINotification.SerialPortOwnerChanged event) {
		List<Consumer<MIDINotification.SerialPortOwnerChanged>> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(serialPortOwnerChangedListeners);
		}
		for (Consumer<MIDINotification.SerialPortOwnerChanged> handler : handlers) {
			handler.accept(event);
		}
	}

	public static void addSetupChangedListener(Consumer<MIDINotification.SetupChanged> handler) {
		init();
		synchronized (lock) {
			setupChangedListeners.add(handler);
		}
	}

	public static void removeSetupChangedListener(Consumer<MIDINotification.SetupChanged> handler) {
		init();
		synchronized (lock) {
			setupChangedListeners.remove(handler);
		}
	}

	public static void addObjectAddedListener(Consumer<MIDINotification.ObjectAdded> handler) {
		init();
		synchronized (lock) {
			objectAddedListeners.add(handler);
		}
	}

	public static void removeObjectAddedListener(Consumer<MIDINotification.ObjectAdded> handler) {
		init();
		synchronized (lock) {
			objectAddedListeners.remove(handler);
		}
	}

	public static void addObjectRemovedListener(Consumer<MIDINotification.ObjectRemoved> handler) {
		init();
		synchronized (lock) {
			objectRemovedListeners.add(handler);
		}
	}

	public static void removeObjectRemovedListener(Consumer<MIDINotification.ObjectRemoved> handler) {
		init();
		synchronized (lock) {
			objectRemovedListeners.remove(handler);
		}
	}

	public static void addPropertyChangedListener(Consumer<MIDINotification.PropertyChanged> handler) {
		init();
		synchronized (lock) {
			propertyChangedListeners.add(handler);
		}
	}

	public static void removePropertyChangedListener(Consumer<MIDINotification.PropertyChanged> handler) {
		init();
		synchronized (lock) {
			propertyChangedListeners.remove(handler);
		}
	}

	public static void addIOErrorListener(Consumer<MIDINotification.IOError> handler) {
		init();
		synchronized (lock) {
			ioErrorListeners.add(handler);
		}
	}

	public static void removeIOErrorListener(Consumer<MIDINotification.IOError> handler) {
		init();
		synchronized (lock) {
			ioErrorListeners.remove(handler);
		}
	}

	public static void addThruConnectionsChangedListener(Consumer<MIDINotification.ThruConnectionsChanged> handler) {
		init();
		synchronized (lock) {
			thruConnectionsChangedListeners.add(handler);
		}
	}

	public static void removeThruConnectionsChangedListener(Consumer<MIDINotification.ThruConnectionsChanged> handler) {
		init();
		synchronized (lock) {
			thruConnectionsChangedListeners.remove(handler);
		}
	}

	public static void addSerialPortOwnerChangedListener(Consumer<MIDINotification.SerialPortOwnerChanged> handler) {
		init();
		synchronized (lock) {
			serialPortOwnerChangedListeners.add(handler);
		}
	}

	public static void removeSerialPortOwnerChangedListener(Consumer<MIDINotification.SerialPortOwnerChanged> handler) {
		init();
		synchronized (lock) {
			serialPortOwnerChangedListeners.remove(handler);
		}
	}

	public static int getClientId() {
		init();
		return clientId;
	}

	private static void handleMidiNotification(Pointer nRef, Pointer context) {
		MIDINotification notification = MIDINotification.fromNative(nRef, 0);

		if (LOG.isTraceEnabled()) {
			LOG.trace("MIDI notification received: {}", notification);
		}

		switch (notification.getType()) {
		case kMIDIMsgIOError:
			handleIOError((MIDINotification.IOError) notification);
			break;
		case kMIDIMsgObjectAdded:
			handleObjectAdded((MIDINotification.ObjectAdded) notification);
			break;
		case kMIDIMsgObjectRemoved:
			handleObjectRemoved((MIDINotification.ObjectRemoved) notification);
			break;
		case kMIDIMsgPropertyChanged:
			handlePropertyChanged((MIDINotification.PropertyChanged) notification);
			break;
		case kMIDIMsgSerialPortOwnerChanged:
			handleSerialPortOwnerChanged((MIDINotification.SerialPortOwnerChanged) notification);
			break;
		case kMIDIMsgSetupChanged:
			handleSetupChanged((MIDINotification.SetupChanged) notification);
			break;
		case kMIDIMsgThruConnectionsChanged:
			handleThruConnectionsChanged((MIDINotification.ThruConnectionsChanged) notification);
			break;
		default:
			break;
		}
	}

	public static void init() {
		if (!available()) {
			throw new IllegalStateException("CoreMIDI is not available");
		}

		synchronized (lock) {
			if (init) {
				return;
			}

			Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownInternal(true)));

			RunLoop.getDefault()
					.orElseThrow(() -> new IllegalStateException("Default runloop not set"))
					.invokeAndWait(() -> {
						clientId = CoreMidi.getInstance()
								.createClient("MacMidiNotifications", MacMidi::handleMidiNotification);
						LOG.debug("Created client");
					});

			init = true;
		}
	}

	public static void shutdown() {
		synchronized (lock) {
			setupChangedListeners.clear();
			objectAddedListeners.clear();
			objectRemovedListeners.clear();
			propertyChangedListeners.clear();
			ioErrorListeners.clear();
			thruConnectionsChangedListeners.clear();
			serialPortOwnerChangedListeners.clear();

			if (clientId != Integer.MIN_VALUE) {
				RunLoop.getDefault().ifPresentOrElse(
						t -> shutdownInternal(false),
						() -> shutdownInternal(false));
			}

			init = false;
		}
	}

	private static void shutdownInternal(boolean jvmExit) {
		if (clientId != Integer.MIN_VALUE) {
			if (jvmExit) {
				LOG.debug("Shutting down due to JVM exit");
			} else if (!RunLoop.executingOnDefaultRunLoop()) {
				LOG.debug("Executing shutdown on thread != default runloop");
			}
			CoreMidi.getInstance().closeClient(clientId);
			clientId = Integer.MIN_VALUE;
		}
	}

	// native interface

	public static boolean isDeviceSupported(MidiDevice.Info info) {
		return MacMidiDeviceProvider.getInstance().isDeviceSupported(info);
	}

	public static MidiDevice getDevice(MidiDevice.Info info) {
		return MacMidiDeviceProvider.getInstance().getDevice(info);
	}

	public static MidiDevice.Info[] getDeviceInfo() {
		return MacMidiDeviceProvider.getInstance().getDeviceInfo();
	}

	// extended interface

	public static boolean isMacMidiDevice(MidiDevice.Info info) {
		return MacMidiDeviceProvider.getInstance().isDeviceSupported(info);
	}

	public static boolean isMacMidiDevice(MidiDevice device) {
		return MacMidiDeviceProvider.getInstance().isDeviceSupported(device.getDeviceInfo());
	}

}
