https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html

# error in logs

> max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]

## check value

> sysctl vm.max_map_count

## set value

> sudo sysctl vm.max_map_count=262144


# useful output

`
Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
  ...
`
`
Configure Kibana to use this cluster:
• Run Kibana and click the configuration link in the terminal when Kibana starts.
• Copy the following enrollment token and paste it into Kibana in your browser (valid for the next 30 minutes):
  ...
`  