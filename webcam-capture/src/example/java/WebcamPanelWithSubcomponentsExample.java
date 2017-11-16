import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;


@SuppressWarnings("serial")
public class WebcamPanelWithSubcomponentsExample {

	public static void main(String[] args) throws InterruptedException {

		final Dimension size = WebcamResolution.VGA.getSize();

		final Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(size);

		final WebcamPanel panel = new WebcamPanel(webcam, false);
		panel.setFPSDisplayed(true);
		panel.setDisplayDebugInfo(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(true);

		final String play = "PLAY";
		final String stop = "STOP";

		final JButton button = new JButton();
		button.setAction(new AbstractAction(play) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (panel.isStarted()) {
					panel.stop();
					button.setText(play);
				} else {
					panel.start();
					button.setText(stop);
				}
			}
		});

		button.setBounds(getButtonBounds(size));

		panel.setLayout(null);
		panel.add(button);

		final JFrame window = new JFrame("Test webcam panel");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}

	private static Rectangle getButtonBounds(Dimension size) {
		final int x = (int) (size.width * 0.1);
		final int y = (int) (size.height * 0.8);
		final int w = (int) (size.width * 0.8);
		final int h = (int) (size.height * 0.1);
		return new Rectangle(x, y, w, h);
	}
}
