package blah;

import java.io.IOException;

import blah.helpers.EsConnectionHelper;
import blah.helpers.EsConnectionHelper.EsClosableThings;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.nodes.NodesStatsResponse;
import co.elastic.clients.elasticsearch.nodes.Stats;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeStatFetcher {

  // elastic password...
  // api key...
  public static void main(String[] args) throws ElasticsearchException, IOException {

    EsClosableThings esThings = EsConnectionHelper.setupEsConnection();
    try {
      NodesStatsResponse stats = esThings.esClient().nodes().stats();
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

    } finally {
      EsConnectionHelper.closeDem(esThings);
    }
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
