// CustomTrustManager.java

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

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

public class CustomTrustManager implements X509TrustManager {
	protected boolean acceptAny = false;
	//protected KeyStore ks;
	protected X509TrustManager dflt;
	protected X509TrustManager cstm;
	
	protected X509Certificate[] lastChain;
	
	protected CustomTrustManager() {
	}
	
	public static CustomTrustManager credulousTrustManager() {
		CustomTrustManager result = new CustomTrustManager();
		result.acceptAny = true;
		return result;
	}
	
	public CustomTrustManager(KeyStore customKeyStore) {
		try {
			TrustManagerFactory factory = TrustManagerFactory.getInstance(CustomSSLSocketFactory.proto);
			factory.init((KeyStore)null);
			dflt = (X509TrustManager)factory.getTrustManagers()[0];
			factory.init(customKeyStore);
			cstm = (X509TrustManager)factory.getTrustManagers()[0];
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		lastChain = chain;
		if(acceptAny) return;
		try {
			cstm.checkClientTrusted(chain, authType);
		} catch (CertificateException e) {
			dflt.checkClientTrusted(chain, authType);
		}
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		lastChain = chain;
		if(acceptAny) return;
		try {
			cstm.checkServerTrusted(chain, authType);
		} catch (CertificateException e) {
			dflt.checkServerTrusted(chain, authType);
		}
	}
	
	public X509Certificate[] getLastChain() {
		return lastChain;
	}

	public X509Certificate[] getAcceptedIssuers() {
		if(acceptAny) return null;
		X509Certificate[] dfltIssuers = dflt.getAcceptedIssuers();
		X509Certificate[] cstmIssuers = cstm.getAcceptedIssuers();
		if(cstmIssuers == null || cstmIssuers.length == 0)
			return dfltIssuers;
		if(dfltIssuers == null || dfltIssuers.length == 0)
			return cstmIssuers;
		X509Certificate[] result = new X509Certificate[dfltIssuers.length + cstmIssuers.length];
		for (int i = 0; i < dfltIssuers.length; i++) {
			result[i] = dfltIssuers[i];
		}
		for (int i = 0; i < cstmIssuers.length; i++) {
			result[dfltIssuers.length + i] = cstmIssuers[i];
		}
		return result;
	}

}
