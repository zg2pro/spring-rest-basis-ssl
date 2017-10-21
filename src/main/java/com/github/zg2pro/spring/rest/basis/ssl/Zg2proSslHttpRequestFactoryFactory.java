/*
 * The MIT License
 *
 * Copyright 2017 zg2pro.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.zg2pro.spring.rest.basis.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

/**
 *
 * @author zg2pro
 */
public class Zg2proSslHttpRequestFactoryFactory {

    private static KeyStore loadKeystore(Path resourcePath, String password, String storeType)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        InputStream is = new FileInputStream(resourcePath.toFile());
        KeyStore keyStore = KeyStore.getInstance(storeType);
        keyStore.load(is, password.toCharArray());
        return keyStore;
    }

    private static HttpClient buildHttpClient(UnloadedKeystore keystore)
            throws KeyManagementException {
        SSLContext sslContext = null;
        try {
            KeyStore keyStore = loadKeystore(keystore.getKeystoreFilepath(), keystore.getKeystorePwd(),
                    keystore.getKeystoreType().name());
            sslContext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, keystore.getKeystoreCertPwd().toCharArray())
                    .build();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
            throw new KeyManagementException(ex);
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, (String hostname, SSLSession session) -> true);
        return HttpClients.custom()
                .setSSLSocketFactory(sslsf).build();
    }

    public static Zg2proSslHttpRequestFactory build(UnloadedKeystore keystore) throws KeyManagementException {
        keystore.checkConsistency();
        return new Zg2proSslHttpRequestFactory(buildHttpClient(keystore));
    }

}
