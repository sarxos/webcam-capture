
package com.github.sarxos.webcam.ds.raspberrypi;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/** 
 * ClassName: GenerateInstances <br/> 
 * date: Jan 31, 2019 12:36:17 PM <br/> 
 * 
 * @author maoanapex88@163.com alexmao86 
 * @version  
 * @since JDK 1.8
 */
public class GenerateInstances {
	public static void main(String args[]) throws IOException {
		Set<String> longOpt=new HashSet<>();
		longOpt.addAll(load("still.txt"));
		longOpt.addAll(load("vid.txt"));
		longOpt.addAll(load("vidyuv.txt"));
		longOpt.addAll(load("yuv.txt"));
		
		for(String opt:longOpt) {
			String upper=opt.toUpperCase();
			System.out.println("/** "+upper+" --"+opt+"*/\n");
			System.out.println("String OPT_"+upper+"=\""+opt+"\";");
		}
	}
	public static Set<String> load(String name) throws IOException {
		Set<String> longOpt=new HashSet<>();
		List<String> lines = FileUtils.readLines(new File("src/etc/"+name));
		for (String line : lines) {
			if (!line.startsWith("-")) {
				continue;
			}
			int indexOfDesc = line.indexOf(":");
			String opts[] = line.substring(0, indexOfDesc).trim().split(",");
			longOpt.add(opts[1].replaceFirst("--", "").trim());
		}
		return longOpt;
	}
}
