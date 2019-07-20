package com.github.sarxos.webcam.ds.raspberrypi;

import org.apache.commons.cli.Options;

import junit.framework.TestCase;

public class TestOptionsBuilder extends TestCase {
	public void testBuilder() {

		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			System.out.println("this in on on board test, can not run on windows host. thanks");
		} else {
			Options options = OptionsBuilder.create("cat src/etc/still.txt");
			assertTrue(options.hasOption("w"));
			assertTrue(options.hasOption("height"));
			assertTrue(options.hasOption("keypress"));
			assertTrue(options.hasOption("cs"));
		}
	}
}
