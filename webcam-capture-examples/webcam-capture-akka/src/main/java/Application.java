import static akka.pattern.Patterns.ask;
import static scala.concurrent.Await.result;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.typesafe.config.ConfigFactory;

public class Application {

	static enum GetImageMsg {
		OBJECT;
	}

	static class WebcamActor extends UntypedActor {

		final Webcam webcam;

		public WebcamActor(Webcam webcam) {
			this.webcam = webcam;
		}

		@Override
		public void preStart() throws Exception {
			webcam.setViewSize(WebcamResolution.VGA.getSize());
			webcam.open();
		}

		@Override
		public void postStop() throws Exception {
			webcam.close();
		}

		@Override
		public void onReceive(Object msg) throws Exception {
			if (msg instanceof GetImageMsg) {
				sender().tell(getImage(), self());
			} else {
				unhandled(msg);
			}
		}

		public BufferedImage getImage() {
			return webcam.getImage();
		}
	}

	public static void main(String[] args) throws Exception {

		final ActorSystem system = ActorSystem.create("hello-from-akka", ConfigFactory.defaultApplication());
		final ActorRef ref = system.actorOf(Props.create(WebcamActor.class, Webcam.getDefault()));

		final File file = new File("test.jpg");
		final Duration timeout = FiniteDuration.create("10s");
		final BufferedImage image = (BufferedImage) result(ask(ref, GetImageMsg.OBJECT, timeout.toMillis()), timeout);

		ImageIO.write(image, "JPG", file);

		JOptionPane.showMessageDialog(null, "Image has been saved in file: " + file);

		system.terminate();
	}
}
