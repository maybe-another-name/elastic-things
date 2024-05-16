# creating linked documents (dynamically created index and mapping)

`
POST ip_location/_doc?refresh
{
  "ip": "192.168.1.1",
  "country": "Canada",
  "city": "Montreal"
}

POST logs/_doc?refresh
{
  "host": "192.168.1.1",
  "message": "the first message"
}

POST logs/_doc?refresh
{
  "host": "192.168.1.2",
  "message": "the second message"
}

POST logs/_doc?refresh
{
  "host": "192.168.1.1",
  "message": "another first message"
}
`

can search from many to one (many messages to one location):

`
POST logs/_search
{
  "runtime_mappings": {
    "location": {
        "type": "lookup", 
        "target_index": "ip_location", 
        "input_field": "host", 
        "target_field": "ip", 
        "fetch_fields": ["country", "city"] 
    }
  },
  "fields": [
    "host",
    "message",
    "location"
  ],
  "_source": false
}
`

# aside - document size...

> yellow open   authors                                                            jNU59vrlSZyTAZPagDhvTQ   1   1          1            0      5.9kb          5.9kb        5.9kb

* not sure why it is yellow... only 1 node when there is a replica?
* not sure why that little json is 5kb...
  will add another and see what happens...

`
POST authors/_doc?refresh
{
  "book_id": "113607",
  "first_name": "Jimmy",
  "last_name": "James"
}
`
> yellow open   authors                                                            jNU59vrlSZyTAZPagDhvTQ   1   1          2            0     11.9kb         11.9kb       11.9kb


Yeah... seems large (6kb).  will try one more

`
POST authors/_doc?refresh
{
  "book_id": "113608",
  "first_name": "Billy",
  "last_name": "Willy"
}
`
> yellow open   authors                                                            jNU59vrlSZyTAZPagDhvTQ   1   1          3            0     17.7kb         17.7kb       17.7kb

So...roughly 6KB per document...

interesting... the mapping is kinda strange (not sure if text or keyword), ex:

`
        "last_name": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        }
`

* all the fields seem to come back as an array (except for native ones, like _index and _id)

* for now, just leave that, and continue with the main example...

* the books have a specified id, while the authors have a generated one...

* so, when the mapping doesn't have a match, there isn't an error
  - that part is just absent from that result...

* post doens't need an id, but put does...

# going further

## try having a many to many (more than one location for an ip address)

* add another address
`
POST ip_location/_doc?refresh
{
  "ip": "192.168.1.1",
  "country": "Canada",
  "city": "Ottawa"
}
`
-> search only returns one of the locations

## try searching from the other side...

`
POST ip_location/_search
{
  "runtime_mappings": {
    "message": {
        "type": "lookup", 
        "target_index": "logs", 
        "input_field": "ip", 
        "target_field": "host", 
        "fetch_fields": ["message"] 
    }
  },
  "fields": [
    "ip",
    "message",
    "country",
    "city"
  ],
  "_source": false
}
`
-> again, search only returned on of the messages

this matches their documentation:

https://www.elastic.co/guide/en/elasticsearch/reference/current/runtime-retrieving-fields.html
> Fields that are retrieved by runtime fields of type lookup can be used to enrich the hits in a search response. It’s not possible to query or aggregate on these fields.
> The response of lookup fields are grouped to maintain the independence of each document from the lookup index. The lookup query for each input value is expected to match at most one document on the lookup index. If the lookup query matches more than one documents, then a random document will be selected.

...will this cause any problems?
  - would not be able to go one to many, but can go many to one
  - ex: cannot get bodies from email, but can get email from bodies...

## including a query

`
GET ip_location/_search
{
  "query": {
    "match": {
      "city": "Ottawa"
    }
  },
  "runtime_mappings": {
    "message": {
        "type": "lookup", 
        "target_index": "logs", 
        "input_field": "ip", 
        "target_field": "host", 
        "fetch_fields": ["message"] 
    }
  },
  "fields": [
    "ip",
    "message",
    "country",
    "city"
  ],
  "_source": false
}
`

# testing a lookup against the same field (separate document/sparse-schema)


`
POST logs/_doc?refresh
{
  "host": "192.168.1.1",
  "user": "jimmy-jimmy"
}
`

`
POST logs/_search
{
  "runtime_mappings": {
    "user": {
        "type": "lookup", 
        "target_index": "logs", 
        "input_field": "host", 
        "target_field": "host", 
        "fetch_fields": ["user"] 
    }
  },
  "fields": [
    "host",
    "message",
    "user"
  ],
  "_source": false
}
`
-> doesn't seem to work... confirm against a separate index

`
POST logins/_doc?refresh
{
  "host": "192.168.1.1",
  "user": "jimmy-jimmy"
}
`

`
POST logs/_search
{
  "runtime_mappings": {
    "user": {
        "type": "lookup", 
        "target_index": "logins", 
        "input_field": "host", 
        "target_field": "host", 
        "fetch_fields": ["user"] 
    }
  },
  "fields": [
    "host",
    "message",
    "user"
  ],
  "_source": false
}
`
-> expected result