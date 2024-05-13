# two issues

1) We enrich our data into a single document.  The subsequent updates cause deletes; later when ES merges the segments, it can try to pull too much into memory, causing cascading OOMs.
2) We have large documents.  ES seems to pull the entire document into memory when searching (even if we only request some fields come back).  This doesn't cause cascading failures, but does cause intermittent unavailablility.

# possible approaches

...it _sounds_ like using 'runtime_fields' allow us to address both problems 
  1) by splitting up documents
  2) by allowing us to 'enrich' documents without reindexing...

it also sounds like it would have the benefit of fewer irs changes (since it only needs to do a single search...)