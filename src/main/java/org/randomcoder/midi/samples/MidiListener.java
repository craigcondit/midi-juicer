package org.randomcoder.midi.samples;

import org.randomcoder.midi.mac.MacMidi;

public class MidiListener {

	public static void main(String[] args) throws Exception {

		if (MacMidi.isAvailable()) {
			MacMidi.setDebug(true);
			MacMidi.init();

			MacMidi.addSetupChangedListener(() -> {
				System.out.println("setup changed");
			});
		}

		System.out.println("Ready");

		try {
			while (true) {
				Thread.sleep(1000L);
			}
		} finally {
			MacMidi.shutdown();
		}

	}

}
