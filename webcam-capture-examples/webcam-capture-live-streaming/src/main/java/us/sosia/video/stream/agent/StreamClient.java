package us.sosia.video.stream.agent;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.xuggler.IAudioSamples;

import us.sosia.video.stream.agent.ui.SingleVideoDisplayWindow;
import us.sosia.video.stream.handler.StreamFrameListener;

public class StreamClient {
	/**
	 * @author kerr
	 * */
	private static SourceDataLine mLine;
	private static boolean isFirst = true; 
	private final static Dimension dimension = new Dimension(320,240);
	private final static SingleVideoDisplayWindow displayWindow = new SingleVideoDisplayWindow("Stream example",dimension);
	protected final static Logger logger = LoggerFactory.getLogger(StreamClient.class);
	public static void main(String[] args) {
		//setup the videoWindow
		displayWindow.setVisible(true);
		
		//setup the connection
		logger.info("setup dimension :{}",dimension);
		StreamClientAgent clientAgent = new StreamClientAgent(new StreamFrameListenerIMPL(),dimension);
		clientAgent.connect(new InetSocketAddress("localhost", 20000));
		try {
			openJavaSound();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	protected static class StreamFrameListenerIMPL implements StreamFrameListener{
		private volatile long count = 0;
		@Override
		public void onFrameReceived(BufferedImage image) {
			logger.info("frame received :{}",count++);
			displayWindow.updateImage(image);			
		}
		@Override
		public void onAudioRecieved(IAudioSamples samples) {
			playJavaSound(samples);
			// TODO Auto-generated method stub
			
		}		
	}
	
	private static void openJavaSound() throws LineUnavailableException
	  {
	    AudioFormat audioFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				44100.0F, 16, 2, 4, 44100, false);
	    
	    AudioFormat format = new AudioFormat(44100,
	            16,
	            2,
	            true, /* xuggler defaults to signed 16 bit samples */
	            false);
	    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
	    mLine = (SourceDataLine) AudioSystem.getLine(info);
	    /**
	     * if that succeeded, try opening the line.
	     */
	    mLine.open(audioFormat);
	    /**
	     * And if that succeed, start the line.
	     */
	    mLine.start();
	    
	    
	  }
	
	 private static synchronized void playJavaSound(IAudioSamples aSamples)
	  {
		 if (isFirst) {
			 isFirst = false;
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		 
	    /**
	     * We're just going to dump all the samples into the line.
	     */
	    byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
	    System.out.println("got: " + rawBytes.length );
	    mLine.write(rawBytes, 0, rawBytes.length);	    
	    
	  }
	

}
