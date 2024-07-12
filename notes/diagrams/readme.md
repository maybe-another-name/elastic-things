# Notes for the reader

Some diagrams to help illustrate the different write and read behaviours across the different schema candidates.

## ToDos

### Detailing Coordination

* Eliminating conflicts
    * Code base separation (either through endpoint or feature-flag)
    * Deployment separation (ensure both approaches can be deployed concurrently, so adoption is optional)

### Demonstration of specific requirements

Refer to confluence page for requirements.  Something which demonstrates how they are achieved with the different candidates is important.

### Read diagrams

Specific details outlined here:  [testing_multiple_runtime_lookups.md](../testing_multiple_runtime_lookups.md)

### Addressing duplicates

Refer to ES documenation on 'field collapsing'.  Example yet included.

### Addressing cross-index boolean queries

Refer to ES documentation on combining queries with aggregation field.  Example yet included.

#### Key things to consider: 

* consistent field naming
    * problems in field names may not present as errors in ES (just missing results)
* appropriate field type (must be 'aggregatable', like keyword)
* multi-index queries should not be considered an objective for arbitrary fields (ex: querying across different 'content' fields, like attachment extracts and bodies)
    * instead, focus on key metadata + content combinations

# Editor notes

## Style notes for consistency

Main desire is to make things easier to view in both dark and light themes.

## Filetype

Saving as editable png (svg introduces a checkering when viewed).

## Filename

Use the '-editable' suffix to help make it clear that these can be opened and edited in draw.io (https://app.diagrams.net/)
Keep it as the suffix so that the filenames are still readable.

## Sizes
* Use larger font (16pt)

* Use larger lines (+3)

## Colours
* Main is purple (9673A6)
* Mild Text Highlight is blue (7EA6E0)
* Mild Element Highlight is orange (CC6600)