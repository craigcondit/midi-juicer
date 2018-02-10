package org.randomcoder.midi.mac.spi;

import java.util.concurrent.atomic.AtomicReference;

import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import org.randomcoder.midi.mac.coremidi.CoreMidi;
import org.randomcoder.midi.mac.coremidi.MIDIPacket;
import org.randomcoder.midi.mac.coremidi.MIDIPacketList;

import com.sun.jna.Pointer;

public class MacMidiSourceTransmitter implements MidiDeviceTransmitter {

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

		String clientName = String.format("receiver-%d-%d",
				source.getDeviceInfo().getUniqueId(), id);

		String outputPortName = String.format("output-%d-%d",
				source.getDeviceInfo().getUniqueId(), id);

		source.open();

		clientId = midi.createClient(clientName);
		inputPortId = midi.createInputPort(outputPortName, clientId, this::handleMidi);
		connRef = midi.connectSource(inputPortId, source.getDeviceRef());

		open = true;
	}

	private void handleMidi(Pointer pktlist, Pointer readProcRefCon, Pointer srcConnRefCon) {
		Receiver receiver = receiverHolder.get();

		// short-circuit out if receiver is not set or transmitter inactive
		if (receiver == null || !open) {
			return;
		}

		System.out.printf("MIDIReadProc pktlist: %s srcConnRefCon: %s%n", pktlist, srcConnRefCon);

		// go through packets
		MIDIPacketList pList = new MIDIPacketList(pktlist, 0);
		System.out.printf("Packet list: %s%n", pList);

		for (int i = 0; i < pList.getLength(); i++) {
			MIDIPacket packet = pList.getPackets().get(i);
			System.out.printf("Packet: %s%n", packet);
			System.out.print("  data (hex):");
			for (short j = 0; j < packet.getLength(); j++) {
				System.out.printf("  %02x", packet.getData()[j]);
			}
			System.out.println();
			System.out.print("  data (dec):");
			for (int j = 0; j < packet.getLength(); j++) {
				System.out.printf(" %3d", packet.getData()[j] & 0xff);
			}
			System.out.println();
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
