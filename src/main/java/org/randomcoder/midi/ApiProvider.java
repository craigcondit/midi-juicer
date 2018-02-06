package org.randomcoder.midi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.spi.MacRunLoopThread;

public class ApiProvider {

	public static void main(String[] args) throws Exception {

		// wait for device to be present
		MacRunLoopThread rlt = CoreMidi.getInstance().createRunLoopThread("test");
		rlt.start();
		CoreMidi.getInstance().createClient("client", rlt, e -> {
			System.out.println(e);
		});

		List<MidiDevice.Info> deviceInfos = Arrays.stream(MidiSystem.getMidiDeviceInfo())
				.filter(MacMidi::isMacMidiDevice)
				.filter(di -> "Out".equals(di.getName()))
				.filter(di -> "E-MU Systems,Inc.".equals(di.getVendor()))
				.filter(di -> "E-MU XMidi1X1 Tab Out".equals(di.getDescription()))
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
				.filter(d -> d.getMaxReceivers() != 0)
				.collect(Collectors.toList());

		if (devices.isEmpty()) {
			System.out.println("No matching devices found");
			return;
		}

		MidiDevice device = devices.get(0);
		Receiver receiver = device.getReceiver();

		int minNote = 60;
		int maxNote = 72;
		int delayMs = 500;

		// send sysex
		byte[] sysexData;
		try (InputStream in = ApiProvider.class.getResourceAsStream("/dx7_patch.sysex")) {
			try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
				in.transferTo(bos);
				bos.flush();
				sysexData = bos.toByteArray();
			}
		}
		receiver.send(new SysexMessage(sysexData, sysexData.length), device.getMicrosecondPosition());

		// sleep for 1 second
		Thread.sleep(1000L);

		for (int i = minNote; i <= maxNote; i++) {
			receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, i, 127), device.getMicrosecondPosition());
			receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, i + 4, 127), device.getMicrosecondPosition());
			receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, i + 7, 127), device.getMicrosecondPosition());

			Thread.sleep(delayMs);

			receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, i, 0), device.getMicrosecondPosition());
			receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, i + 4, 0), device.getMicrosecondPosition());
			receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, i + 7, 0), device.getMicrosecondPosition());
		}

		receiver.close();
		device.close();
	}

}
