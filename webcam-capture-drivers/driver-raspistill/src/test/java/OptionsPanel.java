import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.github.sarxos.webcam.Webcam;

public class OptionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<Webcam> cams;

	/**
	 * Create the panel.
	 */
	public OptionsPanel() {
		setToolTipText("colfx ");
		setLayout(new GridLayout(0, 4, 0, 0));
		
		JLabel lblWidth = new JLabel("width");
		add(lblWidth);
		
		JSlider slider = new JSlider();
		slider.setToolTipText("width");
		slider.setMaximum(1920);
		slider.setMinimum(196);
		add(slider);
		
		JLabel label = new JLabel("");
		add(label);
		
		JLabel label_1 = new JLabel("");
		add(label_1);
		
		JLabel lblHeight = new JLabel("height");
		add(lblHeight);
		
		JSlider slider_1 = new JSlider();
		slider_1.setMaximum(1080);
		slider_1.setMinimum(128);
		slider_1.setToolTipText("height");
		add(slider_1);
		
		JLabel label_2 = new JLabel("");
		add(label_2);
		
		JLabel label_3 = new JLabel("");
		add(label_3);
		
		JLabel lblQuality = new JLabel("quality");
		add(lblQuality);
		
		JSlider slider_2 = new JSlider();
		slider_2.setToolTipText("quality");
		add(slider_2);
		
		JLabel label_4 = new JLabel("");
		add(label_4);
		
		JLabel label_5 = new JLabel("");
		add(label_5);
		
		JLabel lblSharpness = new JLabel("sharpness ");
		add(lblSharpness);
		
		JSlider slider_3 = new JSlider();
		slider_3.setValue(0);
		slider_3.setMinimum(-100);
		slider_3.setToolTipText("sharpness ");
		add(slider_3);
		
		JLabel label_6 = new JLabel("");
		add(label_6);
		
		JLabel label_7 = new JLabel("");
		add(label_7);
		
		JLabel lblNewLabel = new JLabel("contrast ");
		add(lblNewLabel);
		
		JSlider slider_4 = new JSlider();
		slider_4.setValue(0);
		slider_4.setToolTipText("contrast");
		add(slider_4);
		
		JLabel label_8 = new JLabel("");
		add(label_8);
		
		JLabel label_9 = new JLabel("");
		add(label_9);
		
		JLabel lblBrightness = new JLabel("brightness ");
		add(lblBrightness);
		
		JSlider slider_5 = new JSlider();
		slider_5.setToolTipText("brightness ");
		add(slider_5);
		
		JLabel label_10 = new JLabel("");
		add(label_10);
		
		JLabel label_11 = new JLabel("");
		add(label_11);
		
		JLabel lblNewLabel_1 = new JLabel("saturation ");
		add(lblNewLabel_1);
		
		JSlider slider_6 = new JSlider();
		slider_6.setToolTipText("saturation ");
		add(slider_6);
		
		JLabel label_12 = new JLabel("");
		add(label_12);
		
		JLabel label_13 = new JLabel("");
		add(label_13);
		
		JLabel lblIso = new JLabel("ISO ");
		add(lblIso);
		
		JCheckBox chckbxIso = new JCheckBox("ISO ");
		chckbxIso.setToolTipText("ISO");
		add(chckbxIso);
		
		JLabel label_14 = new JLabel("");
		add(label_14);
		
		JLabel label_15 = new JLabel("");
		add(label_15);
		
		JLabel lblVstab = new JLabel("vstab ");
		add(lblVstab);
		
		JCheckBox chckbxVstab = new JCheckBox("vstab ");
		chckbxVstab.setToolTipText("vstab ");
		add(chckbxVstab);
		
		JLabel label_16 = new JLabel("");
		add(label_16);
		
		JLabel label_17 = new JLabel("");
		add(label_17);
		
		JLabel lblEv = new JLabel("ev ");
		add(lblEv);
		
		JCheckBox chckbxEv = new JCheckBox("ev ");
		chckbxEv.setToolTipText("ev ");
		add(chckbxEv);
		
		JLabel label_18 = new JLabel("");
		add(label_18);
		
		JLabel label_19 = new JLabel("");
		add(label_19);
		
		JLabel lblExposure = new JLabel("exposure");
		add(lblExposure);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setToolTipText("exposure");
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"off", "auto", "night", "nightpreview", "backlight", "spotlight", "sports", "snow", "beach", "verylong", "fixedfps", "antishake", "fireworks"}));
		add(comboBox);
		
		JLabel label_20 = new JLabel("");
		add(label_20);
		
		JLabel label_21 = new JLabel("");
		add(label_21);
		
		JLabel lblAwb = new JLabel("awb ");
		add(lblAwb);
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"off", "auto", "sun", "cloud", "shade", "tungsten", "fluorescent", "incandescent", "flash", "horizon"}));
		comboBox_1.setToolTipText("awb");
		add(comboBox_1);
		
		JLabel label_22 = new JLabel("");
		add(label_22);
		
		JLabel label_23 = new JLabel("");
		add(label_23);
		
		JLabel lblImxfx = new JLabel("imxfx");
		add(lblImxfx);
		
		JComboBox comboBox_2 = new JComboBox();
		comboBox_2.setModel(new DefaultComboBoxModel(new String[] {"none", "negative", "solarise", "sketch", "denoise", "emboss", "oilpaint", "hatch", "gpen", "pastel", "watercolour", "film", "blur", "saturation", "colourswap", "washedout", "posterise", "colourpoint", "colourbalance", "cartoon"}));
		comboBox_2.setToolTipText("imxfx");
		add(comboBox_2);
		
		JLabel label_24 = new JLabel("");
		add(label_24);
		
		JLabel label_25 = new JLabel("");
		add(label_25);
		
		JLabel lblColfx = new JLabel("colfx ");
		add(lblColfx);
		
		JComboBox comboBox_3 = new JComboBox();
		comboBox_3.setModel(new DefaultComboBoxModel(new String[] {"U", "V"}));
		comboBox_3.setToolTipText("colfx ");
		add(comboBox_3);
		
		JLabel label_26 = new JLabel("");
		add(label_26);
		
		JLabel label_27 = new JLabel("");
		add(label_27);
		
		JLabel lblMetering = new JLabel("metering ");
		add(lblMetering);
		
		JComboBox comboBox_4 = new JComboBox();
		comboBox_4.setModel(new DefaultComboBoxModel(new String[] {"average", "spot", "backlit", "matrix"}));
		comboBox_4.setToolTipText("metering ");
		add(comboBox_4);
		
		JLabel label_28 = new JLabel("");
		add(label_28);
		
		JLabel label_29 = new JLabel("");
		add(label_29);
		
		JLabel lblHflip = new JLabel("hflip");
		add(lblHflip);
		
		JCheckBox chckbxHflip = new JCheckBox("hflip");
		chckbxHflip.setToolTipText("hflip");
		add(chckbxHflip);
		
		JLabel label_30 = new JLabel("");
		add(label_30);
		
		JLabel label_31 = new JLabel("");
		add(label_31);
		
		JLabel lblVflip = new JLabel("vflip ");
		add(lblVflip);
		
		JCheckBox chckbxVflip = new JCheckBox("vflip ");
		chckbxVflip.setToolTipText("hflip");
		add(chckbxVflip);
		
		JLabel label_32 = new JLabel("");
		add(label_32);
		
		JLabel label_33 = new JLabel("");
		add(label_33);
		
		JLabel lblRotation = new JLabel("rotation");
		add(lblRotation);
		
		JSlider slider_7 = new JSlider();
		slider_7.setValue(0);
		slider_7.setMaximum(359);
		slider_7.setToolTipText("rotation ");
		add(slider_7);
		
		JLabel label_34 = new JLabel("");
		add(label_34);
		
		JLabel label_35 = new JLabel("");
		add(label_35);
		
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(Webcam c:cams) {
					if(!c.isOpen()) {
						c.open();
					}
				}
			}
		});
		add(btnStart);
		
		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(Webcam c:cams) {
					if(c.isOpen()) {
						c.close();
					}
				}
			}
		});
		add(btnStop);
		
		JLabel label_36 = new JLabel("");
		add(label_36);
		
		JLabel label_37 = new JLabel("");
		add(label_37);

	}

	/** 
	 * Creates a new instance of OptionsPanel. 
	 * 
	 * @param cams 
	 */ 
	public OptionsPanel(List<Webcam> cams) {
		this.cams=cams;
	}

	public Map<String, String> getOptionMap(){
		Map<String, String> map=new LinkedHashMap<String, String>();
		Component[] components=this.getComponents();
		for(int i=0;i<components.length;i++) {
			JComponent c=(JComponent)components[i];
			if(c instanceof JSlider) {
				map.put(c.getToolTipText().trim(), ((JSlider) c).getValue()+"");
			}
			else if(c instanceof JComboBox) {
				map.put(c.getToolTipText().trim(), ((JComboBox<?>) c).getSelectedItem().toString());
			}
			else if(c instanceof JCheckBox) {
				if(((JCheckBox) c).isSelected()) {
					map.put(c.getToolTipText().trim(), "");
				}
			}
		}
		return map;
	}
}
