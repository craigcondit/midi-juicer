package org.randomcoder.midi.mac.corefoundation;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface CFRunLoopSourcePerform extends Callback {
	public void invoke(Pointer context);
}