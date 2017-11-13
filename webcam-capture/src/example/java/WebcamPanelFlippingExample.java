import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;


@SuppressWarnings("serial")
public class WebcamPanelFlippingExample {

	public static void main(String[] args) throws InterruptedException {

		final Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.QVGA.getSize());

		final WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(true);

		final JCheckBox flip = new JCheckBox();
		flip.setSelected(true);
		flip.setAction(new AbstractAction("Flip") {

			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setMirrored(flip.isSelected());
			}
		});

		JFrame window = new JFrame("Test webcam panel");
		window.setLayout(new FlowLayout());
		window.add(panel);
		window.add(flip);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
