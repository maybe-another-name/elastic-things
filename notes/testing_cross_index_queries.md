# testing_cross_index_queries

# searching using a combination of envelope and view

## Basic query
```
GET email-envelope,enrichment-views/_search
{
  "query": {
    "bool": {
      "should": [
        { "match" : { "to": "jimmy" } },
        { "match" : { "views": "bobby" } }
      ]
    }
  }
}
```
This only supports an 'or'; the items would need to be combined in the client (IRS).

## Using aggregations

### Mapping setup

* Needs to ensure that the aggregation field is valid (non-text), so we'll define some mapping templates (good reason to keep the field names the same across indicies)
```
# something to map edh as keyword
PUT enrichment-translated-subject
{
  "mappings": {
    "dynamic_templates": [
      {
        "edh_as_keyword": {
          "match_mapping_type": "string",
          "match": "*edh",
          "mapping": {
            "type": "keyword"
          }
        }
      }
    ]
  }
}
PUT enrichment-views
{
  "mappings": {
    "dynamic_templates": [
      {
        "edh_as_keyword": {
          "match_mapping_type": "string",
          "match": "*edh",
          "mapping": {
            "type": "keyword"
          }
        }
      }
    ]
  }
}
PUT email-envelope
{
  "mappings": {
    "dynamic_templates": [
      {
        "edh_as_keyword": {
          "match_mapping_type": "string",
          "match": "*edh",
          "mapping": {
            "type": "keyword"
          }
        }
      }
    ]
  }
}

```
### aggregated query
```
GET email-envelope,enrichment-views/_search
{
  "query": {
    "bool": {
      "should": [
        { "match" : { "to": "jimmy" } },
        { "match" : { "views": "bobby" } }
      ]
    }
  },
  "aggs": {
    "my-agg-name": {
      "terms": {
        "field": "email-edh"
      }
    }
  }  
}
```

Can include a filter to only include documents with an aggregation count equal to the number of indicies...


# combining/bucketing emails split across multiple documents