package com.github.sarxos.webcam.ds.raspistill;

import org.apache.commons.cli.Options;

import junit.framework.TestCase;

public class TestOptionsBuilder extends TestCase{
	public void testBuilder() {
		if(System.getProperty("os.name").toLowerCase().contains("windows")) {
			System.out.println("this in on on board test, can not run on windows host. thanks");
		}
		else {
			Options options=OptionsBuilder.create();
			assertEquals(options.hasOption("w"), true);
			assertEquals(options.hasOption("gc"), true);
			assertEquals(options.hasOption("width"), true);
			assertEquals(options.hasOption("preview"), true);
		}
	}
}
