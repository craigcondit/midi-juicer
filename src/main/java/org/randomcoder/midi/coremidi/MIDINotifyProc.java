package org.randomcoder.midi.coremidi;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface MIDINotifyProc extends Callback {
    public void invoke(Pointer message, Pointer refCon);
}