package us.sosia.video.stream.agent;

import java.net.SocketAddress;

public interface IStreamServerAgent {
	void start(SocketAddress streamAddress);
	void stop();
}
