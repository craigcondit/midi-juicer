package org.randomcoder.midi.mac.corefoundation;

abstract public class CFRunLoop {
	private CFRunLoop() {
	}

	private static volatile boolean init = false;
	private static volatile boolean shutdown = false;

	private static CFRunLoopThread runloop() {
		init = true;
		return CFRunLoopThreadHolder.instance;
	}

	private static class CFRunLoopThreadHolder {
		private static final CFRunLoopThread instance = new CFRunLoopThread(
				CoreFoundationServiceFactory.getPeer(), CFRunLoop::exceptionHandler);
		static {
			instance.start();
		}
	}

	private static void exceptionHandler(Throwable t) {
		t.printStackTrace();
	}

	public static void invokeLater(final Runnable r) {
		if (shutdown) {
			throw new IllegalStateException("CFRunLoop shutdown");
		}

		runloop().invokeLater(r);
	}

	public static void invokeAndWait(final Runnable r) throws InterruptedException {
		if (shutdown) {
			throw new IllegalStateException("CFRunLoop shutdown");
		}

		runloop().invokeAndWait(r);
	}

	public static void init() {
		if (shutdown) {
			return;
		}
		if (!init) {
			runloop();
		}
	}

	public static void shutdown() {
		if (shutdown) {
			return;
		}

		shutdown = true;
		try {
			runloop().close();
			init = false;
		} catch (InterruptedException ignored) {
		}
	}

}
