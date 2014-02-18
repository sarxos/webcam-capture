package com.github.sarxos.webcam.ds.ipcam.impl;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.ipcam.IpCamAuth;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


@XmlAccessorType(XmlAccessType.FIELD)
public class IpCamDescriptor {

	@XmlAccessorType(XmlAccessType.FIELD)
	protected static class AuthParams {

		@XmlAttribute
		private String user = null;

		@XmlAttribute
		private String password = null;

		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}
	}

	@XmlAttribute
	private String name = null;

	@XmlAttribute(name = "url")
	private String urlString = null;

	private transient URL url = null;

	@XmlAttribute
	private IpCamMode mode = IpCamMode.PULL;

	@XmlElement(name = "auth")
	private AuthParams authParams = null;

	private transient IpCamAuth auth = null;

	public String getName() {
		return name;
	}

	public URL getURL() {
		if (urlString != null && url == null) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException e) {
				throw new WebcamException(e);
			}
		}
		return url;
	}

	public IpCamMode getMode() {
		return mode;
	}

	public IpCamAuth getAuth() {
		if (authParams != null && auth == null) {
			auth = new IpCamAuth(authParams.getUser(), authParams.getPassword());
		}
		return auth;
	}
}
