package org.randomcoder.midi.experimental;

import org.randomcoder.midi.mac.coremidi.CoreMidi;

public class EnumerateMidiDevices {

    public static void main(String[] args) {
	CoreMidi midi = CoreMidi.getInstance();

	System.out.printf("CoreMIDI device count: %d%n", midi.getNumberOfDevices());
	System.out.printf("CoreMIDI source count: %d%n", midi.getNumberOfSources());
	System.out.printf("CoreMIDI destination count: %d%n", midi.getNumberOfDestinations());
	System.out.println();
	midi.getDevices();
    }

}
