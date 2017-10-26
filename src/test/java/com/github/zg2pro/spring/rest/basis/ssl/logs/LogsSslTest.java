package com.github.zg2pro.spring.rest.basis.ssl.logs;

import com.github.zg2pro.spring.rest.basis.MockedControllers;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET_LONG_REPLY;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET_STRUCTURE;
import com.github.zg2pro.spring.rest.basis.ReturnedStructure;
import com.github.zg2pro.spring.rest.basis.exceptions.RestTemplateErrorHandler;
import com.github.zg2pro.spring.rest.basis.logs.LoggingRequestFactoryFactory;
import com.github.zg2pro.spring.rest.basis.logs.LoggingRequestInterceptor;
import com.github.zg2pro.spring.rest.basis.logs.LoggingSslRequestFactoryFactory;
import com.github.zg2pro.spring.rest.basis.logs.LogsTest;
import com.github.zg2pro.spring.rest.basis.ssl.UnloadedKeystore;
import com.github.zg2pro.spring.rest.basis.ssl.Zg2proSslHttpRequestFactoryFactory;
import com.github.zg2pro.spring.rest.basis.template.Zg2proRestSslTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;

/**
 *
 * @author zg2pro
 */
public class LogsSslTest extends LogsTest {

    @Autowired
    private TestRestTemplate rt;

    @Test(expected = IllegalArgumentException.class)
    public void constructions3() {
        UnloadedKeystore keystore = new UnloadedKeystore();
        keystore.setKeystoreCertPwd("testpass");
        keystore.setKeystoreFilepath(new File("/home/userlambda/etc/keystore.jks").toPath());
        new Zg2proRestSslTemplate(keystore);
    }

    private UnloadedKeystore testKeystore() {
        ClassPathResource cpr = new ClassPathResource(
                "com/github/zg2pro/spring/rest/basis/ssl/test-keystore.jks",
                LogsSslTest.class.getClassLoader()
        );
        UnloadedKeystore keystore = new UnloadedKeystore();
        keystore.setKeystoreCertPwd("testpass");
        try {
            keystore.setKeystoreFilepath(cpr.getFile().toPath());
        } catch (IOException ex) {
            fail("we should have found this file");
        }
        keystore.setKeystorePwd("testpass");
        keystore.setKeystoreType(UnloadedKeystore.KeystoreType.JKS);
        return keystore;
    }

    @Test
    public void testInterceptor() {
        UnloadedKeystore keystore = testKeystore();
        LoggingSslRequestFactoryFactory.build(keystore);
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        //spring boot default log level is info
        lInterceptors.add(new LoggingRequestInterceptor(StandardCharsets.ISO_8859_1, 100, Level.ERROR));
        ClientHttpRequestFactory chrf;
        try {
            chrf = Zg2proSslHttpRequestFactoryFactory.build(keystore);
        } catch (KeyManagementException ex) {
            throw new IllegalArgumentException(ex);
        }
        rt.getRestTemplate().setRequestFactory(new InterceptingClientHttpRequestFactory(
                new BufferingClientHttpRequestFactory(chrf),
                lInterceptors
        ));
        ResponseEntity<String> resp = rt.getForEntity(MockedControllers.TEST_URL_GET, String.class);
        assertThat(resp.getBody()).isEqualTo(MockedControllers.TEST_RETURN_VALUE);
    }

    @Test
    public void testsWithLogLevels() {
        ResponseEntity<String> resp;
        UnloadedKeystore keystore = testKeystore();
        for (Level l : Level.values()) {
            rt.getRestTemplate().setRequestFactory(
                    LoggingSslRequestFactoryFactory.build(keystore, new LoggingRequestInterceptor(StandardCharsets.UTF_8, 1000, l))
            );
            for (String str : new String[]{TEST_URL_GET_LONG_REPLY, TEST_URL_GET}) {
                resp = rt.getForEntity(str, String.class);
                assertThat(resp.getBody()).isNotNull();
            }
        }
    }

    @Test
    public void testZg2Template() {
        UnloadedKeystore keystore = testKeystore();
        Zg2proRestSslTemplate z0 = new Zg2proRestSslTemplate(keystore);
        assertThat(z0.getErrorHandler()).isInstanceOf(RestTemplateErrorHandler.class);
        assertThat(z0.getInterceptors().size()).isGreaterThan(0);
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        lInterceptors.add(new LoggingRequestInterceptor());
        List<HttpMessageConverter<?>> covs = z0.getMessageConverters();
        z0 = new Zg2proRestSslTemplate(keystore, null, lInterceptors);
        assertThat(z0).isNotNull();
        Zg2proRestSslTemplate z = new Zg2proRestSslTemplate(keystore, covs, null);
        z.setInterceptors(lInterceptors);
        assertThat(z.getInterceptors().size()).isGreaterThan(0);
        z.setRequestFactory(LoggingRequestFactoryFactory.build());
        assertThat(z.getInterceptors().size()).isGreaterThan(0);
        assertThat(z.getErrorHandler()).isInstanceOf(RestTemplateErrorHandler.class);
        rt.getRestTemplate().setRequestFactory(z.getRequestFactory());
        ResponseEntity<String> resp;
        resp = rt.getForEntity(MockedControllers.TEST_URL_GET_BLANK_REPLY, String.class);
        assertNotNull(resp);
        ReturnedStructure rs = rt.getForObject(TEST_URL_GET_STRUCTURE, ReturnedStructure.class);
        assertThat(rs.getFieldOne()).isEqualTo(12);
    }

}
