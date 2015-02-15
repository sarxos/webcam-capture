import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sarxos.webcam.Webcam;


@WebSocket
public class WebcamWebSocketHandler {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamWebSocketHandler.class);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private Session session;

	private void teardown() {
		try {
			session.close();
			session = null;
		} finally {
			WebcamCache.unsubscribe(this);
		}
	}

	private void setup(Session session) {

		this.session = session;

		Map<String, Object> message = new HashMap<String, Object>();
		message.put("type", "list");
		message.put("webcams", WebcamCache.getWebcamNames());

		send(message);

		WebcamCache.subscribe(this);
	}

	@OnWebSocketClose
	public void onClose(int status, String reason) {
		LOG.info("WebSocket closed, status = {}, reason = {}", status, reason);
		teardown();
	}

	@OnWebSocketError
	public void onError(Throwable t) {
		LOG.error("WebSocket error", t);
		teardown();
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		LOG.info("WebSocket connect, from = {}", session.getRemoteAddress().getAddress());
		setup(session);
	}

	@OnWebSocketMessage
	public void onMessage(String message) {
		LOG.info("WebSocket message, text = {}", message);
	}

	public void newImage(Webcam webcam, BufferedImage image) {

		LOG.info("New image from {}", webcam);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "JPG", baos);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}

		String base64 = null;
		try {
			base64 = new String(Base64.getEncoder().encode(baos.toByteArray()), "UTF8");
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage(), e);
		}

		Map<String, Object> message = new HashMap<String, Object>();
		message.put("type", "image");
		message.put("webcam", webcam.getName());
		message.put("image", base64);

		send(message);
	}

	private void send(String message) {
		try {
			session.getRemote().sendString(message);
		} catch (IOException e) {
			LOG.error("Exception when sending string", e);
		}
	}

	private void send(Object object) {
		try {
			send(MAPPER.writeValueAsString(object));
		} catch (JsonProcessingException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}