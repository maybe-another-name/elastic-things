# relevant things to pull from elastic api

## node stats

### breaker info

{
    "breakers": {
        "request": {
            "estimated_size": "0b",
            "estimated_size_in_bytes": 0,
            "limit_size": "307.1mb",
            "limit_size_in_bytes": 322122547,
            "overhead": 1,
            "tripped": 0
        },
        "parent": {
            "estimated_size": "178.8mb",
            "estimated_size_in_bytes": 187534984,
            "limit_size": "486.3mb",
            "limit_size_in_bytes": 510027366,
            "overhead": 1,
            "tripped": 0
        },
        "fielddata": {
            "estimated_size": "368b",
            "estimated_size_in_bytes": 368,
            "limit_size": "204.7mb",
            "limit_size_in_bytes": 214748364,
            "overhead": 1.0299999713897705,
            "tripped": 0
        },
        "model_inference": {
            "estimated_size": "0b",
            "estimated_size_in_bytes": 0,
            "limit_size": "256mb",
            "limit_size_in_bytes": 268435456,
            "overhead": 1,
            "tripped": 0
        },
        "eql_sequence": {
            "estimated_size": "0b",
            "estimated_size_in_bytes": 0,
            "limit_size": "256mb",
            "limit_size_in_bytes": 268435456,
            "overhead": 1,
            "tripped": 0
        },
        "inflight_requests": {
            "estimated_size": "0b",
            "estimated_size_in_bytes": 0,
            "limit_size": "512mb",
            "limit_size_in_bytes": 536870912,
            "overhead": 2,
            "tripped": 0
        }
    }
}

## field data (memory size)

        "fielddata": {
          "memory_size_in_bytes": 368,
          "evictions": 0,
          "global_ordinals": {
            "build_time_in_millis": 34
          }
        },

## get (time_in_millis)
## indexing (index_time_in_millis)
## search 
(query_time_in_millis)
(fetch_time_in_millis)
## merge total_time_in_millis


## segments (memory_in_bytes)

# mem
  -> doesn't seem to actually reflect (says used is 100%, and only says 1GB)

# jvm

* used heap
* comparing young & old usage (esp 'old')
! it's using the G1 algorithm... hmmmm
  -> configure this to use cms?


lots of thread_pool information...

histogram on incoming transport times...

https://www.elastic.co/guide/en/elasticsearch/reference/current/high-jvm-memory-pressure.html
-> tracking garbage collection events in the logs...

https://www.elastic.co/guide/en/elasticsearch/reference/current/size-your-shards.html
> GET _nodes/stats?human&filter_path=nodes.*.name,nodes.*.indices.mappings.total_estimated_overhead*,nodes.*.jvm.mem.heap_max*
> GET _cluster/stats?human&filter_path=indices.mappings.total_deduplicated_mapping_size*

> "total_deduplicated_mapping_size": "1gb",
> "total_estimated_overhead": "1gb",

`
1 GB for the cluster state field information.
1 GB for the additional estimated heap overhead for the fields of the data node.
0.5 GB of extra heap for other overheads.

> https://www.elastic.co/guide/en/elasticsearch/reference/current/size-your-shards.html#force-merge-during-off-peak-hours
...i thought you coudl do this...but there are caveats...

> https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules-merge.html
...not sure what the current setting is (could decrease it to cause them to wait..)
