package nwah.helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EsConnectionHelper {

  private static String readApiKeyFromEnvFile() {
    String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    String appConfigPath = rootPath + ".env";

    Properties esProps = new Properties();
    try {
      esProps.load(new FileInputStream(appConfigPath));
    } catch (IOException e) {
      log.error("Failed to setup es rest client - could not load es env file");
      throw new RuntimeException(e);
    }

    return esProps.getProperty("ES_LOCAL_API_KEY");
  }

  public static EsClosableThings setupEsConnection(boolean isHttps) {
    String apiKey = readApiKeyFromEnvFile();

    RestClient restClient;
    try {
      restClient = SecurityHelper.setupSecurity(apiKey, isHttps);
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
