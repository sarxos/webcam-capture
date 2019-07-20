package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.List;

import junit.framework.TestCase;

public class TestCommanderUtil extends TestCase {
	public void testNormalExecution() {
		List<String> ret=CommanderUtil.execute("hostname");
		assertTrue(ret.size() != 0);
	}
	
	public void testExecutionWithTimeout() {
		int timeout=2000;
		
		String cmd="ping localhost";
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			cmd="ping localhost -t";
		}
		long before=System.currentTimeMillis();
		List<String> ret=CommanderUtil.execute(cmd, timeout);
		long after=System.currentTimeMillis();
		System.out.println(ret);
		assertTrue(after - before >= timeout);//java is not RTOS, offset is allowed
	}
}
