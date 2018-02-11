package org.randomcoder.midi.mac.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sound.midi.MidiDevice;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.CoreMidiException;
import org.randomcoder.midi.mac.coremidi.CoreMidiProperty;

public class MacMidiDeviceInfo extends MidiDevice.Info {

	private final MacMidiDeviceType type;
	private final int uniqueId;
	private final Integer deviceId;

	protected MacMidiDeviceInfo(String name, String vendor, String description, String version,
			MacMidiDeviceType type, int uniqueId, Integer deviceId) {
		super(name, vendor, description, version);
		this.type = type;
		this.uniqueId = uniqueId;
		this.deviceId = deviceId;
	}

	public MacMidiDeviceType getType() {
		return type;
	}

	public int getUniqueId() {
		return uniqueId;
	}

	public Integer getDeviceId() {
		return deviceId;
	}

	public String getDescriptor() {
		return String.format("%s|%s|%s|%s|%s|%d|%d",
				getName(), getVendor(), getDescription(), getVersion(), type, uniqueId, deviceId);
	}

	public static MidiDevice.Info[] getDeviceInfo() throws CoreMidiException {
		if (!MacMidi.isAvailable()) {
			return new MidiDevice.Info[0];
		}

		CoreMidi midi = CoreMidi.getInstance();

		List<MidiDevice.Info> devices = new ArrayList<>();

		int sourceCount = midi.getNumberOfSources();
		MacMidi.debug("Source count: %d", sourceCount);

		for (int i = 0; i < sourceCount; i++) {
			int handle = midi.getSource(i);
			if (handle == 0) {
				continue;
			}
			createDeviceInfo(midi, handle, MacMidiDeviceType.SOURCE).ifPresent(devices::add);
		}

		int destCount = midi.getNumberOfDestinations();
		MacMidi.debug("Destination count: %d", destCount);

		for (int i = 0; i < destCount; i++) {
			int handle = midi.getDestination(i);
			if (handle == 0) {
				continue;
			}
			createDeviceInfo(midi, handle, MacMidiDeviceType.DESTINATION)
					.ifPresent(devices::add);
		}

		return devices.toArray(new MidiDevice.Info[devices.size()]);
	}

	public static Optional<MacMidiDeviceInfo> createDeviceInfo(CoreMidi midi, int handle, MacMidiDeviceType type) {
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
