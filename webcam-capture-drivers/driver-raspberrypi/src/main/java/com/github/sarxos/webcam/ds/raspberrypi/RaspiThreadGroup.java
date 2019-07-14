package com.github.sarxos.webcam.ds.raspberrypi;

/**
 * app thread group
 * 
 * @author maoanapex88@163.com (alexmao86)
 *
 */
class RaspiThreadGroup {
	private static final String THREAD_GROUP_NAME = "raspistill-driver";
	private static int threadCounter;
	private final static RaspiThreadGroup INSTANCE = new RaspiThreadGroup();
	private final ThreadGroup group;

	private RaspiThreadGroup() {
		group = new ThreadGroup(THREAD_GROUP_NAME);
	}

	public static ThreadGroup threadGroup() {
		return INSTANCE.group;
	}

	public static int threadId() {
		return threadCounter++;
	}
}