package us.sosia.video.stream.channel;

import java.awt.Dimension;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

import us.sosia.video.stream.handler.StreamServerHandler;
import us.sosia.video.stream.handler.StreamServerListener;

public class StreamServerChannelPipelineFactory implements ChannelPipelineFactory{
	protected final StreamServerListener streamServerListener;
	protected final Dimension dimension;

	public StreamServerChannelPipelineFactory(
			StreamServerListener streamServerListener, Dimension dimension) {
		super();
		this.streamServerListener = streamServerListener;
		this.dimension = dimension;
	}


	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		//comment the netty's frame encoder ,if want to use the build in h264 encoder
		pipeline.addLast("frame encoder", new LengthFieldPrepender(4,false));
		pipeline.addLast("stream server handler", new StreamServerHandler(streamServerListener));
		//add the stream h264 encoder
		//for that we may need to server so many client,so we need to move this out of the pipe line
		//so that we only need to encode only once
		//pipeline.addLast("stream h264 encoder", new H264StreamEncoder(dimension,false));
		return pipeline;
	}

	
}
