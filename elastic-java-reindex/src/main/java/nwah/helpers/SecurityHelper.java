package nwah.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class SecurityHelper {

  static RestClient setupSecurity(String apiKey, boolean isHttps)
    throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    String certPath = rootPath + "http_ca.crt";

    log.debug("Loading certificate from: {}", certPath);
    InputStream crtStream = Files.newInputStream(Paths.get(certPath));
    Certificate trustedCa = factory.generateCertificate(crtStream);
    KeyStore trustStore = KeyStore.getInstance("pkcs12");
    trustStore.load(null, null);
    trustStore.setCertificateEntry("ca", trustedCa);
    SSLContextBuilder sslContextBuilder = SSLContexts.custom()
      .loadTrustMaterial(trustStore, null);
    final SSLContext sslContext = sslContextBuilder.build();

    if (isHttps) {
      return RestClient.builder(
        new HttpHost("localhost", 9200, "https"))
        .setHttpClientConfigCallback(new HttpClientConfigCallback() {
          @Override
          public HttpAsyncClientBuilder customizeHttpClient(
            HttpAsyncClientBuilder httpClientBuilder) {
            return httpClientBuilder.setSSLContext(sslContext);
          }
        })
        .setDefaultHeaders(new Header[] {
          new BasicHeader("Authorization", "ApiKey " + apiKey)
        })
        .build();
    } else {
      return RestClient.builder(
        new HttpHost("localhost", 9200, "http"))
        .setHttpClientConfigCallback(new HttpClientConfigCallback() {
          @Override
          public HttpAsyncClientBuilder customizeHttpClient(
            HttpAsyncClientBuilder httpClientBuilder) {
            return httpClientBuilder.setSSLContext(sslContext);
          }
        })
        .setDefaultHeaders(new Header[] {
          new BasicHeader("Authorization", "ApiKey " + apiKey)
        })
        .build();
    }
  }

}
