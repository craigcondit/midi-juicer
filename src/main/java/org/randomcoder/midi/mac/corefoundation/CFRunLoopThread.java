package org.randomcoder.midi.mac.corefoundation;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.sun.jna.Pointer;

public class CFRunLoopThread extends Thread implements AutoCloseable {

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

	@Override
	public void run() {
		final Pointer currentRunLoop = cf.CFRunLoopGetCurrent();
		cf.CFRunLoopRun();
		cf.CFRetain(currentRunLoop);

		init.countDown();

		try {
			while (!shutdown) {
				while (!deque.isEmpty() && !shutdown) {
					Runnable r = deque.takeFirst();
					try {
						System.out.printf("Running task %s%n", r);
						r.run();
						System.out.printf("Finished task %s%n", r);
					} catch (Throwable t) {
						exceptionHandler.accept(t);
					} finally {
						if (r instanceof WaitingRunnable) {
							((WaitingRunnable) r).latch.countDown();
						}
					}
				}
				cf.CFRunLoopRun();
				Thread.sleep(10L); // TODO figure out if there's a better way
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
