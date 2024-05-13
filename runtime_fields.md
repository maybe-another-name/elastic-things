https://www.elastic.co/guide/en/elasticsearch/reference/current/runtime.html

> "At its core, the most important benefit of runtime fields is the ability to add fields to documents after you’ve ingested them."

> "Queries against runtime fields are considered expensive."

* can be defined either during mapping, or during search...

https://www.elastic.co/guide/en/elasticsearch/reference/current/runtime-search-request.html

`
  "runtime_mappings": {
    "day_of_week": {
      "type": "keyword",
      "script": {
        "source": "emit(doc['@timestamp'].value.dayOfWeekEnum.getDisplayName(TextStyle.FULL, Locale.ROOT))"
      }
    }
  },
`

* has a negative impact on search speed: 
> "Your search speed decreases, but your index size is much smaller and you can more quickly process logs without having to index them."

* should have a positive impact on index speed:
> "You define runtime fields directly in the index mapping, saving storage costs and increasing ingestion speed."

* ways to ensure that the script doesn't run against all documents
> "For example, if you’re using the fields parameter on the _search API to retrieve the values of a runtime field, the script runs only against the top hits just like script fields do."

* mitigating performance
> "To balance search performance and flexibility, index fields that you’ll frequently search for and filter on, such as a timestamp. "
- so all the 'metadata' would be indexed normally...

> "Use the asynchronous search API to run searches that include runtime fields. This method of search helps to offset the performance impacts of computing values for runtime fields in each document containing that field. If the query can’t return the result set synchronously, you’ll get results asynchronously as they become available."
- not sure how this would work with paging...

* some underlying details:
https://github.com/elastic/elasticsearch/issues/97187


* script vs runtime field (may just want to use a script field)
https://stackoverflow.com/questions/76844025/whats-the-main-difference-between-script-fields-and-runtime-mapping-or-how-to
https://opster.com/guides/elasticsearch/how-tos/runtime-fields/
> "The big limitation of script fields is that they are applied after the query is made, in the fetch phase. This phase occurs after N results are selected from the query. In this case the script field acts more as a “decorator”, adding extra information to the documents, and not affecting how this document had been queried."


https://opster.com/guides/elasticsearch/search-apis/lookup-runtime-fields/
> "A lookup runtime field is a field in an index whose value is retrieved from another index."

