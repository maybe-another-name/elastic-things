? are we calling 'refresh' on our updates (making them more expensive)?

https://youtu.be/gWXkAhnYFYw?t=243
  ...advocates duplicating fields in multiple indicies

# query vs filter

https://www.elastic.co/guide/en/elasticsearch/reference/current/query-filter-context.html

query: how *well* does the document match?
  - includes a score
  - the 'query' parameter

filter: does the document match
  - yes/no
  - the 'filter' parameter
    ex: filter/must_not params of bool query
    ! a 'must' apparently isn't a filter...
  - cacheable (as bitset array)
  (not all filters are cached though: https://discuss.elastic.co/t/what-kind-of-filters-are-really-cached/180559/3)
    multiple caches: https://www.elastic.co/blog/elasticsearch-caching-deep-dive-boosting-query-speed-one-cache-at-a-time

`
curl localhost:9200/myindex/_search?pretty  -d '
{
  "query": { "bool": { "must": { "match": { "msg": "hello sam" }}}}
}'
`
vs
`
curl localhost:9200/myindex/_search?pretty  -d '
{
  "query": { "bool": { "filter": { "match": { "msg": "hello sam" }}}}
}'
`

