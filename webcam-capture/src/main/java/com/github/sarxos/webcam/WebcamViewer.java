package com.github.sarxos.webcam;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Just a simple webcam viewer.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamViewer extends JFrame implements Runnable, WebcamListener, WindowListener, UncaughtExceptionHandler, ItemListener {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(WebcamViewer.class);

	private Webcam webcam = null;
	private WebcamPanel panel = null;
	private WebcamPicker picker = null;

	public WebcamViewer() {
		SwingUtilities.invokeLater(this);
	}

	@Override
	public void run() {

		setTitle("Webcam Capture Viewer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		addWindowListener(this);

		picker = new WebcamPicker();
		picker.addItemListener(this);

		webcam = picker.getSelectedWebcam();

		if (webcam == null) {
			LOG.error("No webcams found");
			System.exit(1);
		}

		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.addWebcamListener(WebcamViewer.this);

		panel = new WebcamPanel(webcam, false);
		panel.setFPSDisplayed(true);

		add(picker, BorderLayout.NORTH);
		add(panel, BorderLayout.CENTER);

		pack();
		setVisible(true);

		Thread t = new Thread() {

			@Override
			public void run() {
				panel.start();
			}
		};
		t.setName("webcam-viewer-starter");
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(this);
		t.start();
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
		LOG.info("Webcam open");
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		LOG.info("Webcam closed");
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
		LOG.info("Webcam disposed");
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
		// do nothing
	}

	@Override
	public void windowActivated(WindowEvent e) {
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
		LOG.info("Webcam viewer resumed");
		panel.resume();
	}

	@Override
	public void windowIconified(WindowEvent e) {
		LOG.info("Webcam viewer paused");
		panel.pause();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		LOG.error(String.format("Exception in thread %s", t.getName()), e);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getItem() == webcam) {
			return;
		}

		if (webcam == null) {
			return;
		}

		final WebcamPanel tmp = panel;

		remove(panel);

		webcam.removeWebcamListener(this);

		webcam = (Webcam) e.getItem();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.addWebcamListener(this);

		System.out.println("selected " + webcam.getName());

		panel = new WebcamPanel(webcam, false);

		add(panel, BorderLayout.CENTER);

		Thread t = new Thread() {

			@Override
			public void run() {
				tmp.stop();
				panel.start();
			}
		};
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(this);
		t.start();

	}
}
