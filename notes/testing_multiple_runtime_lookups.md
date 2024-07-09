# testing_multiple_runtime_lookups

## schema outline

### index a: email-envelope

`
{
  edh
  to
  from
  date
  subject
}
`

### index b: enrichment-views

`
{
  edh
  views
}
`

### index b: enrichment-translated-subjects

`
{
  edh
  translated-subject
}
`

## sample data

We'll search by views, and tos.

email one
* to: jimmy
* from: jane
* viewed by: bobby
* translated subject: avocadoes

email two
* to: jimmy
* from: jane
* viewed by: timmy
* translated subject: pepper

email three
* to: jane
* from: billy
* viewed by: bobby
* translated subject: pepper


### writing email one
```
POST email-envelope/_doc?refresh
{
  "email-edh": "1",
  "to": "jimmy",
  "from": "jane",
  "date": "monday",
  "subject": "אבוקדואים"
}
POST enrichment-views/_doc?refresh
{
  "email-edh": "1",
  "views": "bobby"
}
POST enrichment-translated-subject/_doc?refresh
{
  "email-edh": "1",
  "translated-subject": "avocadoes"
}
```

### writing email two
```
POST email-envelope/_doc?refresh
{
  "email-edh": "2",
  "to": "jimmy",
  "from": "jane",
  "date": "monday",
  "subject": "перцы"
}
POST enrichment-views/_doc?refresh
{
  "email-edh": "2",
  "views": "timmy"
}
POST enrichment-translated-subject/_doc?refresh
{
  "email-edh": "2",
  "translated-subject": "peppers"
}
```

### writing email three
```
POST email-envelope/_doc?refresh
{
  "email-edh": "3",
  "to": "jane",
  "from": "billy",
  "date": "monday",
  "subject": "перцы"
}
POST enrichment-views/_doc?refresh
{
  "email-edh": "3",
  "views": "bobby"
}
POST enrichment-translated-subject/_doc?refresh
{
  "email-edh": "3",
  "translated-subject": "peppers"
}
```



## queries

### single field queries

#### searching based on enrichment

We'll query using for all emails viewed by bobby (matching emails 1&3), looking up their envelopes and translated subjects.
Because 'fields' in Elastic are always an array, we have some oddities with field name nesting of lookups.  We can structure our query to make this a little more clear

:zap: note that having a typo in the fetch_field does not give an error (just no results)

##### Query which doesn't do any flattening of fields
```
GET enrichment-views/_search
{
  "query": {
    "match": {
      "views": "bobby"
    }
  },
  "runtime_mappings": {
    "to": {
        "type": "lookup", 
        "target_index": "email-envelope", 
        "input_field": "email-edh", 
        "target_field": "edh", 
        "fetch_fields": ["to"] 
    },
    "from": {
        "type": "lookup", 
        "target_index": "email-envelope", 
        "input_field": "email-edh", 
        "target_field": "edh", 
        "fetch_fields": ["from"] 
    },
    "translated-subject": {
        "type": "lookup", 
        "target_index": "enrichment-translated-subject", 
        "input_field": "email-edh", 
        "target_field": "email-edh", 
        "fetch_fields": ["translated-subject"] 
    }
  },
  "fields": [
    "views",
    "to",
    "from",
    "translated-subject"
  ],
  "_source": false
}
```

##### Query which embraces the nesting
```
GET enrichment-views/_search
{
  "query": {
    "match": {
      "views": "bobby"
    }
  },
  "runtime_mappings": {
    "envelope": {
        "type": "lookup", 
        "target_index": "email-envelope", 
        "input_field": "email-edh", 
        "target_field": "edh", 
        "fetch_fields": ["to","from"] 
    },
    "enrichment": {
        "type": "lookup", 
        "target_index": "enrichment-translated-subject", 
        "input_field": "email-edh", 
        "target_field": "email-edh", 
        "fetch_fields": ["translated-subject"] 
    }
  },
  "fields": [
    "views",
    "envelope",
    "enrichment"
  ],
  "_source": false
}
```

#### searching based on envelope

As before we'll have two lookups.  We'll use the nesting-resigned query again.

```
GET email-envelope,enrichment-views/_search
{
  "query": {
    "match": {
      "to": "jimmy"
    }
  },
  "runtime_mappings": {
    "enrichment-views": {
        "type": "lookup", 
        "target_index": "enrichment-views", 
        "input_field": "edh", 
        "target_field": "email-edh", 
        "fetch_fields": ["views"] 
    },
    "enrichment-translated-subject": {
        "type": "lookup", 
        "target_index": "enrichment-translated-subject", 
        "input_field": "edh", 
        "target_field": "email-edh", 
        "fetch_fields": ["translated-subject"] 
    }
  },
  "fields": [
    "enrichment-views",
    "to",
    "from",
    "subject",
    "enrichment-translated-subject"
  ],
  "_source": false
}
```

### multi-field queries

see separate document about cross-index queries
