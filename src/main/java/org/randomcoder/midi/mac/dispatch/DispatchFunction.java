package org.randomcoder.midi.mac.dispatch;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface DispatchFunction extends Callback {
	public void invoke(Pointer context);
}
