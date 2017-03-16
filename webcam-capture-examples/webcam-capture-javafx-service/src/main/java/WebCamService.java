import java.awt.Dimension;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class WebCamService extends Service<Image> {

	private final Webcam cam ;
	
	private final WebcamResolution resolution ;
	
	public WebCamService(Webcam cam, WebcamResolution resolution) {
		this.cam = cam ;
		this.resolution = resolution;
		cam.setCustomViewSizes(new Dimension[] {resolution.getSize()});
		cam.setViewSize(resolution.getSize());
	}
	
	public WebCamService(Webcam cam) {
		this(cam, WebcamResolution.QVGA);
	}
	
	@Override
	public Task<Image> createTask() {
		return new Task<Image>() {
			@Override
			protected Image call() throws Exception {
				
				try {
					cam.open();
					while (!isCancelled()) {
						if (cam.isImageNew()) {
							BufferedImage bimg = cam.getImage();
							updateValue(SwingFXUtils.toFXImage(bimg, null));
						}
					}
					System.out.println("Cancelled, closing cam");
					cam.close();
					System.out.println("Cam closed");
					return getValue();
				} finally {
					cam.close();
				}
			}
			
		};
	}
	

	public int getCamWidth() {
		return resolution.getSize().width ;
	}
	
	public int getCamHeight() {
		return resolution.getSize().height ;
	}
		
}

