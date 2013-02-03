package com.github.sarxos.webcam;

import java.util.concurrent.atomic.AtomicInteger;


public class ConcurrentThreadsExample {

	private static AtomicInteger counter = new AtomicInteger(0);

	private static final class Capture extends Thread {

		private static final AtomicInteger number = new AtomicInteger(0);

		public Capture() {
			super("capture-" + number.incrementAndGet());
		}

		@Override
		public void run() {
			while (true) {
				Webcam.getDefault().getImage();
				int value = counter.incrementAndGet();
				if (value != 0 && value % 10 == 0) {
					System.out.println(Thread.currentThread().getName() + ": Frames captured: " + value);
				}
			}
		}
	}

	public static void main(String[] args) {

		/**
		 * This example will start several concurrent threads which use single
		 * webcam instance.
		 */

		int n = Runtime.getRuntime().availableProcessors() * 4;
		for (int i = 0; i < n; i++) {
			System.out.println("Thread: " + i);
			new Capture().start();
		}
	}
}
