package org.randomcoder.midi.mac;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.midi.MidiDevice;

import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.CoreMidiServiceFactory;
import org.randomcoder.midi.mac.coremidi.MIDINotification;
import org.randomcoder.midi.mac.coremidi.MIDINotificationMessageID;
import org.randomcoder.midi.mac.spi.MacMidiDeviceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacMidi {

	private static final Logger LOG = LoggerFactory.getLogger(MacMidi.class);

	private static final Object lock = new Object();

	private static volatile boolean init = false;
	private static volatile int clientId = -1;
	private static final Set<Runnable> setupChangedListeners = new LinkedHashSet<>();

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

	private static void handleSetupChanged() {
		List<Runnable> handlers = new ArrayList<>();
		synchronized (lock) {
			handlers.addAll(setupChangedListeners);
		}
		for (Runnable handler : handlers) {
			handler.run();
		}
	}

	public static void addSetupChangedListener(Runnable handler) {
		init();
		synchronized (lock) {
			setupChangedListeners.add(handler);
		}
	}

	public static void removeSetupChangedListener(Runnable handler) {
		init();
		synchronized (lock) {
			setupChangedListeners.remove(handler);
		}
	}

	public static int getClientId() {
		init();
		return clientId;
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
								.createClient("MacMidi", (n, c) -> {
									LOG.debug("MIDINotifyProc() called");
									if (MIDINotification.fromNative(n, 0)
											.getType() == MIDINotificationMessageID.kMIDIMsgSetupChanged) {
										handleSetupChanged();
									}
								});
						LOG.debug("Created client");
					});

			init = true;
		}
	}

	public static void shutdown() {
		synchronized (lock) {
			setupChangedListeners.clear();
			if (clientId != -1) {
				RunLoop.getDefault().ifPresentOrElse(
						t -> shutdownInternal(false),
						() -> shutdownInternal(false));
			}
			init = false;
		}
	}

	private static void shutdownInternal(boolean jvmExit) {
		if (jvmExit) {
			LOG.debug("Shutting down due to JVM exit");
		} else if (!RunLoop.executingOnDefaultRunLoop()) {
			LOG.debug("Executing shutdown on thread != default runloop");
		}
		CoreMidi.getInstance().closeClient(clientId);
		clientId = -1;
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
