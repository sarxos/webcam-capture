package com.lti.civil.impl.jni;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;


/**
 * Original class tries to load it, but we have it already loaded.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class NativeCaptureSystemFactory implements CaptureSystemFactory {

	public CaptureSystem createCaptureSystem() throws CaptureException {
		return newCaptureSystemObj();
	}

	private static native CaptureSystem newCaptureSystemObj();

}
