package org.randomcoder.midi.mac.spi;

import java.util.Collections;
import java.util.List;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class MacMidiSource extends AbstractMacMidiDevice {

	public MacMidiSource(MacMidiDeviceInfo info) {
		super(info);
	}

	@Override
	public int getMaxReceivers() {
		return 0;
	}

	@Override
	public Receiver getReceiver() throws MidiUnavailableException {
		throw new MidiUnavailableException();
	}

	@Override
	public List<Receiver> getReceivers() {
		return Collections.emptyList();
	}

	@Override
	public int getMaxTransmitters() {
		return -1;
	}

	@Override
	public Transmitter getTransmitter() throws MidiUnavailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Transmitter> getTransmitters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOpen() {
		// TODO handle this
		return false;
	}

	@Override
	public void open() throws MidiUnavailableException {
		if (isOpen()) {
			return;
		}

		// TODO Auto-generated method stub
	}

	@Override
	public void close() {
		if (!isOpen()) {
			return;
		}

		// TODO Auto-generated method stub
	}

}
