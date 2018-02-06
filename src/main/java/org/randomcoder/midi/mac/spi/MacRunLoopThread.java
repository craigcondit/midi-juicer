package org.randomcoder.midi.mac.spi;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.randomcoder.midi.mac.corefoundation.CoreFoundationPeer;

import com.sun.jna.Pointer;

public class MacRunLoopThread extends Thread implements AutoCloseable {

	private final CoreFoundationPeer cf;
	private volatile boolean shutdown = false;

	private final LinkedList<Task<?>> tasks = new LinkedList<>();

	public MacRunLoopThread(String name, CoreFoundationPeer cf) {
		super(name);
		super.setDaemon(true);
		this.cf = cf;
	}

	private Task<?> takeTask() {
		synchronized (tasks) {
			return tasks.isEmpty() ? null : tasks.removeFirst();
		}
	}

	private void putTask(Task<?> task) {
		synchronized (tasks) {
			tasks.add(task);
		}
	}

	@Override
	public void run() {
		Pointer currentRunLoop = cf.CFRunLoopGetCurrent();
		cf.CFRetain(currentRunLoop);

		try {
			while (!shutdown) {
				try {
					synchronized (this) {
						this.wait(100L);
					}
					Task<?> task = null;
					while ((task = takeTask()) != null) {
						try {
							task.result = task.block.call();
						} catch (Exception e) {
							task.exception = e;
						} finally {
							task.latch.countDown();
						}
					}
				} catch (InterruptedException e) {
					return;
				}
				cf.CFRunLoopRun();
			}
		} finally {
			cf.CFRunLoopStop(currentRunLoop);
			cf.CFRelease(currentRunLoop);

			Task<?> task = null;
			while ((task = takeTask()) != null) {
				task.latch.countDown();
				task.exception = new InterruptedException();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T execute(Callable<T> callable) throws Exception {
		Task<T> task = new Task<>(callable);
		putTask(task);

		synchronized (this) {
			this.notifyAll();
		}

		try {
			task.latch.await();
		} catch (InterruptedException e) {
			throw e;
		}
		if (task.exception != null) {
			throw task.exception;
		}
		return (T) task.result;
	}

	public void execute(Runnable runnable) throws Exception {
		execute(() -> {
			runnable.run();
			return null;
		});
	}

	@Override
	public void close() throws InterruptedException {
		shutdown = true;
		synchronized (this) {
			this.notifyAll();
		}
		join();
	}

	private static class Task<T> {
		final Callable<T> block;
		final CountDownLatch latch;
		volatile Object result;
		volatile Exception exception;

		public Task(Callable<T> block) {
			this.block = block;
			latch = new CountDownLatch(1);
		}
	}
}
