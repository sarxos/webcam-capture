package com.github.sarxos.webcam.ds.gst1.impl;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.State;
import org.freedesktop.gstreamer.StateChangeReturn;
import org.freedesktop.gstreamer.Structure;

import com.github.sarxos.webcam.WebcamException;


public class GsUtils {

	public static Element getCompatibleSource(String name) {
		final Element source = ElementFactory.make(getCompatibleSourceFactory(), name + "-source");
		source.set(getCompatibleSourceFactory(), name);
		return source;
	}

	public static String getCompatibleSourceFactory() {
		switch (GsPlatform.getOs()) {
			case LINUX:
				return "v4l2src";
			case WINDOWS:
				return "ksvideosrc";
			case MACOS:
				return "qtkitvideosrc";
			default:
				throw new WebcamException("This operating system is not supported");
		}
	}

	public static String getCompatibleSourceProperty() {
		switch (GsPlatform.getOs()) {
			case LINUX:
				return "device";
			case WINDOWS:
			case MACOS:
				return "device-index";
			default:
				throw new WebcamException("This operating system is not supported");
		}
	}

	public static Dimension capsStructureToResolution(Structure structure) {

		int w = -1;
		int h = -1;

		if (GsPlatform.isWindows()) {
			w = structure.getRange("width").getMinInt();
			h = structure.getRange("height").getMinInt();
		} else if (GsPlatform.isLinux()) {
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

	private String getCompatibleSourceIdentifier(int id) {
		switch (GsPlatform.getOs()) {
			case LINUX:
				return "/dev/video" + id;
			case WINDOWS:
			case MACOS:
				return Integer.toString(id);
			default:
				throw new WebcamException("This operating system is not supported");
		}
	}

	public List<String> getVideoIdentifiers() {

		final List<String> ids = new ArrayList<>();

		for (int i = 0; i < 50; i++) {

			final String id = getCompatibleSourceIdentifier(i);
			final String property = getCompatibleSourceProperty();
			final Element source = getCompatibleSource(id);

			source.set(property, id);
			source.setState(State.NULL);

			try {
				if (source.setState(State.READY) != StateChangeReturn.FAILURE) {
					ids.add(id);
				}
			} finally {
				source.setState(State.NULL);
				source.dispose();
			}
		}

		return ids;
	}
}
