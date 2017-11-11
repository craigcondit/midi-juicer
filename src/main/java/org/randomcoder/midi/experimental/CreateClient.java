package org.randomcoder.midi.experimental;

import org.randomcoder.midi.coremidi.CoreMidi;

public class CreateClient {

	public static void main(String[] args) {
		CoreMidi midi = CoreMidi.getInstance();

		midi.createClient();
	}

}
