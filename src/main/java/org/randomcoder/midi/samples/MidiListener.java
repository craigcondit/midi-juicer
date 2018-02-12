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

				MacMidi.addSetupChangedListener(() -> {
					LOG.info("MIDI setup changed");
				});
			}

			LOG.info("MacMidi setup complete");

			while (true) {
				Thread.sleep(1000L);
			}
		}

	}

}
