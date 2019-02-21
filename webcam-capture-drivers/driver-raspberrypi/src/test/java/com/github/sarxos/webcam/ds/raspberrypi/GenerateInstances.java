
package com.github.sarxos.webcam.ds.raspberrypi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * ClassName: GenerateInstances <br/>
 * utility to generate constant fields date: Jan 31, 2019 12:36:17 PM <br/>
 * 
 * @author maoanapex88@163.com alexmao86
 * @version
 * @since JDK 1.8
 */
public class GenerateInstances {
	public static void main(String args[]) throws IOException {
		Map<String, String> longOpt = new HashMap<>();
		load("still.txt", longOpt);
		load("vid.txt", longOpt);
		load("vidyuv.txt", longOpt);
		load("yuv.txt", longOpt);

		for (String opt : longOpt.keySet()) {
			String upper = opt.replaceAll("-", "_").toUpperCase();
			System.out.println("/** " + upper + " --" + opt + ", " + longOpt.get(opt) + " */");
			System.out.println("String OPT_" + upper + "=\"" + opt + "\";\n");
		}
	}

	public static Map<String, String> load(String name, Map<String, String> longOpt) throws IOException {
		List<String> lines = FileUtils.readLines(new File("src/etc/" + name));
		for (String line : lines) {
			if (!line.startsWith("-")) {
				continue;
			}
			int indexOfDesc = line.indexOf(":");
			String opts[] = line.substring(0, indexOfDesc).trim().split(",");

			String key = opts[1].replaceFirst("--", "").trim();
			if (longOpt.containsKey(key)) {
				String value = longOpt.get(key);
				longOpt.put(key, value + ", raspi" + name.replaceFirst(".txt", ""));
			} else {
				longOpt.put(key, "raspi" + name.replaceFirst(".txt", ""));
			}
		}
		return longOpt;
	}
}
