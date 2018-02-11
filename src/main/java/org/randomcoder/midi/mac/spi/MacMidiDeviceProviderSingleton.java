package org.randomcoder.midi.mac.spi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.dispatch.Dispatch;

public class MacMidiDeviceProviderSingleton extends MidiDeviceProvider {

	private final ConcurrentMap<Integer, MacMidiSource> sources = new ConcurrentHashMap<>();
	private final ConcurrentMap<Integer, MacMidiDestination> destinations = new ConcurrentHashMap<>();

	private volatile boolean available = false;

	@Override
	public boolean isDeviceSupported(MidiDevice.Info info) {
		return info instanceof MacMidiDeviceInfo;
	}

	private boolean available() {
		if (available) {
			return available;
		}
		available = MacMidi.isAvailable();
		return available;
	}

	@Override
	public MidiDevice getDevice(MidiDevice.Info info) {
		if (!available()) {
			throw new UnsupportedOperationException("MacMidi subsystem is unavailable");
		}

		MacMidiDeviceInfo minfo = (MacMidiDeviceInfo) info;
		Integer uniqueId = minfo.getUniqueId();

		switch (minfo.getType()) {
		case SOURCE:
			return sources.compute(uniqueId, (k, v) -> (v == null) ? new MacMidiSource(minfo) : v);
		case DESTINATION:
			return destinations.compute(uniqueId, (k, v) -> (v == null) ? new MacMidiDestination(minfo) : v);
		default:
			throw new IllegalArgumentException(
					String.format("Unknown device type %s", minfo.getType()));
		}
	}

	@Override
	public MidiDevice.Info[] getDeviceInfo() {
		if (!available()) {
			return new MidiDevice.Info[0];
		}

		AtomicReference<MidiDevice.Info[]> result = new AtomicReference<>(new MidiDevice.Info[0]);
		Dispatch.getInstance().runOnMainThread(null, c -> {
			try {
				result.set(MacMidiDeviceInfo.getDeviceInfo());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return result.get();
	}

}
