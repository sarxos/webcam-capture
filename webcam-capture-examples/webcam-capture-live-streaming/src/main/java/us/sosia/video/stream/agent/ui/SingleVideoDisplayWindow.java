package us.sosia.video.stream.agent.ui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class SingleVideoDisplayWindow {
	protected final VideoPanel videoPannel;
	protected final JFrame window;

	public SingleVideoDisplayWindow(String name,Dimension dimension) {
		super();
		this.window = new JFrame(name);
		this.videoPannel = new VideoPanel();

		this.videoPannel.setPreferredSize(dimension);
		this.window.add(videoPannel);
		this.window.pack();
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setVisible(boolean visible) {
		this.window.setVisible(visible);
	}

	public void updateImage(BufferedImage image) {
		videoPannel.updateImage(image);
	}
	
	public void close(){
		window.dispose();
		videoPannel.close();
	}
}
