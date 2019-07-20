package us.sosia.video.stream.handler;

import org.jboss.netty.channel.Channel;

public interface StreamServerListener {
	void onClientConnectedIn(Channel channel);
	void onClientDisconnected(Channel channel);
	void onExcaption(Channel channel, Throwable t);
}
