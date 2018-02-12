package org.randomcoder.midi.mac.corefoundation;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.randomcoder.midi.mac.coremidi.CoreMidiServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;

public class CFRunLoopThread extends Thread implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(CFRunLoopThread.class);

	private static final String KCF_RUN_LOOP_COMMON_MODES = "kCFRunLoopCommonModes";
	private static final String KCF_RUN_LOOP_DEFAULT_MODE = "kCFRunLoopDefaultMode";

	private final BlockingDeque<Runnable> deque = new LinkedBlockingDeque<Runnable>();

	private final CoreFoundationPeer cf;
	private final Consumer<Throwable> exceptionHandler;
	private final CountDownLatch init = new CountDownLatch(1);
	private volatile boolean shutdown = false;

	CFRunLoopThread(CoreFoundationPeer cf, Consumer<Throwable> exceptionHandler) {
		super("CFRunLoop");
		setDaemon(true);
		this.cf = cf;
		this.exceptionHandler = exceptionHandler;
	}

	private CFStringRef resolve(String prop) {
		return new CFStringRef(CoreMidiServiceFactory.getNativeLibrary()
				.getGlobalVariableAddress(prop).getPointer(0));
	}

	@Override
	public void run() {
		final Pointer currentRunLoop = cf.CFRunLoopGetCurrent();

		LOG.debug("Creating context");
		CFRunLoopSourceContext context = new CFRunLoopSourceContext(currentRunLoop, c -> {
			LOG.debug("CFRunLoopSource called");
		});

		cf.CFRetain(currentRunLoop);

		CFStringRef commonModes = resolve(KCF_RUN_LOOP_COMMON_MODES);
		CFStringRef defaultMode = resolve(KCF_RUN_LOOP_DEFAULT_MODE);

		LOG.debug("Creating source");
		final Pointer source = cf.CFRunLoopSourceCreate(null, 0, context);

		LOG.debug("Adding source");
		cf.CFRunLoopAddSource(currentRunLoop, source, commonModes);

		LOG.debug("Signalling source");
		cf.CFRunLoopSourceSignal(source);

		LOG.debug("Running run loop");
		cf.CFRunLoopRunInMode(defaultMode, 0.10d, false);

		LOG.debug("Removing source");
		cf.CFRunLoopRemoveSource(currentRunLoop, source, commonModes);

		LOG.debug("Init complete");
		init.countDown();

		try {
			while (!shutdown) {
				while (!deque.isEmpty() && !shutdown) {
					Runnable r = deque.takeFirst();
					try {
						LOG.debug("Running task {}", r);
						r.run();
						LOG.debug("Finished task {}", r);
					} catch (Throwable t) {
						exceptionHandler.accept(t);
					} finally {
						if (r instanceof WaitingRunnable) {
							((WaitingRunnable) r).latch.countDown();
						}
					}
				}
				cf.CFRunLoopRunInMode(defaultMode, 0.10d, false);
			}
		} catch (InterruptedException e) {
			return;
		} finally {
			cf.CFRelease(currentRunLoop);
		}
	}

	public void invokeLater(Runnable r) {
		deque.add(r);
	}

	public void invokeAndWait(Runnable r) throws InterruptedException {
		WaitingRunnable wr = new WaitingRunnable(r);
		deque.add(wr);
		while (!wr.latch.await(10L, TimeUnit.MILLISECONDS)) {
			if (shutdown) {
				throw new InterruptedException("CFRunLoop shutting down");
			}
		}
	}

	@Override
	public void close() throws InterruptedException {
		synchronized (this) {
			shutdown = true;
			interrupt();
		}
		join();
		deque.clear();
	}

	private static class WaitingRunnable implements Runnable {
		private final Runnable target;
		private final CountDownLatch latch = new CountDownLatch(1);

		private WaitingRunnable(Runnable target) {
			this.target = target;
		}

		@Override
		public void run() {
			target.run();
		}
	}
}
