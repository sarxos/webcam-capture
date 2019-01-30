import java.awt.Component;
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

import net.miginfocom.swing.MigLayout;

public class OptionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<Webcam> cams;

	/**
	 * Create the panel.
	 */
	public OptionsPanel() {
		setToolTipText("colfx ");
		setLayout(new MigLayout("", "[53px][200px,grow]", "[26px][26px][26px][26px][26px][26px][26px][][][][][][][][][][][][]"));
		
		JLabel lblWidth = new JLabel("width");
		add(lblWidth, "cell 0 0,growx,aligny center");
		
		JSlider slider = new JSlider();
		slider.setToolTipText("width");
		slider.setMaximum(1920);
		slider.setMinimum(196);
		add(slider, "cell 1 0,alignx left,aligny top");
		
		JLabel lblHeight = new JLabel("height");
		add(lblHeight, "cell 0 1,alignx left,aligny center");
		
		JSlider slider_1 = new JSlider();
		slider_1.setMaximum(1080);
		slider_1.setMinimum(128);
		slider_1.setToolTipText("height");
		add(slider_1, "cell 1 1,alignx left,aligny top");
		
		JLabel lblQuality = new JLabel("quality");
		add(lblQuality, "cell 0 2,growx,aligny center");
		
		JSlider slider_2 = new JSlider();
		slider_2.setToolTipText("quality");
		add(slider_2, "cell 1 2,alignx left,aligny top");
		
		JLabel lblSharpness = new JLabel("sharpness ");
		add(lblSharpness, "cell 0 3,growx,aligny center");
		
		JSlider slider_3 = new JSlider();
		slider_3.setValue(0);
		slider_3.setMinimum(-100);
		slider_3.setToolTipText("sharpness ");
		add(slider_3, "cell 1 3,alignx left,aligny top");
		
		JLabel lblNewLabel = new JLabel("contrast ");
		add(lblNewLabel, "cell 0 4,growx,aligny center");
		
		JSlider slider_4 = new JSlider();
		slider_4.setValue(0);
		slider_4.setToolTipText("contrast");
		add(slider_4, "cell 1 4,alignx left,aligny top");
		
		JLabel lblBrightness = new JLabel("brightness ");
		add(lblBrightness, "cell 0 5,growx,aligny center");
		
		JSlider slider_5 = new JSlider();
		slider_5.setToolTipText("brightness ");
		add(slider_5, "cell 1 5,alignx left,aligny top");
		
		JLabel lblNewLabel_1 = new JLabel("saturation ");
		add(lblNewLabel_1, "cell 0 6,growx,aligny center");
		
		JSlider slider_6 = new JSlider();
		slider_6.setToolTipText("saturation ");
		add(slider_6, "cell 1 6,alignx left,aligny top");
		
		JLabel lblIso = new JLabel("ISO ");
		add(lblIso, "cell 0 7");
		
		JCheckBox chckbxIso = new JCheckBox("ISO ");
		chckbxIso.setToolTipText("ISO");
		add(chckbxIso, "cell 1 7");
		
		JLabel lblVstab = new JLabel("vstab ");
		add(lblVstab, "cell 0 8");
		
		JCheckBox chckbxVstab = new JCheckBox("vstab ");
		chckbxVstab.setToolTipText("vstab ");
		add(chckbxVstab, "cell 1 8");
		
		JLabel lblEv = new JLabel("ev ");
		add(lblEv, "cell 0 9");
		
		JCheckBox chckbxEv = new JCheckBox("ev ");
		chckbxEv.setToolTipText("ev ");
		add(chckbxEv, "cell 1 9");
		
		JLabel lblExposure = new JLabel("exposure");
		add(lblExposure, "cell 0 10,alignx trailing");
		
		JComboBox comboBox = new JComboBox();
		comboBox.setToolTipText("exposure");
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"off", "auto", "night", "nightpreview", "backlight", "spotlight", "sports", "snow", "beach", "verylong", "fixedfps", "antishake", "fireworks"}));
		add(comboBox, "cell 1 10,growx");
		
		JLabel lblAwb = new JLabel("awb ");
		add(lblAwb, "cell 0 11,alignx trailing");
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"off", "auto", "sun", "cloud", "shade", "tungsten", "fluorescent", "incandescent", "flash", "horizon"}));
		comboBox_1.setToolTipText("awb");
		add(comboBox_1, "cell 1 11,growx");
		
		JLabel lblImxfx = new JLabel("imxfx");
		add(lblImxfx, "cell 0 12,alignx trailing");
		
		JComboBox comboBox_2 = new JComboBox();
		comboBox_2.setModel(new DefaultComboBoxModel(new String[] {"none", "negative", "solarise", "sketch", "denoise", "emboss", "oilpaint", "hatch", "gpen", "pastel", "watercolour", "film", "blur", "saturation", "colourswap", "washedout", "posterise", "colourpoint", "colourbalance", "cartoon"}));
		comboBox_2.setToolTipText("imxfx");
		add(comboBox_2, "cell 1 12,growx");
		
		JLabel lblColfx = new JLabel("colfx ");
		add(lblColfx, "cell 0 13,alignx trailing");
		
		JComboBox comboBox_3 = new JComboBox();
		comboBox_3.setModel(new DefaultComboBoxModel(new String[] {"U", "V"}));
		comboBox_3.setToolTipText("colfx ");
		add(comboBox_3, "cell 1 13,growx");
		
		JLabel lblMetering = new JLabel("metering ");
		add(lblMetering, "cell 0 14,alignx trailing");
		
		JComboBox comboBox_4 = new JComboBox();
		comboBox_4.setModel(new DefaultComboBoxModel(new String[] {"average", "spot", "backlit", "matrix"}));
		comboBox_4.setToolTipText("metering ");
		add(comboBox_4, "cell 1 14,growx");
		
		JLabel lblHflip = new JLabel("hflip");
		add(lblHflip, "cell 0 15");
		
		JCheckBox chckbxHflip = new JCheckBox("hflip");
		chckbxHflip.setToolTipText("hflip");
		add(chckbxHflip, "cell 1 15");
		
		JLabel lblVflip = new JLabel("vflip ");
		add(lblVflip, "cell 0 16");
		
		JCheckBox chckbxVflip = new JCheckBox("vflip ");
		chckbxVflip.setToolTipText("hflip");
		add(chckbxVflip, "cell 1 16");
		
		JLabel lblRotation = new JLabel("rotation");
		add(lblRotation, "cell 0 17");
		
		JSlider slider_7 = new JSlider();
		slider_7.setValue(0);
		slider_7.setMaximum(359);
		slider_7.setToolTipText("rotation ");
		add(slider_7, "cell 1 17");
		
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
		add(btnStart, "cell 0 18");
		
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
		add(btnStop, "cell 1 18");

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
