package org.randomcoder.midi.samples;

import org.randomcoder.midi.mac.MacMidi;

public class MidiListener {

	public static void main(String[] args) throws Exception {

		if (MacMidi.isAvailable()) {
			MacMidi.init();

			MacMidi.addSetupChangedListener(e -> {
				System.out.println(e);
			});
		}

		try {
			Thread.sleep(60000L);
		} finally {
			MacMidi.shutdown();
		}
	}

}
