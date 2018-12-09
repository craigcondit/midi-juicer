package org.randomcoder.midi.mac.pthread;

import com.sun.jna.Library;

public interface PThreadPeer extends Library {
  int pthread_main_np();
}
