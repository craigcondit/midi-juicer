package org.randomcoder.midi.mac.dispatch;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface DispatchPeer extends Library {
	void dispatch_async_f(Pointer queue, Pointer context, DispatchFunction work);

	void dispatch_sync_f(Pointer queue, Pointer context, DispatchFunction work);
}
