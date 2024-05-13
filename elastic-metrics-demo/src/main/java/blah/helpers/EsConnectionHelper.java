package blah.helpers;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EsConnectionHelper {

  public static EsClosableThings setupEsConnection() {
    String apiKey = "";

    RestClient restClient;
    try {
      restClient = SecurityHelper.setupSecurity(apiKey);
    } catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException
        | IOException e) {
      log.error("Failed to setup es rest client");
      throw new RuntimeException(e);
    }
    log.info("Created the rest-client");

    ElasticsearchTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper());
    log.info("Created the transport and json mapper");

    ElasticsearchClient esClient = new ElasticsearchClient(transport);
    log.info("Created the es client");

    return new EsClosableThings(esClient, transport, restClient);
  }

  public static void closeDem(EsClosableThings esThings) throws IOException {
    esThings.esClient.shutdown();
    esThings.transport.close();
    esThings.restClient.close();
  }

  public record EsClosableThings(ElasticsearchClient esClient, ElasticsearchTransport transport,
      RestClient restClient) {
  };
}
