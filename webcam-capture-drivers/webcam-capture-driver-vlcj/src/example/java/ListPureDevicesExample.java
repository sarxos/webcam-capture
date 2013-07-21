import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.vlcj.VlcjDriver;


/**
 * This class intends to be used only for VLCj Webcam Driver test purpose!
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class ListPureDevicesExample {

	public static void main(String[] args) {
		WebcamDriver driver = new VlcjDriver();
		for (WebcamDevice device : driver.getDevices()) {
			System.out.println(device);
		}
	}
}
