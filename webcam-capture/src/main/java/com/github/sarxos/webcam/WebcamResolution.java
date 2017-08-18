package com.github.sarxos.webcam;

import java.awt.Dimension;


/**
 * Various resolutions.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public enum WebcamResolution {

	// VGA (Video Graphics Array)

	/**
	 * The Quarter-QVGA resolution with size 176x144 px.
	 */
	QQVGA(176, 144),

	/**
	 * The Half-QVGA resolution with size 176x144 px.
	 */
	HQVGA(240, 160),

	/**
	 * Size 320x240
	 */
	QVGA(320, 240),

	/**
	 * The Wide QVGA resolution with size 400x240 px.
	 */
	WQVGA(400, 240),

	/**
	 * The Half-size VGA with size 480x320 px.
	 */
	HVGA(480, 320),

	/**
	 * Size 640x480 px.
	 */
	VGA(640, 480),

	/**
	 * The Wide VGA resolution with size 768x480 px.
	 */
	WVGA(768, 480),

	/**
	 * The Full Wide VGA with size 854x480 px.
	 */
	FWVGA(854, 480),

	/**
	 * The Super VGA with size 800x600 px.
	 */
	SVGA(800, 600),

	/**
	 * The Double-size VGA resolution with size 960x640 px.
	 */
	DVGA(960, 640),

	/**
	 * The Wide Super VGA resolution, 1st variant, with size 1024x576 px.
	 */
	WSVGA1(1024, 576),

	/**
	 * The Wide Super VGA resolution, 2nd variant, with size 1024x600 px.
	 */
	WSVGA2(1024, 600),

	// XGA (Extended Graphics Array)

	/**
	 * Resolution with size 1024x768 px.
	 */
	XGA(1024, 768),

	/**
	 * The Extended Graphics Array Plus resolution with size 1152x864 px.
	 */
	XGAP(1152, 864),

	/**
	 * The Wide Extended Graphics Array, 1st variant, with size 1366x768 px.
	 */
	WXGA1(1366, 768),

	/**
	 * The Wide Extended Graphics Array, 2nd variant, with size 1280x800 px.
	 */
	WXGA2(1280, 800),

	/**
	 * The Wide Extended Graphics Array Plus resolution with size 1440x900 px.
	 */
	WXGAP(1440, 900),

	/**
	 * The Super XGA resolution with size 1280x1024 px.
	 */
	SXGA(1280, 1024),

	/**
	 * The Super XGA Plus resolution with size 1400x1050 px.
	 */
	SXGAP(1400, 1050),

	/**
	 * The Widescreen Super Extended Graphics Array Plus resolution with size 1680x1050 px.
	 */
	WSXGAP(1680, 1050),

	/**
	 * The Ultra Extended Graphics Array resolution with size 1600x1200 px.
	 */
	UXGA(1600, 1200),

	/**
	 * The Widescreen Ultra Extended Graphics Array resolution with size 1920x1200 px.
	 */
	WUXGA(1920, 1200),

	// QXGA (Quad Extended Graphics Array)

	/**
	 * The Quad Wide Extended Graphics Array resolution with size 2048x1152 px.
	 */
	QWXGA(2048, 1152),

	/**
	 * The Quad Extended Graphics Array resolution with size 2048x1536 px.
	 */
	QXGA(2048, 1536),

	/**
	 * The Wide Quad Extended Graphics Array resolution with size 2560x1600 px.
	 */
	WQXGA(2560, 1600),

	/**
	 * The Quad Super Extended Graphics Array is a display resolution with size 2560×2048 px.
	 */
	QSXGA(2560, 2048),

	/**
	 * The Wide Quad Super Extended Graphics Array resolution with size 3200x2048 px.
	 */
	WQSXGA(3200, 2048),

	/**
	 * The Quad Ultra Extended Graphics Array resolution with size 3200x2400 px.
	 */
	QUXGA(3200, 2400),

	/**
	 * The Wide Quad Ultra Extended Graphics Array resolution with size 3840x2400 px.
	 */
	WQUXGA(3840, 2400),

	// HXGA (Hyper Extended Graphics Array)

	/**
	 * The Hexadecatuple Extended Graphics Array resolution with size 4096x3072 px.
	 */
	HXGA(4096, 3072),

	/**
	 * The Wide Hexadecatuple Extended Graphics Array resolution with size 5120x3200 px.
	 */
	WHXGA(5120, 3200),

	/**
	 * The Hexadecatuple Super Extended Graphics Array resolution with size 5120x4096 px.
	 */
	HSXGA(5120, 4096),

	/**
	 * The Wide Hexadecatuple Super Extended Graphics Array resolution with size 6400x4096 px.
	 */
	WHSXGA(6400, 4096),

	/**
	 * The Hexadecatuple Ultra Extended Graphics Array resolution with size 6400x4800 px.
	 */
	HUXGA(6400, 4800),

	/**
	 * The Wide Hexadecatuple Ultra Extended Graphics Array resolution with size 7680x4800 px.
	 */
	WHUXGA(7680, 4800),

	// HD (High-Definition)

	/**
	 * The nHD resolution with image dimension of 640x360 px.
	 */
	NHD(640, 360),

	/**
	 * The qHD resolution with szie 960x540 px.
	 */
	QHD(960, 540),

	/**
	 * Size 1280x720 also known as HD 720p.
	 */
	HD(1280, 720),

	/**
	 * The HD+ resolution of 1600x900 pixels.
	 */
	HDP(1600, 900),

	/**
	 * The FHD or Full HD resolution of 1920x1080 pixels.
	 */
	FHD(1920, 1080),

	/**
	 * The Full HD+ is a resolution of 2160x1440 pixels.
	 */
	FHDP(2160, 1440),

	/**
	 * DCI 2K, also referred to as Cinema 2K, the resolution with size 2048x1080 px.
	 */
	DCI2K(2048, 1080),

	/**
	 * The Wide Quad HD resolution with size 2560x1440 px.
	 */
	WQHD(2560, 1440),

	/**
	 * The Wide Quad HD resolution with size 3200x1800 px.
	 */
	WQHDP(3200, 1800),

	/**
	 * UWQHD (Ultra-Wide Quad HD), is a display resolution of 3440x1440 pixels.
	 */
	UWQHD(3440, 1440),

	/**
	 * UW4K (Ultra-Wide 4K), is a display resolution of 3840x1600 pixels.
	 */
	UW4K(3840, 1600),

	/**
	 * 4K UHD (Ultra HD), also referred to as UHDTV-1 or 4Kx2K, is a display resolution of 3840x2160
	 * pixels.
	 */
	UHD4K(3840, 2160),

	/**
	 * DCI 4K, also referred to as Cinema 4K or 4Kx2K with size 4096x2160 px.
	 */
	DCI4K(4096, 2160),

	/**
	 * UW5K (Ultra-Wide 5K), is a display resolution of 5120x2160 pixels.
	 */
	UW5K(5120, 2160),

	/**
	 * 5K is a display resolution of 5120x2880 pixels.
	 */
	UHDP5K(5120, 2880),

	/**
	 * UW8K (Ultra-Wide 8K), is a display resolution of 7680x3200 pixels.
	 */
	UW8K(7680, 3200),

	/**
	 * 8K UHD (Ultra HD), also referred to as UHDTV-2, is a display resolution of 7680x4320 pixels.
	 */
	UHD8K(7680, 4320),

	// other

	/**
	 * Size 768x576
	 */
	PAL(768, 576),

	/**
	 * Size 352x288
	 */
	CIF(352, 288),

	;

	/**
	 * Resolution size.
	 */
	private Dimension size = null;

	/**
	 * 
	 * @param width the resolution width
	 * @param height the resolution height
	 */
	private WebcamResolution(int width, int height) {
		this.size = new Dimension(width, height);
	}

	/**
	 * Get resolution size.
	 * 
	 * @return Dimension object
	 */
	public Dimension getSize() {
		return size;
	}

	public int getPixelsCount() {
		return size.width * size.height;
	}

	public Dimension getAspectRatio() {
		final int factor = getCommonFactor(size.width, size.height);
		final int wr = size.width / factor;
		final int hr = size.height / factor;
		return new Dimension(wr, hr);
	}

	private int getCommonFactor(int width, int height) {
		return (height == 0) ? width : getCommonFactor(height, width % height);
	}

	public int getWidth() {
		return size.width;
	}

	public int getHeight() {
		return size.height;
	}

	@Override
	public String toString() {

		final int w = size.width;
		final int h = size.height;
		final Dimension ratio = getAspectRatio();
		final int rw = ratio.width;
		final int rh = ratio.height;

		return new StringBuilder()
			.append(super.toString())
			.append(' ')
			.append(w).append('x').append(h)
			.append(" (")
			.append(rw).append(':').append(rh)
			.append(')')
			.toString();
	}

	public static void main(String[] args) {

	}
}
