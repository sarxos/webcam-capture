package com.github.sarxos.webcam.ds.buildin;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class WebcamGrabberProcessor implements Runnable, ThreadFactory {

	private static WebcamGrabberProcessor instance = null;

	private final SynchronousQueue<WebcamGrabberTask> tasks = new SynchronousQueue<WebcamGrabberTask>(true);
	private final Executor runner = Executors.newSingleThreadExecutor(this);

	private OpenIMAJGrabber grabber = null;

	private WebcamGrabberProcessor() {
		runner.execute(this);
	}

	public static WebcamGrabberProcessor getInstance() {
		if (instance == null) {
			instance = new WebcamGrabberProcessor();
		}
		return instance;
	}

	protected void process(WebcamGrabberTask task) {
		try {
			synchronized (task) {
				tasks.offer(task, 30, TimeUnit.MINUTES);
				task.wait();
			}
		} catch (InterruptedException e) {
			throw new WebcamException("Offer interrupted", e);
		}
	}

	@Override
	public void run() {
		initialize();
		while (true) {
			WebcamGrabberTask task = null;
			try {
				(task = tasks.take()).handle(grabber);
			} catch (InterruptedException e) {
				throw new WebcamException("Take interrupted", e);
			} finally {
				synchronized (task) {
					task.notifyAll();
				}
			}
		}
	}

	private void initialize() {
		grabber = new OpenIMAJGrabber();
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, getClass().getSimpleName());
		t.setDaemon(true);
		return t;
	}
}
