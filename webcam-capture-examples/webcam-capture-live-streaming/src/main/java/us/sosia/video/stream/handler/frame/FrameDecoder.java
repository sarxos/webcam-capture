package us.sosia.video.stream.handler.frame;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.HeapChannelBufferFactory;


public class FrameDecoder{
	//protected final ChannelBuffer dataSink = ChannelBuffers.dynamicBuffer(65536);
	protected ChannelBuffer dataSink = ChannelBuffers.dynamicBuffer(65536, new HeapChannelBufferFactory());
	protected final int headLength;
	
	public FrameDecoder(int headLength) {
		super();
		this.headLength = headLength;

	}

	public ChannelBuffer decode(ChannelBuffer channelBuffer) throws Exception {
		dataSink.writeBytes(channelBuffer);
        int actualLengthFieldOffset = dataSink.readerIndex();
        long frameLength;
        switch (headLength) {
	        case 1:
	            frameLength = dataSink.getUnsignedByte(actualLengthFieldOffset);
	            break;
	        case 2:
	            frameLength = dataSink.getUnsignedShort(actualLengthFieldOffset);
	            break;
	        case 3:
	            frameLength = dataSink.getUnsignedMedium(actualLengthFieldOffset);
	            break;
	        case 4:
	            frameLength = dataSink.getUnsignedInt(actualLengthFieldOffset);
	            break;
	        case 8:
	            frameLength = dataSink.getLong(actualLengthFieldOffset);

	            break;
	        default:
	            throw new Error("should not reach here");
        }
		
        
        if (frameLength < 0) {
            dataSink.skipBytes(headLength);
        }
        int frameLengthInt = (int) frameLength;
      //  System.out.println("frame length :"+frameLengthInt);
        final ChannelBuffer frame;
        if (dataSink.readableBytes() >= frameLengthInt + headLength) {
        	dataSink.skipBytes(headLength);
        	frame = ChannelBuffers.buffer(frameLengthInt);
        	dataSink.readBytes(frame);
        	dataSink.discardReadBytes();
		}else{
			return null;
		}
		return frame;
	}



}
