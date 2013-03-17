package us.sosia.video.stream.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamServerHandler extends SimpleChannelHandler{
	protected final StreamServerListener streamServerListener;
	protected final static Logger logger = LoggerFactory.getLogger(StreamServerHandler.class);
	
	

	public StreamServerHandler(StreamServerListener streamServerListener) {
		super();
		this.streamServerListener = streamServerListener;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		Throwable t = e.getCause();
		logger.debug("exception caught at :{},exception :{}",channel,t);
		streamServerListener.onExcaption(channel, t);
		//super.exceptionCaught(ctx, e);
	}
	
	

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		logger.info("channel connected :{}",channel);
		streamServerListener.onClientConnectedIn(channel);
		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		Channel channel = e.getChannel();
		logger.info("channel disconnected :{}",channel);
		streamServerListener.onClientDisconnected(channel);
		super.channelDisconnected(ctx, e);
	}

	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		long size = e.getWrittenAmount();
		logger.info("frame send at :{} size :{}",channel,size);
		super.writeComplete(ctx, e);
	}
	
	
	
	
}
