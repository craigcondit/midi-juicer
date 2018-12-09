package org.randomcoder.midi.mac.coremidi;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface MIDIReadProc extends Callback {
  public void invoke(Pointer pktlist, Pointer readProcRefCon,
      Pointer srcConnRefCon);
}