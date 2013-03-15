package us.sosia.video.stream.handler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.sosia.video.stream.handler.frame.FrameDecoder;

public class StreamClientHandler extends SimpleChannelHandler{
	protected final StreamClientListener streamClientListener;
	protected final static Logger logger = LoggerFactory.getLogger(StreamClientHandler.class);
	protected final FrameDecoder frameDecoder = new FrameDecoder(4);
	public StreamClientHandler(StreamClientListener streamClientListener) {
		super();
		this.streamClientListener = streamClientListener;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		Throwable t = e.getCause();
		logger.debug("exception at :{}",channel);
		streamClientListener.onException(channel, t);
		//super.exceptionCaught(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		logger.info("channel connected at {}",channel);
		streamClientListener.onConnected(channel);
		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		Channel channel = e.getChannel();
		logger.info("channel disconnected at :{}",channel);
		streamClientListener.onDisconnected(channel);
		super.channelDisconnected(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		ChannelBuffer channelBuffer = (ChannelBuffer)e.getMessage();
		logger.info("message received :{}",channelBuffer.readableBytes());
		super.messageReceived(ctx, e);
	}
	
	
	
	
	
}
