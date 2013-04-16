package com.github.sarxos.webcam;

import java.util.List;

import javax.swing.JComboBox;


public class WebcamPicker extends JComboBox {

	private static final long serialVersionUID = 1L;

	private static final WebcamPickerCellRenderer RENDERER = new WebcamPickerCellRenderer();

	public WebcamPicker() {
		this(Webcam.getWebcams());
	}

	public WebcamPicker(List<Webcam> webcams) {
		super(new WebcamPickerModel(webcams));
		setRenderer(RENDERER);
	}

	public Webcam getSelectedWebcam() {
		return (Webcam) getSelectedItem();
	}
}
