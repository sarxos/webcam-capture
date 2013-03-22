

import java.nio.ByteBuffer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;


public class DifferentFileFormatsExample {

	public static void main(String[] args) {

		Webcam webcam = Webcam.getDefault();

		// save files directly to disk

		// creates test1.bmp
		WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_BMP);
		// creates test1.gif
		WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_GIF);
		// creates test1.jpg
		WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_JPG);
		// creates test1.png
		WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_PNG);
		// creates test1.wbmp
		WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_WBMP);

		// this is equivalent of this code

		// creates test2.bmp
		WebcamUtils.capture(webcam, "test2", "bmp");
		// creates test2.gif
		WebcamUtils.capture(webcam, "test2", "gif");
		// creates test2.jpg
		WebcamUtils.capture(webcam, "test2", "jpg");
		// creates test2.png
		WebcamUtils.capture(webcam, "test2", "png");
		// creates test2.wbmp
		WebcamUtils.capture(webcam, "test2", "wbmp");

		// NOTE !!!
		// you can use any format you want until there is a ImageIO plugin
		// installed which supports it

		// get image as bytes array / bytes buffer

		// save image in JPG format and return as array of bytes
		byte[] bytes = WebcamUtils.getImageBytes(webcam, "jpg");
		System.out.println("Bytes length: " + bytes.length);

		// save image in JPG format and return as byte buffer
		ByteBuffer buffer = WebcamUtils.getImageByteBuffer(webcam, "jpg");
		System.out.println("Buffer length: " + buffer.capacity());
	}
}
