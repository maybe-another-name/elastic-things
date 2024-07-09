# Abstract samples (copied from comparison)

## Sample document structure (copied from comparison)

```
small-display-field-a 
small-display-field-b
small-display-field-c
large-field-d
enriched-display-field-e
large-enriched-field-f
```

* All the 'display' fields must come back in the result to be displayed in the table
* A substantial portion of the documents will have the 'enriched-display-field-e'
* All documents have a 'sample-id' field (not listed for simplicity)
  * This becomes relevant for lookups, which would use this to perform a join


## Samples (copied from comparison)

sample a:
* has no large or large-enriched fields
* has 'enriched-display-field-e'

sample b:
* has large field
* has 'enriched-display-field-e'

sample c:
* has large-enriched field
* has 'enriched-display-field-e'

sample d:
* has large field
* has large-enriched field
* has 'enriched-display-field-e'

___Note: samples 'b','c'&'d' are handled the same in the candidates; the difference is on the client-side___
  * for brevity, only samples 'a' & 'b' are demonstrated (since 'b' is anticipated as being the same as 'c' & 'd')

# Things to demonstrate

* Want to make the field-names more tangible
* Want to make samples tied to specific uses-cases
  * normal lookup operation (no splits, and a single lookup)
  * alternative lookup operation (searching against lookup field directly)
  * handling multiple hits for a search
    * specific token included 
  * specific boolean searches which will not work
    * AND (across documents)
    * NOT (across documents)
  * specific boolean searches which will always work
    * OR (across documents)
    * AND (across non-split fields)
    * NOT (across non-split fields)