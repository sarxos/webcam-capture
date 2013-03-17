package us.sosia.video.stream.handler.frame;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class FrameEncoder{
	protected final int headLength;
	
	public FrameEncoder(int headLength) {
		super();
		this.headLength = headLength;
	}


	public ChannelBuffer encode(ChannelBuffer channelBuffer) throws Exception {
		int length = channelBuffer.readableBytes();
		//System.out.println("message length :"+length);
		ChannelBuffer header = ChannelBuffers.buffer(headLength);
		//System.out.println(channelBuffer.order());
		switch (headLength) {
	        case 1:
	            if (length >= 256) {
	                throw new IllegalArgumentException(
	                        "length does not fit into a byte: " + length);
	            }
	            header.writeByte((byte) length);
	            break;
	        case 2:
	            if (length >= 65536) {
	                throw new IllegalArgumentException(
	                        "length does not fit into a short integer: " + length);
	            }
	            header.writeShort((short) length);
	            break;
	        case 3:
	            if (length >= 16777216) {
	                throw new IllegalArgumentException(
	                        "length does not fit into a medium integer: " + length);
	            }
	            header.writeMedium(length);
	            break;
	        case 4:
	            header.writeInt(length);
	            break;
	        case 8:
	            header.writeLong(length);
	            break;
	        default:
	            throw new Error("should not reach here");
	        }
 		return ChannelBuffers.wrappedBuffer(header,channelBuffer);
	}
	
}
