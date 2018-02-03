package org.randomcoder.midi.mac.coremidi;

public class CoreMidiDevice {
    private final int id;

    public CoreMidiDevice(int id) {
	this.id = id;
    }

    public int getId() {
	return id;
    }
}
