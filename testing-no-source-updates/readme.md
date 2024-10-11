# objective

Verify the behaviour of 'updating' documents which have some fields excluded from source.

* Is there an update operation which preserves a field excluded from source?

# setup

see : [es-setup](../notes/running_locally.md)

Only need elastic, and kibana (no other nodes or api key).

pw: ...
kibana key: ...

# excluding fields from source - mapping

https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-source-field.html#include-exclude

PUT emails
{
  "mappings": {
    "_source": {
      "excludes": ["body"]
    }
  }
}

# writing a document with fields excluded from source

POST emails/_doc?refresh
{
  "email-edh": "1",
  "to": "jimmy",
  "from": "jane",
  "date": "monday",
  "body": "large body with text"
}

# verify it exists

POST emails/_search

# verify the body is searchable

POST emails/_search
  "query": {
    "term": {
      "body": {
        "value": "large"
      }
    }
  }
}

# try adding another field

POST emails/_update/uPXofJIBLs1UBsxkxlP6
{
  "script" : "ctx._source.attachment_text = 'small extraction'"
}

# is the non-source field still searchable?

yes...

# what other operations can we do?

what about updating a (non-source) field?

POST emails/_update/uPXofJIBLs1UBsxkxlP6
{
  "script" : "ctx._source.from = 'bobby'"
}

still searchable...