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

import java.nio.file.Path;
import java.security.KeyManagementException;
import org.springframework.util.StringUtils;

/**
 *
 * This is a structure which helps you gather your keystore information in an object
 * 
 * @author zg2pro
 */
public class UnloadedKeystore {

    /**
     * depending on the extension of your file p12 or jks
     */
    public enum KeystoreType {
        PKCS12,
        JKS
    }

    private Path keystoreFilepath;
    private String keystorePwd;
    private KeystoreType keystoreType;
    private String keystoreCertPwd;

    protected void checkConsistency() throws KeyManagementException {
        if (keystoreFilepath == null || keystoreType == null
                || StringUtils.isEmpty(keystoreCertPwd)
                || StringUtils.isEmpty(keystorePwd)) {
            throw new KeyManagementException("Your UnloadedKeystore object is not complete, "
                    + "all information must be provided");
        }
    }

    public Path getKeystoreFilepath() {
        return keystoreFilepath;
    }

    public void setKeystoreFilepath(Path keystoreFilepath) {
        this.keystoreFilepath = keystoreFilepath;
    }

    public String getKeystorePwd() {
        return keystorePwd;
    }

    public void setKeystorePwd(String keystorePwd) {
        this.keystorePwd = keystorePwd;
    }

    public KeystoreType getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(KeystoreType keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getKeystoreCertPwd() {
        return keystoreCertPwd;
    }

    public void setKeystoreCertPwd(String keystoreCertPwd) {
        this.keystoreCertPwd = keystoreCertPwd;
    }

}
