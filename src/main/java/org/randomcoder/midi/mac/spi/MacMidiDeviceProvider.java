package org.randomcoder.midi.mac.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.CoreMidiProperty;

public class MacMidiDeviceProvider extends MidiDeviceProvider {

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

		CoreMidi midi = CoreMidi.getInstance();

		List<MidiDevice.Info> devices = new ArrayList<>();

		int sourceCount = midi.getNumberOfSources();
		System.out.printf("Source count: %d%n", sourceCount);

		for (int i = 0; i < sourceCount; i++) {
			int handle = midi.getSource(i);
			if (handle == 0) {
				continue;
			}
			createDeviceInfo(midi, handle, MacMidiDeviceType.SOURCE).ifPresent(devices::add);
		}

		int destCount = midi.getNumberOfDestinations();
		System.out.printf("Destination count: %d%n", destCount);

		for (int i = 0; i < destCount; i++) {
			int handle = midi.getDestination(i);
			if (handle == 0) {
				continue;
			}
			createDeviceInfo(midi, handle, MacMidiDeviceType.DESTINATION).ifPresent(devices::add);
		}

		return devices.toArray(new MidiDevice.Info[devices.size()]);
	}

	private Optional<MacMidiDeviceInfo> createDeviceInfo(CoreMidi midi, int handle, MacMidiDeviceType type) {
		Integer uniqueId = midi.getIntegerProperty(CoreMidiProperty.kMIDIPropertyUniqueID, handle);
		Integer deviceId = midi.getIntegerProperty(CoreMidiProperty.kMIDIPropertyDeviceID, handle);
		String name = midi.getStringProperty(CoreMidiProperty.kMIDIPropertyName, handle);
		String displayName = midi.getStringProperty(CoreMidiProperty.kMIDIPropertyDisplayName, handle);
		String manufacturer = midi.getStringProperty(CoreMidiProperty.kMIDIPropertyManufacturer, handle);
		Integer driverVersion = midi.getIntegerProperty(CoreMidiProperty.kMIDIPropertyDriverVersion, handle);

		if (uniqueId == null) {
			return Optional.empty();
		}

		if (name == null) {
			name = displayName;
		}
		if (name == null) {
			name = "Unknown Name";
		}
		if (displayName == null) {
			displayName = "Unknown description";
		}
		if (manufacturer == null || manufacturer.isEmpty()) {
			manufacturer = "Unknown vendor";
		}
		String version = (driverVersion == null)
				? "Unknown version"
				: String.format("Version %d", driverVersion);

		return Optional.of(new MacMidiDeviceInfo(name, manufacturer, displayName, version, type, uniqueId, deviceId));
	}

}
