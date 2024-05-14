package blah;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import blah.helpers.EsConnectionHelper;
import blah.helpers.EsConnectionHelper.EsClosableThings;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.extern.slf4j.Slf4j;

import static blah.Things.LogEntry;

/**
 * As outlined in testing_goals.md, this will try to cause *measureable* problem
 * in elastic...
 * <p>
 * ...'many', 'really large' documents, updated 'multiple' times
 * <p>
 * <p>
 * 'many': start with 1 every 10s
 * <br>
 * <br>
 * 'really large': start with 1MB
 * <br>
 * <br>
 * 'multiple': start with twice...
 */
@Slf4j
public class BadDocumentIndexer {

  public static void main(String args[]) throws ElasticsearchException, IOException {
    EsClosableThings esThings = EsConnectionHelper.setupEsConnection();

    try {
      LogEntry logEntry = new LogEntry("192.168.1.1", "another message");

      esThings.esClient().index(index -> index
          .index("logs")
          .document(logEntry));

      esThings.esClient().index(index -> index
          .index("logs")
          .document(makeBadLogEntry()));

    } finally {
      EsConnectionHelper.closeDem(esThings);
    }
  }

  static LogEntry makeOkayLogEntry() {
    return new LogEntry("192.168.1." + new Random().nextInt(1, 255), "another message");
  }

  static LogEntry makeBadLogEntry() {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    URL fileUrl = classloader.getResource("sherlock.txt");
    try {
      String reallyBigMessage = Files.readString(Paths.get(fileUrl.toURI()));
      return new LogEntry("192.168.1." + new Random().nextInt(1, 255), reallyBigMessage);
    } catch (IOException | URISyntaxException e) {
      log.error("Couldn't generate message");
      throw new RuntimeException(e);
    }
  }

}
