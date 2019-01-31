package com.github.sarxos.webcam.ds.raspistill;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Stack;

import javax.imageio.IIOException;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

/**
 * this is experimental! after test, using java imageio is really low in performance. it is almost can not decode images in 1 second on raspberrypi.
 * and also buggy!
 * 
 * default jre uses file cache ImageInputStream which is low performance. this class is used to read sequential images from raspistill stdout
 * this class supposed to support png, gif, bmp and jpg. but there is one bug in oracle jre jpeg image reader,
 * please check <a href="https://github.com/haraldk/TwelveMonkeys/issues/202">12 monkeys issue</a>.
 * 
 * so it is narrowed to only for PNG
 */
public class PNGImagesInputStream implements ImageInputStream {
    /** 
	 * in png images stream each time of reading will leave 12 bytes in buffer, it must be skipped when close
	 */ 
	private static final int PNG_TAILING = 14;
	// Length of the buffer used for readFully(type[], int, int)
    private static final int BYTE_BUF_LENGTH = 8192;
    private Stack<Long> markByteStack = new Stack<Long>();

    private Stack<Integer> markBitStack = new Stack<Integer>();
    private boolean isClosed = false;

    /**
     * The byte order of the stream as an instance of the enumeration
     * class <code>java.nio.ByteOrder</code>, where
     * <code>ByteOrder.BIG_ENDIAN</code> indicates network byte order
     * and <code>ByteOrder.LITTLE_ENDIAN</code> indicates the reverse
     * order.  By default, the value is
     * <code>ByteOrder.BIG_ENDIAN</code>.
     */
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    
    /**
     * The current bit offset within the stream.  Subclasses are
     * responsible for keeping this value current from any method they
     * override that alters the bit offset.
     */
    private int bitOffset;

    /**
     * The position prior to which data may be discarded.  Seeking
     * to a smaller position is not allowed.  <code>flushedPos</code>
     * will always be {@literal >= 0}.
     */
    private long flushedPos = 0;
    
    private final InputStream stream;
    private final ByteBuf buff;
    
    public PNGImagesInputStream(InputStream stream) {
    	this(stream, BYTE_BUF_LENGTH);
    }
    
    public PNGImagesInputStream(InputStream stream, int bufferSize) {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        this.stream = stream;
        //default BIG ENDIAN, because we are read/write frequently, use direct buffer
        buff = Unpooled.directBuffer(bufferSize);
    }
    /**
     * Throws an <code>IOException</code> if the stream has been closed.
     * Subclasses may call this method from any of their methods that
     * require the stream not to be closed.
     *
     * @exception IOException if the stream is closed.
     */
    protected final void checkClosed() throws IOException {
        if (isClosed) {
            throw new IOException("closed");
        }
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }
    
    /**
     * A convenience method that calls <code>read(b, 0, b.length)</code>.
     *
     * <p> The bit offset within the stream is reset to zero before
     * the read occurs.
     *
     * @return the number of bytes actually read, or <code>-1</code>
     * to indicate EOF.
     *
     * @exception NullPointerException if <code>b</code> is
     * <code>null</code>.
     * @exception IOException if an I/O error occurs.
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException("len < 0!");
        }
        if (buf == null) {
            throw new NullPointerException("buf == null!");
        }

        byte[] data = new byte[len];
        len = read(data, 0, len);

        buf.setData(data);
        buf.setOffset(0);
        buf.setLength(len);
    }

    public boolean readBoolean() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (ch != 0);
    }

    public byte readByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte)ch;
    }

    public int readUnsignedByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    public short readShort() throws IOException {
    	this.ensureCacheLoaded(2);
    	if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
    		return buff.readShortLE();
    	}
    	return buff.readShort();
    }

    public int readUnsignedShort() throws IOException {
    	this.ensureCacheLoaded(2);
        return ((int)readShort()) & 0xffff;
    }

    public char readChar() throws IOException {
    	this.ensureCacheLoaded(2);
        return (char)readShort();
    }

    public int readInt() throws IOException {
    	this.ensureCacheLoaded(4);
    	if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
    		return buff.readIntLE();
    	}
    	return buff.readInt();
    }

    public long readUnsignedInt() throws IOException {
        return ((long)readInt()) & 0xffffffffL;
    }

    public long readLong() throws IOException {
    	this.ensureCacheLoaded(8);
    	if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
    		return buff.readLongLE();
    	}
        return buff.readLong();
    }

    public float readFloat() throws IOException {
    	this.ensureCacheLoaded(4);
    	if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
    		return buff.readFloatLE();
    	}
        return buff.readFloat();
    }

    public double readDouble() throws IOException {
    	this.ensureCacheLoaded(8);
    	if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
    		return buff.readDoubleLE();
    	}
        return buff.readDouble();
    }

    public String readLine() throws IOException {
        StringBuilder input = new StringBuilder();
        int c = -1;
        boolean eol = false;

        while (!eol) {
            switch (c = read()) {
            case -1:
            case '\n':
                eol = true;
                break;
            case '\r':
                eol = true;
                long cur = getStreamPosition();
                if ((read()) != '\n') {
                    seek(cur);
                }
                break;
            default:
                input.append((char)c);
                break;
            }
        }

        if ((c == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }

    public String readUTF() throws IOException {
        this.bitOffset = 0;
        // Fix 4494369: method ImageInputStreamImpl.readUTF()
        // does not work as specified (it should always assume
        // network byte order).
        
        //netty default big endian
        //ByteOrder oldByteOrder = getByteOrder();
        //setByteOrder(ByteOrder.BIG_ENDIAN);

        String ret;
        try {
            ret = DataInputStream.readUTF(this);
        } catch (IOException e) {
            // Restore the old byte order even if an exception occurs
            //setByteOrder(oldByteOrder);
            throw e;
        }
        
        //setByteOrder(oldByteOrder);
        return ret;
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > b.length!");
        }
        
        int actuallLoaded=this.ensureCacheLoaded(len);
        if(actuallLoaded<len) {
        	throw new EOFException();
        }
        buff.readBytes(b, off, len);
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
        System.out.println(ByteUtil.toHexString(b));
    }

    public void readFully(short[] s, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > s.length!");
        }
        int actualLoaded=this.ensureCacheLoaded(len<<1);//ensure loaded to cache
        for(int i=0;i<(actualLoaded>>1);i++) {
        	s[i+off]=this.readShort();
        }
    }

    public void readFully(char[] c, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > c.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > c.length!");
        }
        int actualLoaded=this.ensureCacheLoaded(len<<1);//ensure loaded to cache
        for(int i=0;i<(actualLoaded>>1);i++) {
        	c[i+off]=this.readChar();
        }
    }

    public void readFully(int[] ia, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > ia.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > i.length!");
        }
        int actualLoaded=this.ensureCacheLoaded(len<<2);//ensure loaded to cache
        for(int i=0;i<(actualLoaded>>2);i++) {
        	ia[i+off]=this.readInt();
        }
    }

    public void readFully(long[] l, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > l.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > l.length!");
        }
        int actualLoaded=this.ensureCacheLoaded(len<<3);//ensure loaded to cache
        for(int i=0;i<(actualLoaded>>3);i++) {
        	l[i+off]=this.readLong();
        }
    }

    public void readFully(float[] f, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > f.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > f.length!");
        }
        int actualLoaded=this.ensureCacheLoaded(len<<2);//ensure loaded to cache
        for(int i=0;i<(actualLoaded>>2);i++) {
        	f[i+off]=this.readFloat();
        }
    }

    public void readFully(double[] d, int off, int len) throws IOException {
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > d.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off + len > d.length!");
        }

        int actualLoaded=this.ensureCacheLoaded(len<<3);//ensure loaded to cache
        for(int i=0;i<(actualLoaded>>3);i++) {
        	d[i+off]=this.readDouble();
        }
    }


    public int getBitOffset() throws IOException {
        checkClosed();
        return bitOffset;
    }

    public void setBitOffset(int bitOffset) throws IOException {
        checkClosed();
        if (bitOffset < 0 || bitOffset > 7) {
            throw new IllegalArgumentException("bitOffset must be betwwen 0 and 7!");
        }
        this.bitOffset = bitOffset;
    }

    public int readBit() throws IOException {
        checkClosed();

        // Compute final bit offset before we call read() and seek()
        int newBitOffset = (this.bitOffset + 1) & 0x7;

        int val = buff.getByte(buff.readerIndex());
        
        if (val == -1) {
            throw new EOFException();
        }

        if (newBitOffset != 0) {
            // Shift the bit to be read to the rightmost position
            val >>= 8 - newBitOffset;
        }
        this.bitOffset = newBitOffset;

        return val & 0x1;
    }

    public long readBits(int numBits) throws IOException {
        checkClosed();

        if (numBits < 0 || numBits > 64) {
            throw new IllegalArgumentException();
        }
        if (numBits == 0) {
            return 0L;
        }

        // Have to read additional bits on the left equal to the bit offset
        int bitsToRead = numBits + bitOffset;

        // Compute final bit offset before we call read() and seek()
        int newBitOffset = (this.bitOffset + numBits) & 0x7;

        // Read a byte at a time, accumulate
        long accum = 0L;
        while (bitsToRead > 0) {
            int val = read();
            if (val == -1) {
                throw new EOFException();
            }

            accum <<= 8;
            accum |= val;
            bitsToRead -= 8;
        }

        //there is one byte read ahead, then push back!
        // Move byte position back if in the middle of a byte
        if (newBitOffset != 0) {
            seek(getStreamPosition() - 1);
        }
        this.bitOffset = newBitOffset;

        // Shift away unwanted bits on the right.
        accum >>>= (-bitsToRead); // Negative of bitsToRead == extra bits read

        // Mask out unwanted bits on the left
        accum &= (-1L >>> (64 - numBits));

        return accum;
    }

    /**
     * Returns <code>-1L</code> to indicate that the stream has unknown
     * length.  Subclasses must override this method to provide actual
     * length information.
     *
     * @return -1L to indicate unknown length.
     */
    public long length() {
        return -1L;
    }

    /**
     * Advances the current stream position by calling
     * <code>seek(getStreamPosition() + n)</code>.
     *
     * <p> The bit offset is reset to zero.
     *
     * @param n the number of bytes to seek forward.
     *
     * @return an <code>int</code> representing the number of bytes
     * skipped.
     *
     * @exception IOException if <code>getStreamPosition</code>
     * throws an <code>IOException</code> when computing either
     * the starting or ending position.
     */
    public int skipBytes(int n) throws IOException {
    	this.ensureCacheLoaded(n);
        long pos = getStreamPosition();
        seek(pos + n);
        return (int)(getStreamPosition() - pos);
    }

    /**
     * Advances the current stream position by calling
     * <code>seek(getStreamPosition() + n)</code>.
     *
     * <p> The bit offset is reset to zero.
     *
     * @param n the number of bytes to seek forward.
     *
     * @return a <code>long</code> representing the number of bytes
     * skipped.
     *
     * @exception IOException if <code>getStreamPosition</code>
     * throws an <code>IOException</code> when computing either
     * the starting or ending position.
     */
    public long skipBytes(long n) throws IOException {
    	if(n>Integer.MAX_VALUE) {
    		throw new RuntimeException();
    	}
    	this.ensureCacheLoaded((int)n);
        long pos = getStreamPosition();
        seek(pos + n);
        return getStreamPosition() - pos;
    }

    public void flushBefore(long pos) throws IOException {
    	if(pos>Integer.MAX_VALUE) {
    		throw new RuntimeException();
    	}
    	
        checkClosed();
        if (pos < flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }
        if (pos > getStreamPosition()) {
            throw new IndexOutOfBoundsException("pos > getStreamPosition()!");
        }
        // Invariant: flushedPos >= 0
        flushedPos = pos;
    }

    public void flush() throws IOException {
        flushBefore(getStreamPosition());
    }

    public long getFlushedPosition() {
        return flushedPos;
    }

    /**
     * Default implementation returns false.  Subclasses should
     * override this if they cache data in a temporary file.
     */
    public boolean isCachedFile() {
        return false;
    }
    /**
     * to read many images from raspberry pi process inputstream.
     * @see javax.imageio.stream.ImageInputStream#close()
     */
    public void close() throws IOException {
    	this.ensureCacheLoaded(PNG_TAILING);
    	this.skipBytes(PNG_TAILING);
        buff.discardReadBytes();
    }
    
    /**
     * Finalizes this object prior to garbage collection.  The
     * <code>close</code> method is called to close any open input
     * source.  This method should not be called from application
     * code.
     *
     * @exception Throwable if an error occurs during superclass
     * finalization.
     */
    protected void finalize() throws Throwable {
        if (!isClosed) {
            try {
                close();
            } catch (IOException e) {
            }
        }
        ReferenceCountUtil.release(buff);
        super.finalize();
    }
    public int read() throws IOException {
        checkClosed();
        bitOffset = 0;
        ensureCacheLoaded(1);
        int byt = (buff.readByte()&0xff);
        return byt;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        checkClosed();
        if (b == null) {
            throw new NullPointerException("b == null!");
        }
        // Fix 4430357 - if off + len < 0, overflow occurred
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                ("off < 0 || len < 0 || off+len > b.length || off+len < 0!");
        }
        bitOffset = 0;
        
        if (len == 0) {
            return 0;
        }

        int espactedLength=len;
        int actualLength=ensureCacheLoaded(espactedLength);
        
        buff.readBytes(b, off, espactedLength);
        return actualLength;
    }

    /**
     * Pushes the current stream position onto a stack of marked
     * positions.
     */
    public void mark() {
        try {
            markByteStack.push(Long.valueOf(getStreamPosition()));
            markBitStack.push(Integer.valueOf(getBitOffset()));
        } catch (IOException e) {
        }
    }

    public long getStreamPosition() throws IOException {
        checkClosed();
        return buff.readerIndex();
    }
    
    /**
     * Resets the current stream byte and bit positions from the stack
     * of marked positions.
     *
     * <p> An <code>IOException</code> will be thrown if the previous
     * marked position lies in the discarded portion of the stream.
     *
     * @exception IOException if an I/O error occurs.
     */
    public void reset() throws IOException {
        if (markByteStack.empty()) {
        	//writer index is going too far
    		if(buff.readerIndex()>(BYTE_BUF_LENGTH>>2)) {
    			int offset = buff.readerIndex();
    			buff.discardReadBytes();//TODO performance GOOD?
    			//reset marks
    			if(!markByteStack.isEmpty()) {
        			Stack<Long> temp=new Stack<Long>();
        			for(Long mark:markByteStack) {
        				temp.push(mark-offset);
        			}
        			markByteStack=temp;//change marked position
    			}
    		}
            return;
        }
        
        long pos = ((Long)markByteStack.pop()).longValue();
        if (pos < flushedPos) {
            throw new IIOException
                ("Previous marked position has been discarded!");
        }
        seek(pos);
        
        int offset = ((Integer)markBitStack.pop()).intValue();
        setBitOffset(offset);
    }
    
    @Override
    public void seek(long pos) throws IOException {
        checkClosed();

        // This test also covers pos < 0
        if (pos < flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }

        buff.readerIndex((int)pos);
        this.bitOffset = 0;
    }

    /**
     * Returns <code>true</code> since this
     * <code>ImageInputStream</code> caches data in order to allow
     * seeking backwards.
     *
     * @return <code>true</code>.
     *
     * @see #isCachedMemory
     * @see #isCachedFile
     */
    public boolean isCached() {
        return true;
    }
    /**
     * Returns <code>true</code> since this
     * <code>ImageInputStream</code> is cache buffer in netty bytebuf
     * cache.
     *
     * @return <code>true</code>.
     *
     * @see #isCached
     */
    public boolean isCachedMemory() {
        return true;
    }
    /**
     * ensure data loaded to cache, 
     * @return how many bytes loaded to cache
     */
    protected int ensureCacheLoaded(int miniRequied) throws IOException {
    	if(buff.readableBytes()<miniRequied) {
    		
    		//cache new data
            for(int i=0;i<miniRequied;i++) {
            	int byt=stream.read();
            	if(byt==-1) {
            		return i+1;
            	}
            	buff.writeByte(byt);
            }
            
            return miniRequied;
    	}
    	return miniRequied;//
    }
    
}