package org.randomcoder.midi.mac;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.sound.midi.MidiDevice;

import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.MIDINotification;
import org.randomcoder.midi.mac.spi.AbstractMacMidiDevice;
import org.randomcoder.midi.mac.spi.MacMidiDeviceInfo;

public class MacMidi {
	private static final Object lock = new Object();

	private static volatile boolean init = false;
	private static volatile Integer clientId;
	private static final Set<Consumer<MIDINotification.SetupChanged>> setupChangedListeners = new LinkedHashSet<>();
	private static final Set<Consumer<MIDINotification.IOError>> ioErrorListeners = new LinkedHashSet<>();

	public static boolean isAvailable() {
		try {
			CoreMidi.getInstance().getNumberOfDevices();
		} catch (Throwable t) {
			return false;
		}

		return true;
	}

	private static void onMidiEvent(MIDINotification event) {
		System.out.println("onMidiEvent");
		System.out.println(event);

		switch (event.getType()) {
		case kMIDIMsgSetupChanged:
			handleSetupChanged((MIDINotification.SetupChanged) event);
			break;
		case kMIDIMsgIOError:
			handleIoError((MIDINotification.IOError) event);
			break;
		case kMIDIMsgObjectAdded:
			break;
		case kMIDIMsgObjectRemoved:
			break;
		case kMIDIMsgPropertyChanged:
			break;
		case kMIDIMsgSerialPortOwnerChanged:
			break;
		case kMIDIMsgThruConnectionsChanged:
			break;
		case kMIDIMsgUnknown:
			break;
		default:
			break;
		}

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

	private static void handleIoError(MIDINotification.IOError event) {
		List<Consumer<MIDINotification.IOError>> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(ioErrorListeners);
		}
		for (Consumer<MIDINotification.IOError> handler : handlers) {
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

	public static void addIoErrorListener(Consumer<MIDINotification.IOError> handler) {
		init();
		synchronized (lock) {
			ioErrorListeners.add(handler);
		}
	}

	public static void removeIoErrorListener(Consumer<MIDINotification.IOError> handler) {
		init();
		synchronized (lock) {
			ioErrorListeners.remove(handler);
		}
	}

	public static void init() {
		synchronized (lock) {
			if (init) {
				return;
			}

			clientId = CoreMidi.getInstance()
					.createClient("MacMidi-default-client", MacMidi::onMidiEvent);

			setupChangedListeners.clear();
			ioErrorListeners.clear();

			init = true;
		}
	}

	public static void shutdown() {
		synchronized (lock) {
			ioErrorListeners.clear();
			setupChangedListeners.clear();
			if (clientId != null) {
				CoreMidi.getInstance().closeClient(clientId);
				clientId = null;
			}
			init = false;
		}
	}

	public static boolean isMacMidiDevice(MidiDevice.Info info) {
		return info instanceof MacMidiDeviceInfo;
	}

	public static boolean isMacMidiDevice(MidiDevice device) {
		return device instanceof AbstractMacMidiDevice;
	}

}
