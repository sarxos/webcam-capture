import java.awt.Component;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class OptionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField widthField;
	private JTextField heightField;

	/**
	 * Create the panel.
	 */
	public OptionsPanel() {
		setToolTipText("colfx ");
		setLayout(new GridLayout(0, 2, 5, 5));

		JLabel lblWidth = new JLabel("width");
		add(lblWidth);

		widthField = new JTextField();
		widthField.setToolTipText("width");
		widthField.setText("320");
		add(widthField);
		widthField.setColumns(10);

		JLabel lblHeight = new JLabel("height");
		add(lblHeight);

		heightField = new JTextField();
		heightField.setToolTipText("height");
		heightField.setText("240");
		add(heightField);
		heightField.setColumns(10);

		JLabel lblSharpness = new JLabel("sharpness ");
		add(lblSharpness);

		JSlider sharpnessSlider = new JSlider();
		sharpnessSlider.setValue(0);
		sharpnessSlider.setMinimum(-100);
		sharpnessSlider.setToolTipText("sharpness ");
		add(sharpnessSlider);

		JLabel contrastL = new JLabel("contrast ");
		add(contrastL);

		JSlider contrastSlider = new JSlider();
		contrastSlider.setValue(0);
		contrastSlider.setToolTipText("contrast");
		add(contrastSlider);

		JLabel lblBrightness = new JLabel("brightness ");
		add(lblBrightness);

		JSlider brightnessS = new JSlider();
		brightnessS.setToolTipText("brightness");
		add(brightnessS);

		JLabel saturationLabel = new JLabel("saturation ");
		add(saturationLabel);

		JSlider saturationS = new JSlider();
		saturationS.setToolTipText("saturation ");
		add(saturationS);

		JLabel lblIso = new JLabel("ISO ");
		add(lblIso);

		JCheckBox chckbxIso = new JCheckBox("ISO ");
		chckbxIso.setToolTipText("ISO");
		add(chckbxIso);

		JLabel lblVstab = new JLabel("vstab ");
		add(lblVstab);

		JCheckBox chckbxVstab = new JCheckBox("vstab ");
		chckbxVstab.setToolTipText("vstab ");
		add(chckbxVstab);

		JLabel lblEv = new JLabel("ev ");
		add(lblEv);

		JCheckBox chckbxEv = new JCheckBox("ev ");
		chckbxEv.setToolTipText("ev ");
		add(chckbxEv);

		JLabel lblExposure = new JLabel("exposure");
		add(lblExposure);

		JComboBox comboBox = new JComboBox();
		comboBox.setToolTipText("exposure");
		comboBox.setModel(new DefaultComboBoxModel(new String[] { "off", "auto", "night", "nightpreview", "backlight",
				"spotlight", "sports", "snow", "beach", "verylong", "fixedfps", "antishake", "fireworks" }));
		add(comboBox);

		JLabel lblAwb = new JLabel("awb ");
		add(lblAwb);

		JComboBox awbcomboBox = new JComboBox();
		awbcomboBox.setModel(new DefaultComboBoxModel(new String[] { "off", "auto", "sun", "cloud", "shade", "tungsten",
				"fluorescent", "incandescent", "flash", "horizon" }));
		awbcomboBox.setToolTipText("awb");
		add(awbcomboBox);

		JLabel lblImxfx = new JLabel("imxfx");
		add(lblImxfx);

		JComboBox imxfxCB = new JComboBox();
		imxfxCB.setModel(new DefaultComboBoxModel(new String[] { "none", "negative", "solarise", "sketch", "denoise",
				"emboss", "oilpaint", "hatch", "gpen", "pastel", "watercolour", "film", "blur", "saturation",
				"colourswap", "washedout", "posterise", "colourpoint", "colourbalance", "cartoon" }));
		imxfxCB.setToolTipText("imxfx");
		add(imxfxCB);

		JLabel lblColfx = new JLabel("colfx ");
		add(lblColfx);

		JComboBox colfxCB = new JComboBox();
		colfxCB.setModel(new DefaultComboBoxModel(new String[] { "U", "V" }));
		colfxCB.setToolTipText("colfx ");
		add(colfxCB);

		JLabel lblMetering = new JLabel("metering ");
		add(lblMetering);

		JComboBox meteringCB = new JComboBox();
		meteringCB.setModel(new DefaultComboBoxModel(new String[] { "average", "spot", "backlit", "matrix" }));
		meteringCB.setToolTipText("metering ");
		add(meteringCB);

		JLabel lblHflip = new JLabel("hflip");
		add(lblHflip);

		JCheckBox chckbxHflip = new JCheckBox("hflip");
		chckbxHflip.setToolTipText("hflip");
		add(chckbxHflip);

		JLabel lblVflip = new JLabel("vflip ");
		add(lblVflip);

		JCheckBox chckbxVflip = new JCheckBox("vflip ");
		chckbxVflip.setToolTipText("hflip");
		add(chckbxVflip);

		JLabel lblRotation = new JLabel("rotation");
		add(lblRotation);

		JSlider rotationS = new JSlider();
		rotationS.setSnapToTicks(true);
		rotationS.setMajorTickSpacing(90);
		rotationS.setPaintTicks(true);
		rotationS.setValue(0);
		rotationS.setMaximum(359);
		rotationS.setToolTipText("rotation ");
		add(rotationS);

	}

	public Map<String, String> getOptionMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		Component[] components = this.getComponents();
		for (int i = 0; i < components.length; i++) {
			JComponent c = (JComponent) components[i];
			if (c instanceof JSlider) {
				map.put(c.getToolTipText().trim(), ((JSlider) c).getValue() + "");
			} else if (c instanceof JComboBox) {
				map.put(c.getToolTipText().trim(), ((JComboBox<?>) c).getSelectedItem().toString());
			} else if (c instanceof JCheckBox) {
				if (((JCheckBox) c).isSelected()) {
					map.put(c.getToolTipText().trim(), "");
				}
			} else if (c instanceof JTextField) {
				map.put(c.getToolTipText().trim(), ((JTextField) c).getText().trim());
			}
		}
		return map;
	}
}
