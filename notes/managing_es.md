https://www.elastic.co/guide/en/cloud/current/ec-memory-pressure.html#ec-memory-pressure-causes


https://www.elastic.co/blog/managing-and-troubleshooting-elasticsearch-memory
> Caused by: org.elasticsearch.common.breaker.CircuitBreakingException: [parent] Data too large, data for [<transport_request>] would be [num/numGB], which is larger than the limit of [num/numGB], usages [request=0/0b, fielddata=num/numKB, in_flight_requests=num/numGB, accounting=num/numGB]
apparently these are the circuit-breaker (not from size limitations)
 - should include this in the dashboard...

> GET /_cat/nodes?v=true&h=name,node*,heap*
> GET /_nodes/stats?filter_path=nodes.*.jvm.mem.pools.old



# on key memory metrics...

"elasticsearch service console"
focuses on the 'fill rate of the old-generation pool'
  https://www.elastic.co/blog/found-understanding-memory-pressure-indicator/

> If JVM memory pressure above 75% happens only occasionally, this is often due to expensive queries. Queries that have a very large request size, that involve aggregations with a large volume of buckets, or that involve sorting on a non-optimized field, can all cause temporary spikes in JVM memory usage. To resolve this problem, consider optimizing your queries or upgrading to a larger cluster.

- elimintate aggregations
- ensure sorting is 'optimized'
- reduce request size


# other sources of problems
https://www.elastic.co/blog/found-crash-elasticsearch
> ...Elasticsearch does not have a timeout for long running scripts...
...can have scripts that don't return theads to pool...

## dealing with problematic memory
https://www.elastic.co/blog/found-understanding-memory-pressure-indicator/
> Consider if there are appropriate optimizations that you can do. Check your mappings, your shard count and your cache utilization. If for some reason Elasticsearch seems to use more heap than you have data, you probably should check your mappings and your sharding strategy.


# aside - mapping

https://www.elastic.co/blog/found-beginner-troubleshooting
> We need to index according to how we are going to search, instead of trying to construct searches according to how things are stored.

https://www.elastic.co/blog/changing-mapping-with-zero-downtime
! just use an alias to point to the new and old indicies...


# on 32GB
https://www.elastic.co/guide/en/elasticsearch/guide/current/heap-sizing.html
> In fact, it takes until around 40–50 GB of allocated heap before you have the same effective memory of a heap just under 32 GB using compressed oops.

? how much memory do we actually have available?
...they have some notes on machines with really large memory
  ...suggest leaving memory for OS cache
  ...suggest running multiple nodes on the same machine...

# things to look for

* relevant jvm memory (esp gc)
* es threads
? kopf
? cache utilization
? more efficient mappings

# to prometheus

https://github.com/prometheus-community/elasticsearch_exporter

> We suggest you measure how long fetching /_nodes/stats and /_all/_stats takes for your ES cluster to determine whether your scraping interval is too short. 

...see if i can set this up to scrap specific things
...doesn't seem to include old generation, or heap...
...unclear how to configure it when running as a docker container...
...seems to require root...


...maybe something smaller?
