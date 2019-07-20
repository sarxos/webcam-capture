package us.sosia.video.stream.handler;

import org.jboss.netty.channel.Channel;


public interface StreamClientListener {
	void onConnected(Channel channel);
	void onDisconnected(Channel channel);
	void onException(Channel channel, Throwable t);
}
