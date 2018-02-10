package org.randomcoder.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

@FunctionalInterface
public interface MidiHandler {

	public void send(MidiMessage message, long timestamp);

	public static Receiver toReceiver(final MidiHandler receiver) {
		return new Receiver() {
			@Override
			public void send(final MidiMessage message, final long timestamp) {
				receiver.send(message, timestamp);
			}

			@Override
			public void close() {
			}
		};
	}
}
