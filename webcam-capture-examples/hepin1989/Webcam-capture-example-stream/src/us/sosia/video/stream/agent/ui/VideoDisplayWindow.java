package us.sosia.video.stream.agent.ui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class VideoDisplayWindow {
	protected final DoubleVideoPannel videoPannel;
	protected final JFrame window;

	public VideoDisplayWindow(String name,Dimension dimension) {
		super();
		this.window = new JFrame(name);
		this.videoPannel = new DoubleVideoPannel();

		this.videoPannel.setPreferredSize(dimension);
		this.window.add(videoPannel);
		this.window.pack();
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setVisible(boolean visible) {
		this.window.setVisible(visible);
	}

	public void updateBigVideo(BufferedImage image) {
		videoPannel.updateBigImage(image);
	}

	public void updateSmallVideo(BufferedImage image) {
		videoPannel.updateSmallImage(image);
	}
	
	public void close(){
		window.dispose();
		videoPannel.close();
	}
}
