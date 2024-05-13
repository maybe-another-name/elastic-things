package blah;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.cat.NodesResponse;
import co.elastic.clients.elasticsearch.nodes.NodesStatsResponse;
import co.elastic.clients.elasticsearch.nodes.Stats;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeStatFetcher {

  // elastic password...
  // api key...
  public static void main(String[] args) throws ElasticsearchException, IOException {
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

    NodesStatsResponse stats = esClient.nodes().stats();
    // log.info("response: {} <end>",stats);

    // just take first node...
    Stats nodestats = stats.nodes().values().iterator().next();
    // log.info("jvm stats: {}", nodestats.jvm());

    var usedHeapBytes = nodestats.jvm().mem().heapUsedInBytes();
    var usedHeapPercent = nodestats.jvm().mem().heapUsedPercent();
    var nonHeap = nodestats.jvm().mem().nonHeapUsedInBytes();
    var oldUsedPercent = nodestats.jvm().mem().pools().get("old").usedInBytes() * 100L
        / nodestats.jvm().mem().pools().get("old").maxInBytes();
    var oldUsed = nodestats.jvm().mem().pools().get("old").usedInBytes();
    var oldMax = nodestats.jvm().mem().pools().get("old").maxInBytes();
    var peakOldUsed = nodestats.jvm().mem().pools().get("old").peakUsedInBytes();
    var peakMaxUsed = nodestats.jvm().mem().pools().get("old").peakMaxInBytes();
    var indexOverhead = nodestats.indices().mappings().totalEstimatedOverhead();
    // doesn't have the 'deduplicated one'

    log.info("Stats:"
        + "\n\tusedHeap: " + usedHeapPercent + "%"
        + "\n\tusedHeap: " + readableBytes(usedHeapBytes)
        + "\n\tnonHeap: " + readableBytes(nonHeap)
        + "\n\toldUsed: " + oldUsedPercent + "%"
        + "\n\toldUsed: " + readableBytes(oldUsed)
        + "\n\toldMax: " + readableBytes(oldMax)
        + "\n\tpeakOldUsed: " + readableBytes(peakOldUsed)
        + "\n\tpeakOldMax: " + readableBytes(peakMaxUsed));

    var breakers = nodestats.breakers();
    log.info("breakers: {}", breakers);

    esClient.shutdown();
    transport.close();
    restClient.close();
  }

  static String readableBytes(Long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    } else if (bytes < 1024 * 1024) {
      return bytes / 1024 + " KB";
    } else if (bytes < 1024 * 1024 * 1024) {
      return bytes / (1024 * 1024) + " MB";
    } else if (bytes < 1024 * 1024 * 1024 * 1024) {
      return bytes / (1024 * 1024 * 1024) + " GB";
    } else
      return bytes + " B";
  }
}
