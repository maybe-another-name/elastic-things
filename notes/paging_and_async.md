https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html

> "Although the query is no longer running, hence is_running is set to false, results may be partial. That happens in case the search failed after some shards returned their results, or when the node that is coordinating the async search dies."

> "request_cache defaults to true"
https://www.elastic.co/guide/en/elasticsearch/reference/current/shard-request-cache.html
> "When a search request is run against an index or against many indices, each involved shard executes the search locally and returns its local results to the coordinating node, which combines these shard-level results into a “global” result set."
...is the cache necessary/useful?


> "Async search does not support scroll..."
? how do we fetch a 'block' of results?


> "batched_reduce_size defaults to 5: this affects how often partial results become available, which happens whenever shard results are reduced. A partial reduction is performed every time the coordinating node has received a certain number of new shard responses (5 by default)."
...this is based on replicas, so not necessarily reflective of a 'page' (as the distribution of results across shards will likely not be uniform...)

> "By default, Elasticsearch doesn’t allow to store an async search response larger than 10Mb, and an attempt to do this results in an error. The maximum allowed size for a stored async search response can be set by changing the search.max_async_search_response_size cluster level setting."
...perhaps a handy safety valve...
...force users to come up with something more restrictive, instead of trying to handle it...
...does the size include the source field?  (if i recall correctly, then no, based on previous testing specifying the fields to return...)

...delete a search to also delete the saved results...


https://opster.com/guides/elasticsearch/how-tos/elasticsearch-pagination-techniques/
...should double-check that we are using search after, and not pagination...

the 'pit' can add additional overhead to the merges:
https://www.elastic.co/guide/en/elasticsearch/reference/current/point-in-time-api.html#point-in-time-api
> "Additionally, if a segment contains deleted or updated documents then the point in time must keep track of whether each document in the segment was live at the time of the initial search request. Ensure that your nodes have sufficient heap space if you have many open point-in-times on an index that is subject to ongoing deletes or updates."
...like the async searches, these should be closed asap

? is there a way to see 'open' resources?