package org.randomcoder.midi.samples;

import org.randomcoder.midi.mac.MacMidi;
import org.randomcoder.midi.mac.RunLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiListener {
	private static final Logger LOG = LoggerFactory.getLogger(MidiListener.class);

	public static void main(String[] args) throws Exception {

		try (RunLoop rl = RunLoop.spawn(true)) {

			if (MacMidi.available()) {
				MacMidi.init();

				MacMidi.addSetupChangedListener(e -> {
					LOG.info("MIDI setup changed: {}", e);
				});
				MacMidi.addObjectAddedListener(e -> {
					LOG.info("MIDI object added: {}", e);
				});
				MacMidi.addObjectRemovedListener(e -> {
					LOG.info("MIDI object removed: {}", e);
				});
				MacMidi.addPropertyChangedListener(e -> {
					LOG.info("MIDI property changed: {}", e);
				});
				MacMidi.addIOErrorListener(e -> {
					LOG.info("MIDI I/O error: {}", e);
				});
				MacMidi.addThruConnectionsChangedListener(e -> {
					LOG.info("MIDI thru connectoins changed: {}", e);
				});
				MacMidi.addSerialPortOwnerChangedListener(e -> {
					LOG.info("MIDI serial port owner changed: {}", e);
				});
			}

			LOG.info("MacMidi setup complete");

			while (true) {
				Thread.sleep(1000L);
			}
		}

	}

}
