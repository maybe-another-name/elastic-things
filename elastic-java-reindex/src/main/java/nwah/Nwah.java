package nwah;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.BulkIndexByScrollFailure;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SlicesCalculation;
import co.elastic.clients.elasticsearch._types.WaitForActiveShardOptions;
import co.elastic.clients.elasticsearch.core.ReindexRequest;
import co.elastic.clients.elasticsearch.core.ReindexRequest.Builder;
import co.elastic.clients.elasticsearch.core.ReindexResponse;
import co.elastic.clients.elasticsearch.tasks.GetTasksRequest;
import co.elastic.clients.elasticsearch.tasks.GetTasksResponse;
import lombok.extern.slf4j.Slf4j;
import nwah.helpers.EsConnectionHelper;
import nwah.helpers.EsConnectionHelper.EsClosableThings;

@Slf4j
public class Nwah {

  private static final int HOW_MANY_TIMES = 10_000;
  private static final String SOURCE_INDEX = "shibu-source";
  private static final String DEST_INDEX = "shibu-dest";

  public static void main(String[] args) throws ElasticsearchException, IOException {
    log.debug("yo");
    UUID.randomUUID();

    EsClosableThings esClosables = EsConnectionHelper.setupEsConnection(false);
    ElasticsearchClient esClient = esClosables.esClient();

    for (int i = 1; i <= HOW_MANY_TIMES; i++) {
      log.info("Started iteration: {}", i);
      String taskId = doReindexAsync(esClient);
      waitForTaskCompletion(esClient, taskId);
      log.info("Completed iteration: {}", i);
    }

    EsConnectionHelper.closeDem(esClosables);

  }

  private static void waitForTaskCompletion(ElasticsearchClient esClient, String taskId) throws IOException {
    boolean taskComplete;
    do {
      GetTasksRequest.Builder getTaskBuilder = new GetTasksRequest.Builder();
      getTaskBuilder.taskId(taskId);
      GetTasksResponse getTasksResponse = esClient.tasks().get(getTaskBuilder.build());
      log.debug("task response: {}", getTasksResponse);
      taskComplete = getTasksResponse.completed();
    } while (!taskComplete);
  }

  private static String doReindexAsync(ElasticsearchClient esClient) throws IOException {
    Builder buildReindex = new ReindexRequest.Builder();
    // async
    buildReindex.waitForCompletion(false);
    // wait for active shards doesn't work when only using one locally
    // buildReindex.waitForActiveShards(wait ->
    // wait.option(WaitForActiveShardOptions.All));
    buildReindex.slices(slices -> slices.computed(SlicesCalculation.Auto));

    // main indexing stuff
    buildReindex.source(src -> src.index(SOURCE_INDEX));
    buildReindex.dest(dest -> dest.index(DEST_INDEX));
    buildReindex.script(s -> s.inline(
        inline -> inline.lang("painless").source(painless)));
    ReindexResponse reindexResponse = esClient.reindex(buildReindex.build());

    List<BulkIndexByScrollFailure> failures = reindexResponse.failures();
    if (failures.size() > 0) {
      log.warn("Failure during reindexing: {}", failures);
    }

    log.debug("reindex response: {}", reindexResponse);
    String taskId = reindexResponse.task();
    return taskId;
  }

  private static String painless = """
      ctx._id = ctx._source.id + "_" + UUID.randomUUID();
      """;
}
