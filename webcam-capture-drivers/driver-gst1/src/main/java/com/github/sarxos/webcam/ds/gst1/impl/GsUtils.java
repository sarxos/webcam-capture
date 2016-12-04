package com.github.sarxos.webcam.ds.gst1.impl;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.bridj.Platform;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Structure;

import com.github.sarxos.webcam.WebcamException;


public class GsUtils {

	public enum OS {
		WINDOWS,
		LINUX,
		MACOS,
	}

	private static OS os;
	static {
		if (Platform.isLinux()) {
			os = OS.LINUX;
		}
		if (Platform.isWindows()) {
			os = OS.WINDOWS;
		}
		if (Platform.isMacOSX()) {
			os = OS.MACOS;
		}
	}

	public static OS getOs() {
		return os;
	}

	public static Element createCompatibleSource(String name) {
		switch (getOs()) {
			case LINUX:
				return createLinuxSource(name);
			case WINDOWS:
				return createWindowsSource(name);
			case MACOS:
				return createMacOsSource(name);
			default:
				throw new WebcamException("This operating system is not supported");
		}
	}

	public static String getCompatibleSourceName() {
		switch (getOs()) {
			case LINUX:
				return "v4l2src";
			case WINDOWS:
				return "dshowvideosrc";
			case MACOS:
				return "qtkitvideosrc";
			default:
				throw new WebcamException("This operating system is not supported");
		}
	}

	private static Element createLinuxSource(String name) {
		final Element source = createSource(name);
		source.set("device", name);
		return source;
	}

	private static Element createWindowsSource(String name) {
		final Element source = createSource(name);
		source.set("device-name", name);
		return source;
	}

	private static Element createMacOsSource(String name) {
		final Element source = createSource(name);
		source.set("device-index", name);
		return source;
	}

	private static Element createSource(String name) {
		return ElementFactory.make(getCompatibleSourceName(), name + "-source");
	}

	public static Dimension capsStructureToResolution(Structure structure) {

		int w = -1;
		int h = -1;

		if (Platform.isWindows()) {
			w = structure.getRange("width").getMinInt();
			h = structure.getRange("height").getMinInt();
		} else if (Platform.isLinux()) {
			w = structure.getInteger("width");
			h = structure.getInteger("height");
		}

		if (w > 0 && h > 0) {
			return new Dimension(w, h);
		} else {
			return null;
		}
	}

	public static Dimension[] getResolutionsFromCaps(Caps caps, String format) {

		final Map<String, Dimension> map = new LinkedHashMap<>();

		for (int i = 0; i < caps.size(); i++) {

			final Structure structure = caps.getStructure(i);
			final String f = structure.getString("format");

			if (!Objects.equals(f, format)) {
				continue;
			}

			final Dimension resolution = capsStructureToResolution(structure);

			if (resolution != null) {
				map.put(resolution.width + "x" + resolution.height, resolution);
			}
		}

		return new ArrayList<Dimension>(map.values()).toArray(new Dimension[0]);
	}

	public static void dispose(Element element) {
		if (element != null) {
			element.dispose();
		}
	}
}
