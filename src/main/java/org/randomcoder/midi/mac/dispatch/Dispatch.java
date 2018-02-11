package org.randomcoder.midi.mac.dispatch;

import org.randomcoder.midi.mac.corefoundation.CFRunLoop;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

public class Dispatch {
	private static Dispatch INSTANCE = new Dispatch();

	public static Dispatch getInstance() {
		return INSTANCE;
	}

	static void setInstance(Dispatch instance) {
		INSTANCE = instance;
	}

	Dispatch() {
	}

	DispatchPeer peer() {
		return DispatchServiceFactory.getPeer();
	}

	NativeLibrary nl() {
		return DispatchServiceFactory.getNativeLibrary();
	}

	public Pointer getMainQueue() {
		return nl().getGlobalVariableAddress("_dispatch_main_q");
	}

	public void dispatchAsyncFunction(Pointer queue, Pointer context, DispatchFunction work) {
		peer().dispatch_async_f(queue, context, work);
	}

	public void dispatchSyncFunction(Pointer queue, Pointer context, DispatchFunction work) {
		peer().dispatch_async_f(queue, context, work);
	}

	public void runOnMainThread(Pointer context, DispatchFunction work) {
		if (context == null) {
			context = getMainQueue();
		}

		final Pointer fContext = context;

		CFRunLoop.init();
		try {
			CFRunLoop.invokeAndWait(() -> {
				work.invoke(fContext);
			});
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted");
		}

		// if (PThread.getInstance().isMainThread()) {
		// MacMidi.debug("Running task on current main thread");
		// work.invoke(context);
		// } else {
		// MacMidi.debug("Submitting task to main thread");
		// dispatchSyncFunction(getMainQueue(), context, work);
		// }

	}
}
