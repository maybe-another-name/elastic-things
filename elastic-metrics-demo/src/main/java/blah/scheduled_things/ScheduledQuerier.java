package blah.scheduled_things;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import blah.helpers.EsConnectionHelper;
import blah.helpers.EsConnectionHelper.EsClosableThings;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static blah.Things.LogEntry;

@RequiredArgsConstructor
@Slf4j
public class ScheduledQuerier {

  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private final EsClosableThings esThings;

  public static void main(String[] args) throws IOException, InterruptedException {

    ScheduledQuerier querier = new ScheduledQuerier(EsConnectionHelper.setupEsConnection());

    scheduler.scheduleAtFixedRate(() -> querier.queryStringQuery(), 0, 2,
        TimeUnit.MINUTES);

    log.info("Awaiting termination");
    scheduler.awaitTermination(2, TimeUnit.SECONDS);
    log.info("Shutting down");
    scheduler.shutdown();
    EsConnectionHelper.closeDem(querier.esThings);
  }

  private void matchQuery() {
    try {
      log.info("Executing scheduled query");
      SearchResponse<LogEntry> searchResponse = esThings.esClient().search(s -> s
          .index("logs")
          .query(q -> q
              .match(t -> t
                  .field("message")
                  .query("another"))),
          LogEntry.class);
      log.info("Search response: {}", searchResponse);
    } catch (IOException | RuntimeException e) {
      log.error("Couldn't query", e);
      throw new RuntimeException(e);
    }
  };

  private void queryStringQuery() {
    try {
      log.info("Executing scheduled query");
      SearchResponse<LogEntry> searchResponse = esThings.esClient().search(s -> s
          .index("logs")
          .query(q -> q
              .queryString(t -> t
                  .query("another"))),
          LogEntry.class);
      log.info("Search response: {}", searchResponse);
    } catch (IOException | RuntimeException e) {
      log.error("Couldn't query", e);
      throw new RuntimeException(e);
    }
  }
}
