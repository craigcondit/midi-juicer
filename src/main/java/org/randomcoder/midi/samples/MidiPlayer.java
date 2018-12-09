package org.randomcoder.midi.samples;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;

public class MidiPlayer {

	public static void main(String[] args) throws Exception {

		var deviceInfos = Arrays.stream(MidiSystem.getMidiDeviceInfo())
				.filter(di -> di.getDescription().toLowerCase(Locale.US).contains("usb"))
				.collect(Collectors.toList());

		var devices = deviceInfos.stream()
				.map(di -> {
					try {
						return MidiSystem.getMidiDevice(di);
					} catch (Exception e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.filter(d -> d.getMaxReceivers() != 0)
				.collect(Collectors.toList());

		if (devices.isEmpty()) {
			System.err.println("No matching devices found");
			System.exit(1);
		}

		if (args.length < 2) {
			System.err.printf("Usage: %s <midi-file> <volume>%n", MidiPlayer.class.getName());
			System.exit(1);
		}

		try (MidiDevice device = devices.get(0); Receiver rx = device.getReceiver()) {
			device.open();
			Thread hook = new Thread(() -> reset(rx));
			Runtime.getRuntime().addShutdownHook(hook);
			try (Sequencer seq = MidiSystem.getSequencer(false); Transmitter tx = seq.getTransmitter()) {
				seq.open();
				try (InputStream in = new BufferedInputStream(new FileInputStream(new File(args[0])))) {
					tx.setReceiver(new VolumeFilter(rx, Float.parseFloat(args[1])));
					seq.setSequence(in);

					seq.start();
					System.out.println("Started");
					while (seq.isRunning()) {
						Thread.sleep(10L);
					}
					seq.stop();
					System.out.println("Done");
				}
			} finally {
				reset(rx);
				Runtime.getRuntime().removeShutdownHook(hook);
			}
		}
	}

	private static void reset(Receiver rx) {
		try {
			System.out.println("ALL NOTES OFF");
			rx.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 123, 0), -1L); // all
																					// notes
																					// off
			System.out.println("DAMPER OFF");
			rx.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 64, 0), -1L); // damper
																					// off
			System.out.println("PORTAMENTO OFF");
			rx.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 65, 0), -1L); // portamento
																					// off
			System.out.println("SUSTENUTO OFF");
			rx.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 66, 0), -1L); // sustenuto
																					// off
		} catch (InvalidMidiDataException e) {
			throw new RuntimeException(e);
		}
	}

	private static class VolumeFilter implements Receiver {
		private final Receiver target;
		private final double volume;

		public VolumeFilter(Receiver target, double volume) {
			this.target = target;
			this.volume = volume;
		}

		@Override
		public void send(MidiMessage message, long timeStamp) {
			try {
				if (message instanceof SysexMessage) {
					return;
				}

				if (message instanceof ShortMessage) {
					ShortMessage sm = ((ShortMessage) message);
					int data1 = sm.getData1();
					int data2 = sm.getData2();
					switch (sm.getCommand()) {
					case ShortMessage.NOTE_ON:
						data2 = Math.max(0, Math.min(127, (int) (data2 * volume)));
						System.out.printf("NOTE ON: %d %d%n", data1, data2);
						break;
					case ShortMessage.NOTE_OFF:
						data2 = Math.max(0, Math.min(127, (int) (data2 * volume)));
						System.out.printf("NOTE OFF: %d %d%n", data1, data2);
						break;
					}
					sm.setMessage(sm.getCommand(), 0, data1, data2);

					target.send(sm, timeStamp);
				}
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void close() {
			target.close();
		}

	}
}
