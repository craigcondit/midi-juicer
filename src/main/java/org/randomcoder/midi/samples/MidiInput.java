package org.randomcoder.midi.samples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Transmitter;

import org.randomcoder.midi.MidiHandler;
import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.RunLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiInput {

	private static final Logger LOG = LoggerFactory.getLogger(MidiInput.class);

	public static void main(String[] args) throws Exception {

		try (RunLoop rl = RunLoop.spawn(true)) {
			if (MacMidi.available()) {
				MacMidi.init();

				MacMidi.addSetupChangedListener(e -> {
					LOG.info("MIDI setup changed: {}", e);
				});
			}

			LOG.info("MacMidi setup complete");

			List<MidiDevice.Info> deviceInfos = Arrays.stream(MidiSystem.getMidiDeviceInfo())
					.filter(MacMidi::isMacMidiDevice)
					.filter(di -> "MIDI1".equals(di.getName()))
					.filter(di -> "Nektar".equals(di.getVendor()))
					.filter(di -> "Impact GX49 MIDI1".equals(di.getDescription()))
					.collect(Collectors.toList());

			List<MidiDevice> devices = deviceInfos.stream()
					.map(di -> {
						try {
							return MidiSystem.getMidiDevice(di);
						} catch (Exception e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.filter(d -> d.getMaxTransmitters() != 0)
					.collect(Collectors.toList());

			if (devices.isEmpty()) {
				LOG.warn("No matching devices found");
				return;
			}

			try (MidiDevice device = devices.get(0); Transmitter transmitter = device.getTransmitter()) {
				LOG.info("Opened transmitter for device: {}", device);

				transmitter.setReceiver(MidiHandler.toReceiver((m, t) -> {
					LOG.debug("MIDI received: {}", m);
				}));

				while (true) {
					Thread.sleep(1000L);
				}
			}
		}
	}

}
