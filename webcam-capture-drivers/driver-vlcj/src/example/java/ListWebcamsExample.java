import java.util.List;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.vlcj.VlcjDriver;


/**
 * This class provides a simple example of how to use VLCj driver to list
 * webcams available in the system.<br>
 * <br>
 * 
 * WARNING: It works correctly only in case when used on Linux box. Windows VLCj
 * implementation does not support webcam discovery!!!
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class ListWebcamsExample {

	static {
		Webcam.setDriver(new VlcjDriver());
	}

	public static void main(String[] args) {

		List<Webcam> webcams = Webcam.getWebcams();

		System.out.format("Webcams detected: %d \n", webcams.size());

		for (int i = 0; i < webcams.size(); i++) {
			System.out.format("%d: %s \n", i + 1, webcams.get(i));
		}
	}
}
