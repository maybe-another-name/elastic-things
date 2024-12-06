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

POST emails/_doc/1
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
{
  "query": {
    "term": {
      "body": {
        "value": "large"
      }
    }
  }
}

This should give a score...

# try adding another field

POST emails/_update/1
{
  "script" : "ctx._source.attachment_text = 'small extraction'"
}

# is the non-source field still searchable?

yes...
no...

# update including non-source

POST emails/_update/1
{
  "script" : """
  ctx._source.attachment_text = 'small extraction';
  ctx._source.body ='smaller body with text'
  """
}

# is the non-source field updated, and searchable?
yes