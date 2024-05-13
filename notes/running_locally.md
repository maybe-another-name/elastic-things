https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html

# things which need to be repeated

1) memory setting (on host machine reboot, unless set persistently)
> sudo sysctl vm.max_map_count=262144
2) copying kibana token (from es startup logs)
3) copying elastic password (for kibana login)
4) generating cert
> elastic-things/elastic-metrics-demo/src/main/java/blah/helpers$ docker cp es01:/usr/share/elasticsearch/config/certs/http_ca.crt .
5) generating app key (for java client)
see: https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/getting-started-java.html