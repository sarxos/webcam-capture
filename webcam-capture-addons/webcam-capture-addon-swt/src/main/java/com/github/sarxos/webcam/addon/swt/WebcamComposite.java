package com.github.sarxos.webcam.addon.swt;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.github.sarxos.webcam.Webcam;


public class WebcamComposite extends Composite {

	private Display display = null;
	private Webcam webcam = null;
	private Image image = null;

	public WebcamComposite(Display display, Composite parent, int style) {
		super(parent, style);
		// setLayout(new FillLayout(SWT.HORIZONTAL));
		this.display = display;
	}

	public void setWebcam(Webcam webcam) {
		if (webcam == null) {
			throw new IllegalArgumentException("Webcam cannot be null");
		}
		this.webcam = webcam;
	}

	public ImageData getImageData() {

		BufferedImage image = webcam.getImage();

		DirectColorModel model = (DirectColorModel) image.getColorModel();
		PaletteData palette = new PaletteData(model.getRedMask(), model.getGreenMask(), model.getBlueMask());
		ImageData data = new ImageData(image.getWidth(), image.getHeight(), model.getPixelSize(), palette);
		WritableRaster raster = image.getRaster();

		int[] pixelArray = new int[3];

		for (int y = 0; y < data.height; y++) {
			for (int x = 0; x < data.width; x++) {
				raster.getPixel(x, y, pixelArray);
				int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
				data.setPixel(x, y, pixel);
			}
		}

		return data;
	}

	@Override
	public void dispose() {
		if (image != null) {
			image.dispose();
		}
		super.dispose();
	}

	@Override
	public void update() {

		Image tmp = image;
		try {
			image = new Image(display, getImageData());
		} finally {
			if (tmp != null) {
				tmp.dispose();
			}
		}
	}

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);

		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setText("Test");
		shell.setSize(640, 480);

		WebcamComposite wc = new WebcamComposite(display, shell, SWT.EMBEDDED);
		wc.setWebcam(Webcam.getDefault());

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				System.out.println("uuu");
				display.sleep();
			}
		}

		System.out.println("pp");

		wc.dispose();
		display.dispose();
	}
}
