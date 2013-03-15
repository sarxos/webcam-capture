package us.sosia.video.stream.handler;

import org.jboss.netty.channel.Channel;


public interface StreamClientListener {
	public void onConnected(Channel channel);
	public void onDisconnected(Channel channel);
	public void onException(Channel channel,Throwable t);
}
