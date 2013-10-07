package com.github.sarxos.spycam;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPicker;
import com.github.sarxos.webcam.WebcamResolution;


/**
 * Proof of concept of how to handle webcam video stream from Java
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class SpycamMain extends JFrame implements Runnable, WebcamListener, WindowListener, UncaughtExceptionHandler, ItemListener, ThreadFactory, WebcamMotionListener {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SpycamMain.class);

	private final ExecutorService executor = Executors.newCachedThreadPool(this);
	private final AtomicInteger counter = new AtomicInteger(0);
	private final AtomicLong idnum = new AtomicLong(1000000000);
	private final DefaultHttpClient client = new DefaultHttpClient();
	private final String uri = "http://webcam-capture.sarxos.pl/upload-demo/upload.php";
	private final Dimension size = WebcamResolution.QVGA.getSize();

	private Webcam webcam = null;
	private WebcamPanel panel = null;
	private WebcamPicker picker = null;
	private WebcamMotionDetector detector = null;

	public SpycamMain() {

		super();

		setTitle("Spy Camera Service");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		addWindowListener(this);

		picker = new WebcamPicker();
		picker.addItemListener(this);

		webcam = Webcam.getDefault();
		webcam.setViewSize(size);

		panel = new WebcamPanel(webcam, false);

		add(picker, BorderLayout.NORTH);
		add(panel, BorderLayout.CENTER);

		pack();
		setVisible(true);

		executor.execute(new Runnable() {

			@Override
			public void run() {
				pick(picker.getSelectedWebcam());
			}
		});
	}

	@Override
	public void run() {

	}

	public static void main(String[] args) {

		if (Webcam.getWebcams().isEmpty()) {
			System.err.println("No webcams detected in the system");
			System.exit(1);
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new SpycamMain();
			}
		});
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
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
		LOG.debug("Spycam window has been closed");
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
	public void windowIconified(WindowEvent e) {
		LOG.debug("Spycam window has been iconified");
		panel.pause();
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		LOG.debug("Spycam window has been deiconified");
		panel.resume();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		System.err.println(String.format("Exception in thread %s", t.getName()));
		e.printStackTrace();
		LOG.error("Exception when running spycam", e);
	}

	private void pick(Webcam w) {

		if (w == null) {
			throw new IllegalArgumentException("Selected webcam cannot be null");
		}

		if (panel != null) {
			panel.stop();
		}

		if (webcam != null) {
			webcam.removeWebcamListener(this);
			webcam.close();
		}

		if (detector != null) {
			detector.removeMotionListener(this);
			detector.stop();
		}

		LOG.info("Selected {}", webcam);

		webcam = w;
		webcam.setViewSize(size);
		webcam.addWebcamListener(this);

		detector = new WebcamMotionDetector(webcam);
		detector.setInterval(5000);
		detector.addMotionListener(this);
		detector.start();

		executor.execute(new Runnable() {

			@Override
			public void run() {
				remove(panel);
				add(panel = new WebcamPanel(webcam), BorderLayout.CENTER);
				pack();
			}
		});

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getItem() != webcam) {
			pick((Webcam) e.getItem());
		}
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, String.format("gui-executor-thread-%d", counter.incrementAndGet()));
		t.setUncaughtExceptionHandler(this);
		t.setDaemon(true);
		return t;
	}

	@Override
	public void motionDetected(WebcamMotionEvent wme) {

		LOG.info("{}: motion {}", idnum.incrementAndGet(), wme.getArea());

		BufferedImage image = webcam.getImage();
		if (image == null) {
			return;
		}

		File tmp = null;
		try {
			tmp = File.createTempFile("spycam-tmp-picture", null);
			ImageIO.write(image, "JPG", tmp);
			upload(tmp);
		} catch (Exception e) {
			LOG.error("Exception while uploading picture", e);
		} finally {
			if (tmp != null && !tmp.delete()) {
				tmp.deleteOnExit();
			}
		}
	}

	private void upload(File file) throws ClientProtocolException, IOException, ParseException, URISyntaxException {

		LOG.debug("Uploading picture {} to {}", file, uri);

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("picture", new FileBody(file, "image/jpg"));
		entity.addPart("passwd", new StringBody("test1234"));

		HttpPost post = new HttpPost(new URI(uri));
		post.setEntity(entity);

		HttpResponse response = client.execute(post);

		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
			LOG.info("Tick picture stored as {} ", EntityUtils.toString(response.getEntity()));
		} else {
			LOG.error("Error {}", response.getStatusLine());
		}
	}
}
