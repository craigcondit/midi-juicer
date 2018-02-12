package org.randomcoder.midi.mac.spi;

import java.util.concurrent.atomic.AtomicReference;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.MIDIPacket;
import org.randomcoder.midi.mac.coremidi.MIDIPacketList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;

public class MacMidiSourceTransmitter implements MidiDeviceTransmitter {

	private static final Logger LOG = LoggerFactory.getLogger(MacMidiSourceTransmitter.class);

	private final int id;
	private final MacMidiSource source;
	private final CoreMidi midi;
	private final AtomicReference<Receiver> receiverHolder = new AtomicReference<>(null);

	private volatile Integer clientId;
	private volatile Integer inputPortId;
	private volatile Pointer connRef;
	private volatile boolean open = false;

	public MacMidiSourceTransmitter(MacMidiSource source, int id) {
		this.id = id;
		this.source = source;
		this.midi = CoreMidi.getInstance();
	}

	synchronized void open() throws MidiUnavailableException {
		if (open) {
			return;
		}

		String clientName = String.format("MacMidiSourceTransmitter:%d:%d",
				source.getDeviceInfo().getUniqueId(), id);

		String inputPortName = String.format("MacMidiInputPort:%d:%d",
				source.getDeviceInfo().getUniqueId(), id);

		source.open();

		clientId = midi.createClient(clientName, (m, t) -> {
		});
		inputPortId = midi.createInputPort(inputPortName, clientId, this::handleMidi);
		connRef = midi.connectSource(inputPortId, source.getDeviceRef());

		open = true;
	}

	private void handleMidi(Pointer pktlist, Pointer readProcRefCon, Pointer srcConnRefCon) {
		Receiver receiver = receiverHolder.get();

		// short-circuit out if receiver is not set or transmitter inactive
		if (receiver == null || !open) {
			return;
		}

		LOG.debug("Got MIDI packet list");

		// go through packets
		MIDIPacketList pList = new MIDIPacketList(pktlist, 0);

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

				receiver.send(message, -1L);

			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	@Override
	public void close() {
		receiverHolder.set(null);

		if (connRef != null) {
			if (inputPortId != null) {
				midi.disconnectSource(inputPortId, source.getDeviceRef(), connRef);
			}
			connRef = null;
		}
		if (inputPortId != null) {
			midi.closeInputPort(inputPortId);
			inputPortId = null;
		}

		if (clientId != null) {
			midi.closeClient(clientId);
			clientId = null;
		}

		open = false;
	}

	@Override
	public Receiver getReceiver() {
		return receiverHolder.get();
	}

	@Override
	public void setReceiver(Receiver receiver) {
		receiverHolder.set(receiver);
	}

	@Override
	public MacMidiSource getMidiDevice() {
		return source;
	}

}
