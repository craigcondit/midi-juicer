package org.randomcoder.midi.mac.spi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

public class MacMidiDeviceProvider extends MidiDeviceProvider {

	private static final MacMidiDeviceProviderSingleton SINGLETON = new MacMidiDeviceProviderSingleton();

	public static MidiDeviceProvider getInstance() {
		return SINGLETON;
	}

	@Override
	public boolean isDeviceSupported(MidiDevice.Info info) {
		return info instanceof MacMidiDeviceInfo;
	}

	@Override
	public MidiDevice getDevice(MidiDevice.Info info) {
		return SINGLETON.getDevice(info);
	}

	@Override
	public MidiDevice.Info[] getDeviceInfo() {
		return SINGLETON.getDeviceInfo();
	}

}
