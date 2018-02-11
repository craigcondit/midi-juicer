package org.randomcoder.midi.samples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Transmitter;
import javax.sound.midi.spi.MidiDeviceProvider;

import org.randomcoder.midi.MidiHandler;
import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.spi.MacMidiDeviceProvider;

public class MidiInputDirect {

	public static void main(String[] args) throws Exception {
		if (MacMidi.isAvailable()) {
			MacMidi.setDebug(true);
			MacMidi.init();

			MacMidi.addSetupChangedListener(() -> {
				System.out.println("setup changed");
			});
		}

		System.out.println("MIDI initialized");

		try {
			MidiDeviceProvider p = MacMidiDeviceProvider.getInstance();

			List<MidiDevice.Info> deviceInfos = Arrays.stream(p.getDeviceInfo())
					.filter(MacMidi::isMacMidiDevice)
					.filter(di -> "MIDI1".equals(di.getName()))
					.filter(di -> "Nektar".equals(di.getVendor()))
					.filter(di -> "Impact GX49 MIDI1".equals(di.getDescription()))
					.collect(Collectors.toList());

			List<MidiDevice> devices = deviceInfos.stream()
					.map(di -> {
						try {
							return p.getDevice(di);
						} catch (Exception e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.filter(d -> d.getMaxTransmitters() != 0)
					.collect(Collectors.toList());

			if (devices.isEmpty()) {
				System.out.println("No matching devices found");
				return;
			}

			try (MidiDevice device = devices.get(0); Transmitter transmitter = device.getTransmitter()) {
				System.out.printf("Opened transmitter for device: %s%n", device);

				transmitter.setReceiver(MidiHandler.toReceiver((m, t) -> {
					System.out.println(m);
				}));

				while (true) {
					Thread.sleep(1000L);
				}
			}

		} finally {
			MacMidi.shutdown();
		}
	}

}
