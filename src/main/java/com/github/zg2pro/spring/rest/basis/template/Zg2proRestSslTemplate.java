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
package com.github.zg2pro.spring.rest.basis.template;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.zg2pro.spring.rest.basis.logs.LoggingRequestInterceptor;
import com.github.zg2pro.spring.rest.basis.logs.LoggingSslRequestFactoryFactory;
import com.github.zg2pro.spring.rest.basis.ssl.UnloadedKeystore;
import com.github.zg2pro.spring.rest.basis.ssl.Zg2proSslHttpRequestFactoryFactory;
import java.security.KeyManagementException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;

/**
 *
 * Layer over spring's RestTemplate loading automatically the lib utilities
 *
 * @author zg2pro
 */
public class Zg2proRestSslTemplate extends AbstractZg2proRestTemplate {

    @Override
    protected void interceptorsIntegration(
            List<ClientHttpRequestInterceptor> lInterceptors,
            Object sslConfiguration) {
        this.setInterceptors(lInterceptors);
        ClientHttpRequestFactory chrf;
        try {
            chrf = Zg2proSslHttpRequestFactoryFactory.build((UnloadedKeystore) sslConfiguration);
        } catch (KeyManagementException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.setRequestFactory(
                new InterceptingClientHttpRequestFactory(
                        new BufferingClientHttpRequestFactory(chrf),
                        lInterceptors
                )
        );
    }

    public Zg2proRestSslTemplate(UnloadedKeystore keystore) {
        this(keystore, null);
    }

    public Zg2proRestSslTemplate(UnloadedKeystore keystore, SimpleModule sm) {
        super(sm);
        //interceptors
        LoggingRequestInterceptor lri = new LoggingRequestInterceptor();
        this.setInterceptors(new ArrayList<>());
        this.getInterceptors().add(lri);
        this.setRequestFactory(LoggingSslRequestFactoryFactory.build(keystore, lri));
    }

    public Zg2proRestSslTemplate(
            UnloadedKeystore keystore, 
            List<HttpMessageConverter<?>> lConverters, 
            List<ClientHttpRequestInterceptor> lInterceptors) {
        super(lConverters, lInterceptors);
        interceptorsIntegration(lInterceptors, keystore);
    }

}
