package com.github.sarxos.webcam.ds.raspistill;

class AppThreadGroup {
	private static final String THREAD_GROUP_NAME = "raspistill-driver";
	private static int threadCounter;
	private final static AppThreadGroup INSTANCE = new AppThreadGroup();
	private final ThreadGroup group;

	private AppThreadGroup() {
		group = new ThreadGroup(THREAD_GROUP_NAME);
	}

	public static ThreadGroup threadGroup() {
		return INSTANCE.group;
	}

	public static int threadId() {
		return threadCounter++;
	}
}