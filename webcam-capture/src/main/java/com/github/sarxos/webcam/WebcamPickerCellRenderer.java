package com.github.sarxos.webcam;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class WebcamPickerCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final ImageIcon ICON = new ImageIcon(WebcamPickerCellRenderer.class.getResource("/com/github/sarxos/webcam/icons/camera-icon.png"));

	public WebcamPickerCellRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
		setIcon(ICON);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int i, boolean selected, boolean focused) {

		Webcam webcam = (Webcam) value;

		if (selected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setText(webcam.getName());
		setFont(list.getFont());

		return this;
	}

}
