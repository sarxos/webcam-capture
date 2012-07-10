package com.github.sarxos.webcam;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;


/**
 * Proof of concept of how to handle webcam video stream from Java
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamViewerExample extends JFrame implements Runnable, WebcamListener, WindowListener {

	private static final long serialVersionUID = -2831291292491395695L;

	private Webcam webcam = null;

	private JPanel wait = null;
	private WebcamPanel view = null;

	@Override
	public void run() {

		setTitle("Java Webcam Capture POC");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);

		wait = new JPanel();
		wait.add(new JLabel("Initialization"));

		setContentPane(wait);

		pack();
		setVisible(true);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				webcam = Webcam.getDefault();
				if (webcam == null) {
					System.out.println("No webcams found...");
					System.exit(1);
				}

				view = new WebcamPanel(webcam);

				webcam.addWebcamListener(WebcamViewerExample.this);
				webcam.open();
			}
		});
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new WebcamViewerExample());
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
		setContentPane(view);
		pack();
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		setContentPane(wait);
		pack();
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosed(WindowEvent e) {
		webcam.close();
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		view.resume();
	}

	@Override
	public void windowIconified(WindowEvent e) {
		view.pause();
	}
}
