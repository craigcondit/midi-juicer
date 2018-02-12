package org.randomcoder.midi.mac.spi;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.randomcoder.midi.mac.coremidi.MIDIPacket;
import org.randomcoder.midi.mac.coremidi.MIDIPacketList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiMessageConverter {

	private static final Logger LOG = LoggerFactory.getLogger(MidiMessageConverter.class);

	public static List<MidiMessage> coreMidiToJava(MIDIPacketList pList) {

		// log packets
		if (LOG.isTraceEnabled()) {
			LOG.trace("Packet list: {}", pList);
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < pList.getLength(); i++) {
				MIDIPacket packet = pList.getPackets().get(i);
				LOG.trace("Packet: {}", packet);

				buf.setLength(0);
				for (short j = 0; j < packet.getLength(); j++) {
					buf.append(String.format("  %02x", packet.getData()[j]));
				}
				LOG.trace("  data (hex): {}", buf.toString());

				buf.setLength(0);
				for (short j = 0; j < packet.getLength(); j++) {
					buf.append(String.format(" %3d", packet.getData()[j] & 0xff));
				}
				LOG.trace("  data (dec): {}", buf.toString());
			}
		}

		// convert to Java MidiMessage
		List<MidiMessage> messages = new ArrayList<>(pList.getLength());

		for (int i = 0; i < pList.getLength(); i++) {
			MIDIPacket packet = pList.getPackets().get(i);
			MidiMessage message = null;
			try {
				if (packet.getLength() == 1) {
					message = new ShortMessage(
							packet.getData()[0] & 0xff);
				} else if (packet.getLength() == 2) {
					message = new ShortMessage(
							packet.getData()[0] & 0xff,
							packet.getData()[1] & 0xff,
							0);
				} else if (packet.getLength() == 3) {
					message = new ShortMessage(
							packet.getData()[0] & 0xff,
							packet.getData()[1] & 0xff,
							packet.getData()[2] & 0xff);
				}

				messages.add(message);

			} catch (InvalidMidiDataException e) {
				LOG.warn("Invalid MIDI packet received", e);
				continue;
			}
		}

		return messages;
	}

}
