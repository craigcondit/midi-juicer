package org.randomcoder.midi.samples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import org.randomcoder.midi.MidiHandler;
import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.RunLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiVirtualDestination {

	private static final Logger LOG = LoggerFactory.getLogger(MidiVirtualDestination.class);

	public static void main(String[] args) throws Exception {

		try (RunLoop rl = RunLoop.spawn(true)) {
			if (!MacMidi.available()) {
				LOG.error("MacMidi required");
				return;
			}

			MacMidi.init();

			MacMidi.addSetupChangedListener(e -> {
				LOG.info("MIDI setup changed: {}", e);
				rl.invokeAndWait(() -> dumpDevices(MidiSystem.getMidiDeviceInfo()));
			});

			LOG.info("MacMidi setup complete");

			try (MidiDevice device = MacMidi.createVirtualDestination("Output 1", "RandomCoder", "RandomCoder Output 1",
					"1.0.0");
					Transmitter transmitter = device.getTransmitter()) {

				LOG.info("Opened transmitter for device: {}", device);

				transmitter.setReceiver(MidiHandler.toReceiver((m, t) -> {
					LOG.debug("MIDI received: {}", m);
				}));

				rl.invokeAndWait(() -> dumpDevices(MidiSystem.getMidiDeviceInfo()));

				Thread.sleep(5000L);
				
				getOutput(MidiSystem.getMidiDeviceInfo()).ifPresent(out -> {
					try (Receiver r = out.getReceiver()) {
						r.send(new ShortMessage(ShortMessage.NOTE_ON, 127, 0), -1L);
						r.send(new ShortMessage(ShortMessage.NOTE_OFF, 127, 0), -1L);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				while (true) {
					Thread.sleep(1000L);
				}
			}
		}
	}

	private static Optional<MidiDevice> getOutput(MidiDevice.Info[] infos) {
		List<MidiDevice.Info> deviceInfos = Arrays.stream(MidiSystem.getMidiDeviceInfo())
				.filter(MacMidi::isMacMidiDevice)
				.filter(di -> "RandomCoder Output 1".equals(di.getName()))
				.filter(di -> "RandomCoder Output 1".equals(di.getDescription()))
				.collect(Collectors.toList());

		return deviceInfos.stream()
				.map(di -> {
					try {
						return MidiSystem.getMidiDevice(di);
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.filter(d -> d.getMaxReceivers() != 0)
				.findFirst();
	}

	private static void dumpDevices(MidiDevice.Info[] infos) {
		for (MidiDevice.Info info : infos) {
			LOG.info("Found MIDI device info {}: {}", info, info.getClass().getName());

			try {
				MidiDevice dev = MidiSystem.getMidiDevice(info);
				LOG.info("  Device: {}", dev);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
}
