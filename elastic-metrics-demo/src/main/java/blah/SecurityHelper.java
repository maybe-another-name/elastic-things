package blah;

import java.io.IOException;
import java.io.InputStream;
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

class SecurityHelper {

  static RestClient setupSecurity(String apiKey)
      throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    InputStream crtStream = SecurityHelper.class.getResourceAsStream("http_ca.crt");
    Certificate trustedCa = factory.generateCertificate(crtStream);
    KeyStore trustStore = KeyStore.getInstance("pkcs12");
    trustStore.load(null, null);
    trustStore.setCertificateEntry("ca", trustedCa);
    SSLContextBuilder sslContextBuilder = SSLContexts.custom()
        .loadTrustMaterial(trustStore, null);
    final SSLContext sslContext = sslContextBuilder.build();

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
  }

}
