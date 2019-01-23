package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * ClassName: SortedMapTest <br/>
 * date: Feb 19, 2019 5:11:25 PM <br/>
 * 
 * @author maoanapex88@163.com alexmao86
 * @version
 * @since JDK 1.8
 */
public class SortedMapTest {
	public static void main(String args[]) {
		Map<String, String> sorted = new TreeMap<>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int s1 = o1.hashCode();
				int s2 = o2.hashCode();

				if (o1.equals("output")) {
					s1 = Integer.MAX_VALUE;
				} else if (o1.equals("raw")) {
					s1 = Integer.MAX_VALUE - 1;
				}

				if (o2.equals("output")) {
					s2 = Integer.MAX_VALUE;
				} else if (o2.equals("raw")) {
					s2 = Integer.MAX_VALUE - 1;
				}

				return s1 - s2;
			}
		});
		Map<String, String> map = new HashMap<>();

		map.put("output", "value o");
		map.put("raw", "value raw");
		map.put("width", "value a");
		map.put("raw-format", "value b");
		map.put("d", "value d");
		map.put("c", "value c");
		sorted.putAll(map);

		System.out.println(sorted);
		sorted.put("e", "value e");
		StringBuilder command = new StringBuilder(128);
		for (Entry<String, String> entry : sorted.entrySet()) {
			command.append("--").append(entry.getKey()).append(" ");
			if (entry.getValue() != null) {
				command.append(entry.getValue()).append(" ");
			}
		}

		String commandString = command.toString();
		System.out.println(commandString);
		System.out.println(1 >> 1);
	}
}
