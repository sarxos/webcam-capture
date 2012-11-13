package com.github.sarxos.webcam.ds.ipcam;

import org.apache.http.auth.UsernamePasswordCredentials;


public class IpCamAuth extends UsernamePasswordCredentials {

	private static final long serialVersionUID = 807247154917333425L;

	public IpCamAuth(String user, String password) {
		super(user, password);
	}
}
