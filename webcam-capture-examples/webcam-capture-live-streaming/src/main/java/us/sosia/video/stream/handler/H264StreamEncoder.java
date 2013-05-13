package us.sosia.video.stream.handler;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.sosia.video.stream.handler.frame.FrameEncoder;

import com.xuggle.ferry.IBuffer;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat.Type;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IStreamCoder.Direction;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

public class H264StreamEncoder extends OneToOneEncoder{
	protected final static Logger logger = LoggerFactory.getLogger(Logger.class);
	protected final IStreamCoder iStreamCoder = IStreamCoder.make(Direction.ENCODING, ICodec.ID.CODEC_ID_H264);
	protected final IStreamCoder iAudioStreamCoder = IStreamCoder.make(Direction.ENCODING, ICodec.ID.CODEC_ID_AAC);
	protected final IPacket iPacket = IPacket.make();
	protected long startTime ;
	protected final Dimension dimension;
	protected final FrameEncoder frameEncoder;
	
	protected final AudioFormat format = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED,
			44100.0F, 16, 2, 4, 44100, false);
	

	public H264StreamEncoder(Dimension dimension,boolean usingInternalFrameEncoder) {
		super();
		this.dimension = dimension;
		if (usingInternalFrameEncoder) {
			frameEncoder = new FrameEncoder(4);
		}else {
			frameEncoder = null;
		}
		initialize();
	}

	private void initialize(){
		//setup
	 	iStreamCoder.setNumPicturesInGroupOfPictures(25);
	 	
		iStreamCoder.setBitRate(200000);
		iStreamCoder.setBitRateTolerance(10000);
		iStreamCoder.setPixelType(Type.YUV420P);
		iStreamCoder.setHeight(dimension.height);
		iStreamCoder.setWidth(dimension.width);
	 	iStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
 		iStreamCoder.setGlobalQuality(0);
		//rate
 		IRational rate = IRational.make(25, 1);
 		iStreamCoder.setFrameRate(rate);
 		//time base
 		//iStreamCoder.setAutomaticallyStampPacketsForStream(true);
 		iStreamCoder.setTimeBase(IRational.make(rate.getDenominator(),rate.getNumerator()));
 		IMetaData codecOptions = IMetaData.make();
 		codecOptions.setValue("tune", "zerolatency");// equals -tune zerolatency in ffmpeg
 		//open it
 		int revl = iStreamCoder.open(codecOptions, null);
 		if (revl < 0) {
			throw new RuntimeException("could not open the coder");
		}
	
 	iStreamCoder.setNumPicturesInGroupOfPictures(25);
 	

 	iAudioStreamCoder.setChannels(2);
 	iAudioStreamCoder.setSampleRate(44100);
 	IRational ratea = IRational.make(44100, 1);
 	/*
 	iAudioStreamCoder.setFrameRate(ratea);
 		//time base
 		//iStreamCoder.setAutomaticallyStampPacketsForStream(true);
 	iAudioStreamCoder.setTimeBase(IRational.make(ratea.getDenominator(),ratea.getNumerator()));
 	*/
 	IMetaData codecOptionsa = IMetaData.make();
 	revl = iAudioStreamCoder.open(codecOptionsa, null);
		if (revl < 0) {
		throw new RuntimeException("could not open the audio coder");
	}
	}
	

	
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		return encode(msg);
	}
	
	public Object encode(Object msg) throws Exception {
		if (msg == null) {
			return null;
		}
		if (!(msg instanceof BufferedImage)) {
			throw new IllegalArgumentException("your need to pass into an bufferedimage");
		}
		logger.info("encode the frame");
		BufferedImage bufferedImage = (BufferedImage)msg;
		//here is the encode
		//convert the image
		BufferedImage convetedImage = ImageUtils.convertToType(bufferedImage, BufferedImage.TYPE_3BYTE_BGR);
		IConverter converter = ConverterFactory.createConverter(convetedImage, Type.YUV420P);
 		//to frame
		long now = System.currentTimeMillis();
		if (startTime == 0) {
				startTime = now;
		}
		IVideoPicture pFrame = converter.toPicture(convetedImage, (now - startTime)*1000);
		//pFrame.setQuality(0);
		iStreamCoder.encodeVideo(iPacket, pFrame, 0) ;
 		//free the MEM
		pFrame.delete();
		converter.delete();
		//write to the container
		iPacket.setStreamIndex(0);
		if (iPacket.isComplete()) {
			iPacket.setFlags(1);
			//iPacket.delete();
			//here we send the package to the remote peer
			try{
				ByteBuffer byteBuffer = iPacket.getByteBuffer();
				if (iPacket.isKeyPacket()) {
					logger.info("key frame");
				}
				ChannelBuffer channelBuffe = ChannelBuffers.copiedBuffer(byteBuffer.order(ByteOrder.BIG_ENDIAN));
				if (frameEncoder != null) {
					System.out.println("using frame encoder");
					return channelBuffe;
					//return frameEncoder.encode(channelBuffe);
				}
				return channelBuffe;

			}finally{
				iPacket.reset();
			}
		}else{
			return null;
		}
	}
	
	public Object encode( byte[] data, int numBytesRead) throws Exception {
		
		//System.out.println("initial data : " + Arrays.toString(data));
		//System.out.println("num bytes: " + numBytesRead + "data size: " + data.length);
		
		//convert the image
		/*
		long now = System.currentTimeMillis();
		if (startTime == 0) {
				startTime = now;
		}
		*/

	    IBuffer iBuf = IBuffer.make(null, data, 0, numBytesRead);

	    IAudioSamples smp = IAudioSamples.make(iBuf,2,IAudioSamples.Format.FMT_S16);
	    smp.setComplete(true, numBytesRead/4, 44100, 2, IAudioSamples.Format.FMT_S16, 0);
	    //System.out.println("sample rate: " + smp.getSampleRate());
	    iAudioStreamCoder.encodeAudio(iPacket, smp, 0);
	    System.out.println("packet size: " + iPacket.getSize());
	    System.out.println("bytes read: " + numBytesRead);

		//write to the container
		if (iPacket.isComplete()) {
			/*
			byte[] bufferdat = new byte[iPacket.getByteBuffer().capacity()];
			iPacket.getByteBuffer().get(bufferdat);
			//System.out.print(Arrays.toString(bufferdat));
		    iPacket.setStreamIndex(1);
		    iPacket.setFlags(2);
		    
		    //System.out.println("trying after");
		    bufferdat = new byte[iPacket.getByteBuffer().capacity()];
		    iPacket.getByteBuffer().get(bufferdat);
			System.out.print(Arrays.toString(bufferdat));
			*/
			 
			//iPacket.delete();
			//here we send the package to the remote peer
			try{
				ByteBuffer byteBuffer = iPacket.getByteBuffer();
				if (iPacket.isKeyPacket()) {
					logger.info("key frame");
				}
				ChannelBuffer channelBuffe = ChannelBuffers.copiedBuffer(byteBuffer.order(ByteOrder.BIG_ENDIAN));
				if (frameEncoder != null) {
					System.out.println("using frame encoder");
					return frameEncoder.encode(channelBuffe);
				}
				return channelBuffe;

			}finally{
				iPacket.reset();
			}
		}else{
			return encode(data,numBytesRead);
		}
	}

}
