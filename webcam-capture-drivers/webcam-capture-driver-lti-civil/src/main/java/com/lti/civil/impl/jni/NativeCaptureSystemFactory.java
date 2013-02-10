package com.lti.civil.impl.jni;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;


/**
 * Original class tries to load native library, but we have it already loaded,
 * so here I just replaced original by the updated one. Please note that it's
 * not very good w/a, but I didn't see any other choice.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class NativeCaptureSystemFactory implements CaptureSystemFactory {

	@Override
	public CaptureSystem createCaptureSystem() throws CaptureException {
		return newCaptureSystemObj();
	}

	private static native CaptureSystem newCaptureSystemObj();

}
