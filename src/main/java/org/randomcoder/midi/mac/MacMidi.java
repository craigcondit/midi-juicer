package org.randomcoder.midi.mac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;

import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.CoreMidiException;
import org.randomcoder.midi.mac.coremidi.MIDINotification;
import org.randomcoder.midi.mac.coremidi.MIDINotificationMessageID;
import org.randomcoder.midi.mac.dispatch.Dispatch;
import org.randomcoder.midi.mac.spi.AbstractMacMidiDevice;
import org.randomcoder.midi.mac.spi.MacMidiDeviceInfo;

public class MacMidi {
	private static final Object lock = new Object();

	private static volatile boolean init = false;
	private static volatile Integer clientId;
	private static volatile SetupChangePoller poller;
	private static volatile String currentSetup = "";
	private static final Set<Runnable> setupChangedListeners = new LinkedHashSet<>();
	private static volatile boolean debug = false;

	public static void setDebug(boolean value) {
		debug = value;
	}

	public static void debug(String format, Object... args) {
		if (debug) {
			System.err.printf("DEBUG: " + format + "%n", args);
		}
	}

	public static boolean isAvailable() {
		try {
			CoreMidi.getInstance().getNumberOfDevices();
		} catch (Throwable t) {
			return false;
		}

		return true;
	}

	private static void handleSetupChanged() {
		refreshSetup(false);
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

	public static void init() {
		synchronized (lock) {
			if (init) {
				return;
			}

			clientId = CoreMidi.getInstance()
					.createClient("MacMidi-default-client", (n, c) -> {
						if (MIDINotification.fromNative(n, 0)
								.getType() == MIDINotificationMessageID.kMIDIMsgSetupChanged) {
							handleSetupChanged();
						}
					});

			setupChangedListeners.clear();

			// if (isAvailable()) {
			// refreshSetup(false);
			// poller = new SetupChangePoller();
			// poller.start();
			// }

			init = true;
		}
	}

	public static void shutdown() {
		synchronized (lock) {
			if (poller != null) {
				poller.shutdown();
			}
			setupChangedListeners.clear();
			if (clientId != null) {
				CoreMidi.getInstance().closeClient(clientId);
				clientId = null;
			}
			init = false;
		}
	}

	static void refreshSetup(boolean sendNotifications) {
		if (!isAvailable()) {
			return;
		}

		Dispatch.getInstance().runOnMainThread(null, c -> {
			try {
				MidiDevice.Info[] infos = MacMidiDeviceInfo.getDeviceInfo();
				String setup = Arrays.stream(infos)
						.filter(info -> info instanceof MacMidiDeviceInfo)
						.map(info -> (MacMidiDeviceInfo) info)
						.map(info -> info.getDescriptor())
						.sorted()
						.collect(Collectors.joining("\n"));

				System.out.printf("Setup: %n%s%n%n", setup);

				if (setup.equals(currentSetup)) {
					return;
				}

				currentSetup = setup;
				if (sendNotifications) {
					handleSetupChanged();
				}
			} catch (CoreMidiException ignored) {
			}
		});
	}

	public static boolean isMacMidiDevice(MidiDevice.Info info) {
		return info instanceof MacMidiDeviceInfo;
	}

	public static boolean isMacMidiDevice(MidiDevice device) {
		return device instanceof AbstractMacMidiDevice;
	}

	private static class SetupChangePoller extends Thread {
		private volatile boolean shutdown = false;

		public SetupChangePoller() {
			super("MIDI setup poller");
			setDaemon(true);
		}

		@Override
		public void run() {
			while (!shutdown) {
				try {
					Thread.sleep(1000L);
					refreshSetup(true);
				} catch (InterruptedException e) {
					return;
				}
			}
		}

		public void shutdown() {
			shutdown = true;
			synchronized (this) {
				interrupt();
			}
			try {
				join();
			} catch (InterruptedException e) {
			}
		}
	}

}
