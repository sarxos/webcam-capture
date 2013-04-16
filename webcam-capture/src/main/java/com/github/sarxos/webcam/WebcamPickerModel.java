package com.github.sarxos.webcam;

import java.util.List;

import javax.swing.DefaultComboBoxModel;


public class WebcamPickerModel extends DefaultComboBoxModel {

	private static final long serialVersionUID = 1L;

	public WebcamPickerModel(List<Webcam> webcams) {
		super(webcams.toArray(new Webcam[webcams.size()]));
	}

	@Override
	public Webcam getSelectedItem() {
		return (Webcam) super.getSelectedItem();
	}

	@Override
	public void setSelectedItem(Object webcam) {
		if (!(webcam instanceof Webcam)) {
			throw new IllegalArgumentException("Selected object has to be an Webcam instance");
		}
		super.setSelectedItem(webcam);
	}
}