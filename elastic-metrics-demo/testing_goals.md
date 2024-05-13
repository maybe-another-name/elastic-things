# writing goals

* something which triggers the circuit breaker
* something which demonstrates write 'load'
  * something which triggers deletions/updates
    * something to measure the load of the update/merge

# searching goals

* something which triggers the circuit breaker
* something which demonstrates search 'load'
* something which returns results, but is slow

# general comparison

* will want something to show the difference between large documents, and split documents


# approach

Start with the 'worst-case', to verify that we can observe problems...

## worst-case

...'many', 'really large' documents, updated 'multiple' times

'many': start with 1 every 10s
'really large': start with 1MB
'multiple': start with twice...