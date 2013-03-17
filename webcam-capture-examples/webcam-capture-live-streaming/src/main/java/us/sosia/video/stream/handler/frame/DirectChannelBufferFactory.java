package us.sosia.video.stream.handler.frame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;

public class DirectChannelBufferFactory implements ChannelBufferFactory{
	
	@Override
	public ChannelBuffer getBuffer(int capacity) {
 		return ChannelBuffers.directBuffer(capacity);
	}

	@Override
	public ChannelBuffer getBuffer(ByteOrder endianness, int capacity) {
 		return ChannelBuffers.directBuffer(endianness, capacity);
	}

	@Override
	public ChannelBuffer getBuffer(byte[] array, int offset, int length) {
		ChannelBuffer channelBuffer = ChannelBuffers.directBuffer(length);
		channelBuffer.writeBytes(array, offset, length);
		return channelBuffer;
	}

	@Override
	public ChannelBuffer getBuffer(ByteOrder endianness, byte[] array,
			int offset, int length) {
		ChannelBuffer channelBuffer = ChannelBuffers.directBuffer(endianness,length);
		channelBuffer.writeBytes(array, offset, length);
 		return channelBuffer;
	}

	@Override
	public ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
		int size = nioBuffer.capacity();
		ChannelBuffer channelBuffer = ChannelBuffers.directBuffer(nioBuffer.order(), size);
		channelBuffer.writeBytes(nioBuffer);
		return channelBuffer;
	}

	@Override
	public ByteOrder getDefaultOrder() {
 		return ByteOrder.BIG_ENDIAN;
	}

}
