// CustomSSLSocketFactory.java

/*
 * Copyright (c) 2008, Gennady & Michael Kushnir
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * 	•	Redistributions of source code must retain the above copyright notice, this
 * 		list of conditions and the following disclaimer.
 * 	•	Redistributions in binary form must reproduce the above copyright notice,
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
 * 		to endorse or promote products derived from this software without specific 
 * 		prior written permission.
 * 		
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.rujel.reusables.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.*;
import javax.net.ssl.*;

import net.rujel.reusables.SettingsReader;

public class CustomSSLSocketFactory extends SSLSocketFactory {
	protected static SSLContext ctx;
	protected SSLSocketFactory sf;
	public static final String proto = "SunX509";

	protected CustomSSLSocketFactory (SSLSocketFactory toUse) {
		sf = toUse;
	}

	public static void reset() {
		ctx = null;
	}

	public static void initSSLContext(SSLContext context, SettingsReader prefs) 
			throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
				, IOException, CertificateException, KeyManagementException{
		KeyManagerFactory kf = KeyManagerFactory.getInstance(proto);
		kf.init(null, null);

		TrustManager tm = null;
		if(prefs.getBoolean("trustAny", false)) {
			tm = CustomTrustManager.credulousTrustManager();
		} else {
			String ksParam = prefs.get("customKeytstore", "");
			FileInputStream ksFile = new FileInputStream(ksParam);
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ksParam = prefs.get("keystorePassphrase", null);
			ks.load(ksFile, (ksParam==null)?null:ksParam.toCharArray());
			tm = new CustomTrustManager(ks);
		}
		context.init(kf.getKeyManagers(), new TrustManager[] {tm}, null);
	}
	
	public static SocketFactory getDefault() {
		if(ctx == null) {
			SettingsReader prefs = SettingsReader.settingsForPath("ssl", false);
			if(prefs == null)
				prefs = SettingsReader.DUMMY;
			try {
				String protocol = prefs.get("protocol", "SSL");
				ctx = SSLContext.getInstance(protocol);
				initSSLContext(ctx, prefs);
			} catch (Exception e) {
				throw new RuntimeException("Error generating cutom socket factory",e);
			}
		}
		SSLSocketFactory gen = ctx.getSocketFactory();
		return new CustomSSLSocketFactory(gen);
	}

	public Socket createSocket(Socket s, String host, int port, boolean autoClose)
	throws IOException {
		return sf.createSocket(s, host, port, autoClose);
	}

	public String[] getDefaultCipherSuites() {
		return sf.getDefaultCipherSuites();
	}

	public String[] getSupportedCipherSuites() {
		return sf.getSupportedCipherSuites();
	}

	public Socket createSocket(String host, int port) throws IOException,
	UnknownHostException {
		return sf.createSocket(host, port);
	}

	public Socket createSocket(InetAddress host, int port) throws IOException {
		return sf.createSocket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress localHost,
			int localPort) throws IOException, UnknownHostException {
		return sf.createSocket(host, port, localHost, localPort);
	}

	public Socket createSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort) throws IOException {
		return sf.createSocket(address, port, localAddress, localPort);
	}

}
