# outline

This document demonstrates some of the considerations and potential schema designs

## general points:

- only things which are expected to be displayed in the table need to be returned
- we have two primary desires, with the current priority:
  1) smaller documents
  2) immutable documents (to prevent updates)
      - this priority is based on the recommendations of the es team, as well as the observations of the other es client team
      - specifically: with small enough document sizes, the 'other client team' index appears to maintain stability even with consistend updates
      - caveats
        - size metrics are difficult to derive (specifically the distribution), so the comparison was high-level
          - (ie: just compared things on the layer - # documents / total size)
        - there could be other factors at play 
          - ex: 
            - timing of updates
            - number of updates (per document)
  3) another consideration: better handling of asynchronous enrichments
     * when enrichments are received before the primary document is ready, they fail

## Sample document structure
 
>small-display-field-a 
>
>small-display-field-b
>
>small-display-field-c
>
>large-field-d
>
>enriched-display-field-e
>
>large-enriched-field-f


* All the 'display' fields must come back in the result to be displayed in the table
* A substantial portion of the documents will have the 'enriched-display-field-e'
* All documents have a 'sample-id' field (not listed for simplicity)
  * This becomes relevant for lookups, which would use this to perform a join


## Samples

sample a:
 * has no large or large-enriched fields
 * has 'enriched-display-field-e'

sample b:
 * has large field
 * has 'enriched-display-field-e'

sample c:
 * has large-enriched field
 * has 'enriched-display-field-e'


# candidate a: single 'sparse' schema with updates

* called sparse since each schema includes fields for parts which aren't present on all documents (ex: large field splits)
  * each 'part' would be in the same named es field, so calling it 'sparse' is an exaggeration
* addresses the problem of document size (by splitting large fields)
* does not address the problem of updates (and is made worse, since each displayable enrichment needs to hit multiple documents)

## Sample document structure (repeated for convenience)
>small-display-field-a 
>
>small-display-field-b
>
>small-display-field-c
>
>large-field-d
>
>enriched-display-field-e
>
>large-enriched-field-f

## Samples (repeated for convenience)

sample a:
 * has no large or large-enriched fields

sample b:
 * has large field

sample c:
 * has large-enriched field

## Document 'a' with no 'large' or 'large-enriched' fields
`
small-field-a
small-field-b
small-field-c
enriched-display-field-e
`
---
## Document 'b' with 'large' field
`
small-field-a
small-field-b
small-field-c
large-field-d.1
enriched-display-field-e
`
---
...
---
`
small-field-a
small-field-b
small-field-c
large-field-d.n
enriched-display-field-e
`
---

** __Note: every 'enriched-display' change hits all sample fragments (multiple documents updated)__

## Document 'c' with 'large-enriched' field
`
small-field-a
small-field-b
small-field-c
large-field-d.1
enriched-display-field-e
`
---
`
small-field-a
small-field-b
small-field-c
large-field-d.n
enriched-display-field-e
`
---

## Considerations:
- no usage of runtime lookup
- no requirement to change searches
  - this means we still fully support 'problematic' searches (like unfielded searches)
- duplication of small fields
  - no substantial negative performance impact anticipated, based on current understanding that overal size is not a problem, but rather size of individual documents
- __including the 'enriched-display-field' means that now a single update fans out across multiple documents__
  - dealing with the primary problem (document size) makes this potentially worse (even more documents to update)
- indexing requires state
  - we currently do this (but I often complain about it adding complexity)
- the 'small-fields' need to be available for each document
  - this is more relevant for the 'enriched' fields
    - they would either require the 'small' fields to be included in their inputs (which I advocate for), or require a lookup
    - indexing speed is not currently an issue, but the complexity of variable incoming rates creates a race condition which causes problems (ie: arrival of 'enrichment' before the original is ready)

# candidate b: 'sparse' index with runtime lookup

* called sparse since each schema includes fields for parts which aren't present on all documents (ex: large field splits)
  * each 'part' would be in the same named es field, so calling it 'sparse' is an exaggeration
* uses same approach of candidate 'a' to address the problem of document size (by splitting large fields, and keeping them in the same index)
* also addresses the problem of updates (by ensuring that each enrichment goes to a separate document)
  * notably, displayable enrichments are kept in a separate index
    (so that they can be joined at query-time)
    * uses 'runtime lookup' to merge all displayable elements

## Sample document structure (repeated for convenience)

`
small-display-field-a
small-display-field-b
small-display-field-c
large-field-d
enriched-display-field-e
large-enriched-field-f
`

## Samples (repeated for convenience)

sample a:
 * has no large or large-enriched fields

sample b:
 * has large field

sample c:
 * has large field and large-enriched field

## Document 'a' with no 'large' or 'large-enriched' fields

index I:
`
small-field-a
small-field-b
small-field-c
`
---
index II:
`
enriched-display-field-e
`
---

## Document 'b' with 'large' field

index I:
`
small-field-a
small-field-b
small-field-c
`
---
index II:
`
enriched-display-field-e
`
---
index I:
`
small-field-a
small-field-b
small-field-c
large-field-d.1
`
---
...
---
`
small-field-a
small-field-b
small-field-c
large-field-d.n
`
---

## Document 'c' with 'large-enriched' field

index I:
`
small-field-a
small-field-b
small-field-c
`
---
index II:
`
enriched-display-field-e
`
---
index I:
`
small-field-a
small-field-b
small-field-c
large-enriched-field-f.1
`
---
...
---
`
small-field-a
small-field-b
small-field-c
large-enriched-field-f.n
`
---

# candidate c: separate indicies with runtime lookup

* unlike the 'sparse' candidates (a/b), the pieces are separated into different index
  * this means that we do not require denormalization
    * the space savings are not relevant
    * the schema is (arguably) easier to understand/maintain
    * would help ensure that fielded queries are working properly (essentially requiring them)
      * this has a noticeable impact on query performance
      * would require the client (IRS) to choose the appropriate primary index to query
        * either based on specific field choice, or 'interpreted/imposed' field choice
* like candidate 'b', uses runtime lookup to join all displayable elements
* does not require the client to have any 'state' to perform indexing

## Sample document structure (repeated for convenience)

`
small-display-field-a
small-display-field-b
small-display-field-c
large-field-d
enriched-display-field-e
large-enriched-field-f
`

## Samples (repeated for convenience)

sample a:
 * has no large or large-enriched fields

sample b:
 * has large field

sample c:
 * has large field and large-enriched field

## Document 'a' with no 'large' or 'large-enriched' fields

Same as candidate 'b'. Repeated for convenience:

index I:
`
small-field-a
small-field-b
small-field-c
`
---
index II:
`
enriched-display-field-e
`
---

## Document 'b' with 'large' field

index I:
`
small-field-a
small-field-b
small-field-c
`
---
index II:
`
enriched-display-field-e
`
---
index III:
`
large-field-d.1
`
---
...
---
`
large-field-d.n
`
---

## Document 'c' with 'large-enriched' field

index I:
`
small-field-a
small-field-b
small-field-c
`
---
index II:
`
enriched-display-field-e
`
---
index IV:
`
large-enriched-field-f.1
`
---
...
---
`
large-enriched-field-f.n
`
---

