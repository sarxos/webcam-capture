import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamStorage;


/**
 * This example demonstrates how webcam capture IP camera driver can be used with conjunction with
 * websockets to feed data to the web application frontend.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamWebSocketsExample {

	static {
		Webcam.setDriver(new IpCamDriver(new IpCamStorage("src/main/resources/cameras.xml")));
	}

	private static final Logger LOG = LoggerFactory.getLogger(WebcamWebSocketsExample.class);

	public static void main(String[] args) throws Exception {

		for (String name : WebcamCache.getWebcamNames()) {
			LOG.info("Will read webcam {}", name);
		}

		Server server = new Server(8123);
		WebSocketHandler wsHandler = new WebSocketHandler() {

			@Override
			public void configure(WebSocketServletFactory factory) {
				factory.register(WebcamWebSocketHandler.class);
			}
		};

		server.setHandler(wsHandler);
		server.start();
		server.join();
	}
}
