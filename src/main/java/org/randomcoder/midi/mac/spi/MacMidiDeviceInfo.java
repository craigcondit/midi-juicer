package org.randomcoder.midi.mac.spi;

import javax.sound.midi.MidiDevice;

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
}
