package com.github.sarxos.webcam;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.image4j.codec.ico.ICODecoder;


public class WebcamExecutableExample extends JFrame implements ActionListener {

	private static final long serialVersionUID = -1368783325310232511L;

	private Executor executor = Executors.newSingleThreadExecutor();
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private Webcam webcam = null;
	private WebcamPanel panel = null;
	private JButton button = null;

	public WebcamExecutableExample() {
		super();

		setTitle("Webcam Executable Example");
		setLayout(new FlowLayout(FlowLayout.CENTER));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		webcam = Webcam.getDefault();
		webcam.setViewSize(webcam.getViewSizes()[0]);

		panel = new WebcamPanel(webcam, false);
		panel.setPreferredSize(webcam.getViewSize());
		panel.setOpaque(true);
		panel.setBackground(Color.BLACK);

		ImageIcon icon = null;
		try {
			List<BufferedImage> icons = ICODecoder.read(getClass().getResourceAsStream("/Security-Camera.ico"));
			icon = new ImageIcon(icons.get(1));
		} catch (IOException e) {
			e.printStackTrace();
		}

		button = new JButton(icon);
		button.addActionListener(this);
		button.setFocusable(false);
		button.setPreferredSize(webcam.getViewSize());

		add(panel);
		add(button);

		pack();
		setVisible(true);
	}

	boolean running = false;

	public static void main(String[] args) throws IOException {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				new WebcamExecutableExample();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (initialized.compareAndSet(false, true)) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					panel.start();
				}
			});
		}
	}

}
