package org.randomcoder.midi.experimental;

import org.randomcoder.midi.mac.coremidi.CoreMidi;

public class CreateClient {

    public static void main(String[] args) {
	CoreMidi midi = CoreMidi.getInstance();

	midi.createClient();
    }

}
