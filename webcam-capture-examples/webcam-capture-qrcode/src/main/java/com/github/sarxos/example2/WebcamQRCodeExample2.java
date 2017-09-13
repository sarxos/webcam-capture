package com.github.sarxos.example2;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


@SuppressWarnings("serial")
public class WebcamQRCodeExample2 extends JFrame {

	public WebcamQRCodeExample2() {

		setTitle("Main frame");
		setLayout(new FlowLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		add(new JButton(new AbstractAction("CAPTURE QR") {

			@Override
			public void actionPerformed(ActionEvent e) {
				final Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						try (QrCapture qr = new QrCapture()) {
							showMessage("QR code text is:\n" + qr.getResult() + "");
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					};
				});
				thread.setDaemon(true);
				thread.start();
			}
		}));

		pack();
		setVisible(true);
	}

	private void showMessage(String text) {
		JOptionPane.showMessageDialog(null, text);
	}

	public static void main(String[] args) {
		new WebcamQRCodeExample2();
	}
}
