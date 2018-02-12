package org.randomcoder.midi.mac;

import java.io.Closeable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.randomcoder.midi.mac.corefoundation.CFRunLoopRunResult;
import org.randomcoder.midi.mac.corefoundation.CFStringRef;
import org.randomcoder.midi.mac.corefoundation.CoreFoundationPeer;
import org.randomcoder.midi.mac.corefoundation.CoreFoundationServiceFactory;
import org.randomcoder.midi.mac.coremidi.CoreMidiServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;

public class RunLoop implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(RunLoop.class);
	private static final String KCF_RUN_LOOP_DEFAULT_MODE = "kCFRunLoopDefaultMode";
	private static final AtomicLong TASK_ID_GENERATOR = new AtomicLong();
	private static final Map<Long, RunLoop> ACTIVE_RUNLOOPS = new HashMap<>();

	private static RunLoop DEFAULT_RUNLOOP;

	private final CoreFoundationPeer cf = CoreFoundationServiceFactory.getPeer();
	private final BlockingDeque<Runnable> deque = new LinkedBlockingDeque<Runnable>();
	private final long threadId;

	private volatile boolean running = false;
	private volatile boolean acceptingTasks = false;

	synchronized static void setDefault(RunLoop runloop) {
		Objects.requireNonNull(runloop);
		DEFAULT_RUNLOOP = runloop;
	}

	public synchronized static boolean available() {
		try {
			CoreFoundationServiceFactory.getPeer();
		} catch (Throwable t) {
			return false;
		}
		return true;
	}

	public synchronized static Optional<RunLoop> getDefault() {
		return Optional.ofNullable(DEFAULT_RUNLOOP);
	}

	public synchronized static boolean executingOnRunLoop() {
		return ACTIVE_RUNLOOPS.containsKey(Long.valueOf(Thread.currentThread().getId()));
	}

	private boolean executingOnThisRunLoop() {
		return Thread.currentThread().getId() == threadId;
	}

	public synchronized static boolean executingOnDefaultRunLoop() {
		RunLoop rl = ACTIVE_RUNLOOPS.get(Long.valueOf(Thread.currentThread().getId()));
		if (rl == null) {
			return false;
		}
		return rl == DEFAULT_RUNLOOP;
	}

	/**
	 * Begins executing a Apple CoreFoundation CFRunLoop event loop in a
	 * background thread.
	 * 
	 * <p>
	 * This ensures that at leat one Thread in an application sets up a proper
	 * CF runloop. The runloop will continue to execute until the
	 * {@link #shutdown()} method is called.
	 * </p>
	 * 
	 * @param setDefault
	 *            whether to set the returned runloop
	 * @return return value of background task
	 */
	public static RunLoop spawn(boolean setDefault) {
		RunLoopThread rlt = new RunLoopThread(setDefault);
		rlt.start();
		return rlt.getRunLoop();
	}

	private volatile RunLoopThread runLoopThread = null;

	public RunLoop(boolean setDefault) {
		this.threadId = Thread.currentThread().getId();

		if (setDefault) {
			synchronized (RunLoop.class) {
				if (getDefault().isPresent()) {
					throw new IllegalStateException("Another run loop is already the default");
				}
				setDefault(this);
			}
		}

		synchronized (RunLoop.class) {
			ACTIVE_RUNLOOPS.put(threadId, this);
		}

	}

	public void close() {
		if (runLoopThread != null) {
			runLoopThread.shutdown();
			runLoopThread = null;
		}

		synchronized (RunLoop.class) {
			ACTIVE_RUNLOOPS.remove(Long.valueOf(threadId));
			if (DEFAULT_RUNLOOP == this) {
				DEFAULT_RUNLOOP = null;
			}
		}
	}

	public void invokeLater(Runnable r) {
		deque.add(r);
	}

	public void invokeAndWait(Runnable r) {
		if (executingOnThisRunLoop()) {
			LOG.debug("Executing re-entrant task");
			try {
				r.run();
			} catch (Throwable t) {
				LOG.error("Error in runloop task", t);
			}
			return;
		}

		try {
			WaitingRunnable wr;
			synchronized (deque) {
				if (!acceptingTasks) {
					throw new IllegalArgumentException("Not accepting new tasks");
				}

				wr = new WaitingRunnable(r);
				deque.add(wr);
			}
			while (!wr.latch.await(10L, TimeUnit.MILLISECONDS)) {
				if (!running) {
					throw new IllegalStateException("RunLoop is stopped");
				}
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException("RunLoop interrupted");
		}
	}

	/**
	 * Begins executing a Apple CoreFoundation CFRunLoop event loop.
	 * 
	 * <p>
	 * This method is meant to be called from main() and ensures that the
	 * primary Thread in an application sets up a proper CF runloop. The
	 * provided task will be executed on a background thread. Once the task
	 * completes, this method will return, allowing for the application to
	 * be terminated gracefully.
	 * </p>
	 * 
	 * @param task
	 *            task to execute in the background
	 * @return return value of background task
	 */
	public <T> T enter(Callable<T> task) throws Exception {
		return enterInternal(task, new CountDownLatch(1));
	}

	public <T> T enterInternal(Callable<T> task, CountDownLatch initLatch) throws Exception {
		if (running) {
			throw new IllegalStateException("Only one Thread may call enter()");
		}
		LOG.debug("Entering runloop");
		running = true;
		try {

			ExecutorService executorService = executorService();

			try {
				Future<T> future = executorService.submit(new WrappedCallable<>(task, initLatch));

				runloop(future, initLatch);

				try {
					return future.get();
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					if (cause instanceof Exception) {
						throw (Exception) cause;
					} else if (cause instanceof Error) {
						throw (Error) cause;
					} else {
						throw new UndeclaredThrowableException(cause);
					}
				}
			} finally {
				executorService.shutdown();
			}
		} finally {
			LOG.debug("Leaving runloop");
			acceptingTasks = false;
			running = false;
		}
	}

	private CFStringRef resolveNativeProperty(String prop) {
		return new CFStringRef(CoreMidiServiceFactory.getNativeLibrary()
				.getGlobalVariableAddress(prop).getPointer(0));
	}

	private void runloop(Future<?> future, CountDownLatch initLatch) throws InterruptedException {

		Pointer currentRunLoop = null;
		boolean retainedCurrentRunLoop = false;

		CFStringRef defaultMode = null;
		boolean retainedDefaultMode = false;

		try {
			currentRunLoop = cf.CFRunLoopGetCurrent();
			cf.CFRetain(currentRunLoop);
			retainedCurrentRunLoop = true;

			defaultMode = resolveNativeProperty(KCF_RUN_LOOP_DEFAULT_MODE);
			cf.CFRetain(defaultMode.getPointer());
			retainedDefaultMode = true;

			long runCount = 0L;
			long stoppedCount = 0L;
			long finishedCount = 0L;
			long timedOutCount = 0L;
			long otherCount = 0L;

			acceptingTasks = true;
			initLatch.countDown();

			long startTime = System.currentTimeMillis();
			while (!future.isDone()) {

				executeTasks();

				int runLoopResult = cf.CFRunLoopRunInMode(defaultMode, 0.10d, false);
				switch (CFRunLoopRunResult.of(runLoopResult)) {
				case CFRunLoopRunFinished:
					finishedCount++;
					Thread.sleep(10L); // no tasks, sleep
					break;
				case CFRunLoopRunStopped:
					stoppedCount++;
					break;
				case CFRunLoopRunTimedOut:
					timedOutCount++;
					break;
				default:
					otherCount++;
					Thread.sleep(10L); // be safe
					break;

				}
				runCount++;

				long elapsed = System.currentTimeMillis() - startTime;
				if (elapsed > 1000) {
					LOG.debug("Called CFRunLoopRunInMode {} times: {} finished, {} stopped, {} timed out, {} other",
							runCount, finishedCount, stoppedCount, timedOutCount, otherCount);
					startTime = System.currentTimeMillis();
				}

				// execute pending tasks
				// run CFRunLoop
				// check for task exiting
			}
		} finally {
			acceptingTasks = false;
			executeTasks();

			if (retainedDefaultMode) {
				cf.CFRelease(defaultMode.getPointer());
			}
			if (retainedCurrentRunLoop) {
				cf.CFRelease(currentRunLoop);
			}
		}
	}

	private void executeTasks() {
		while (true) {
			Runnable task = null;
			synchronized (deque) {
				task = deque.poll();
			}
			if (task == null) {
				return;
			}
			try {
				task.run();
			} catch (Throwable t) {
				LOG.error("Error in runloop task", t);
			} finally {
				if (task instanceof WaitingRunnable) {
					((WaitingRunnable) task).latch.countDown();
				}
			}
		}
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

	private static class WrappedCallable<T> implements Callable<T> {
		private final Callable<T> target;
		private final CountDownLatch initLatch;

		private WrappedCallable(Callable<T> target, CountDownLatch initLatch) {
			this.target = target;
			this.initLatch = initLatch;
		}

		@Override
		public T call() throws Exception {
			initLatch.await();
			return target.call();
		}
	}

	private ExecutorService executorService() {
		return Executors.newFixedThreadPool(1, r -> {
			Thread t = new Thread(Thread.currentThread().getThreadGroup(), r);
			t.setName(String.format("runloop-task-%d", TASK_ID_GENERATOR.incrementAndGet()));
			t.setDaemon(false);
			t.setPriority(Thread.NORM_PRIORITY);
			return t;
		});
	}

	private static class RunLoopThread extends Thread {
		private final boolean setDefault;
		private final CountDownLatch initLatch = new CountDownLatch(2);
		private final CountDownLatch shutdownLatch = new CountDownLatch(1);
		private volatile RunLoop runloop;
		private volatile RuntimeException exception;

		private RunLoopThread(boolean setDefault) {
			super("runloop");
			this.setDefault = setDefault;
		}

		@Override
		public void run() {
			try {
				this.runloop = new RunLoop(setDefault);
				runloop.runLoopThread = this;
			} catch (RuntimeException e) {
				this.exception = e;
				initLatch.countDown();
				initLatch.countDown();
				return;
			}

			initLatch.countDown();
			try {
				runloop.enterInternal(() -> {
					shutdownLatch.await();
					return null;
				}, initLatch);
			} catch (Exception e) {
				LOG.error("Exception in runloop thread", e);
				initLatch.countDown();
				shutdownLatch.countDown();
			}
		}

		private RunLoop getRunLoop() {
			try {
				initLatch.await();
			} catch (InterruptedException e) {
				throw new IllegalStateException("Interrupted", e);
			}
			if (exception != null) {
				throw exception;
			}
			return runloop;
		}

		private void shutdown() {
			shutdownLatch.countDown();
			try {
				join();
			} catch (InterruptedException e) {
				LOG.info("Interrupted", e);
			}
		}
	}

}
