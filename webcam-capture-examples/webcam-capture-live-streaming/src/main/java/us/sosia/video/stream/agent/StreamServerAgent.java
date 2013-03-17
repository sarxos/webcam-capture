package us.sosia.video.stream.agent;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.sosia.video.stream.channel.StreamServerChannelPipelineFactory;
import us.sosia.video.stream.handler.H264StreamEncoder;
import us.sosia.video.stream.handler.StreamServerListener;

import com.github.sarxos.webcam.Webcam;

public class StreamServerAgent implements IStreamServerAgent{
	protected final static Logger logger = LoggerFactory.getLogger(StreamServer.class);
	protected final Webcam webcam;
	protected final Dimension dimension;
	protected final ChannelGroup channelGroup = new DefaultChannelGroup();
	protected final ServerBootstrap serverBootstrap;
	//I just move the stream encoder out of the channel pipeline for the performance
	protected final H264StreamEncoder h264StreamEncoder;
	protected volatile boolean isStreaming;
	protected ScheduledExecutorService timeWorker;
	protected ExecutorService encodeWorker;
	protected int FPS = 25;
	protected ScheduledFuture<?> imageGrabTaskFuture;
	public StreamServerAgent(Webcam webcam, Dimension dimension) {
		super();
		this.webcam = webcam;
		this.dimension = dimension;
		//this.h264StreamEncoder = new H264StreamEncoder(dimension,false);
		this.serverBootstrap = new ServerBootstrap();
		this.serverBootstrap.setFactory(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		this.serverBootstrap.setPipelineFactory(new StreamServerChannelPipelineFactory(
				new StreamServerListenerIMPL(),
				dimension));
		this.timeWorker = new ScheduledThreadPoolExecutor(1);
		this.encodeWorker = Executors.newSingleThreadExecutor();
		this.h264StreamEncoder = new H264StreamEncoder(dimension, false);
	}	
	
	
	
	public int getFPS() {
		return FPS;
	}

	public void setFPS(int fPS) {
		FPS = fPS;
	}

	@Override
	public void start(SocketAddress streamAddress) {
		logger.info("Server started :{}",streamAddress);
		Channel channel = serverBootstrap.bind(streamAddress);
		channelGroup.add(channel);
	}
	
	@Override
	public void stop() {
		logger.info("server is stoping");
		channelGroup.close();
		timeWorker.shutdown();
		encodeWorker.shutdown();
		serverBootstrap.releaseExternalResources();
	}
	
	
	private class StreamServerListenerIMPL implements StreamServerListener{

		@Override
		public void onClientConnectedIn(Channel channel) {
			//here we just start to stream when the first client connected in
			//
			channelGroup.add(channel);
			if (!isStreaming) {
				//do some thing
				Runnable imageGrabTask = new ImageGrabTask();
				ScheduledFuture<?> imageGrabFuture = 
						timeWorker.scheduleWithFixedDelay(imageGrabTask,
						0,
						1000/FPS,
						TimeUnit.MILLISECONDS);
				imageGrabTaskFuture = imageGrabFuture;
				isStreaming = true;
			}
			logger.info("current connected clients :{}",channelGroup.size());
		}

		@Override
		public void onClientDisconnected(Channel channel) {
			channelGroup.remove(channel);
			int size = channelGroup.size();
			logger.info("current connected clients :{}",size);
			if (size == 1) {
				//cancel the task
				imageGrabTaskFuture.cancel(false);
				webcam.close();
				isStreaming = false;
			}
		}

		@Override
		public void onExcaption(Channel channel, Throwable t) {
			channelGroup.remove(channel);
			channel.close();
			int size = channelGroup.size();
			logger.info("current connected clients :{}",size);
			if (size == 1) {
				//cancel the task
				imageGrabTaskFuture.cancel(false);
				webcam.close();
				isStreaming = false;
			
		}
		
	}
	
	protected volatile long frameCount = 0;
	
	private class ImageGrabTask implements Runnable{

		@Override
		public void run() {
			logger.info("image grabed ,count :{}",frameCount++);
			BufferedImage bufferedImage = webcam.getImage();
			/**
			 * using this when the h264 encoder is added to the pipeline
			 * */
			//channelGroup.write(bufferedImage);
			/**
			 * using this when the h264 encoder is inside this class
			 * */
			encodeWorker.execute(new EncodeTask(bufferedImage));
		}
		
	}
	
	private class EncodeTask implements Runnable{
		private final BufferedImage image;
		
		public EncodeTask(BufferedImage image) {
			super();
			this.image = image;
		}

		@Override
		public void run() {
			try {
				Object msg = h264StreamEncoder.encode(image);
				if (msg != null) {
					channelGroup.write(msg);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	
	}
	
	
	
	

}
