package org.randomcoder.midi.mac.coremidi;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface MIDICompletionProc extends Callback {
  public void invoke(Pointer request);
}