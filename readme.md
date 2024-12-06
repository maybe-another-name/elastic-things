# running es locally

typically done with docker: https://www.elastic.co/guide/en/elasticsearch/reference/current/run-elasticsearch-locally.html

## docker setup

https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository

### prevent docker from running on startup

https://askubuntu.com/questions/766318/disable-docker-autostart-at-boot

```
sudo systemctl disable docker.service
sudo systemctl disable docker.socket
```

# running it

* run from ide
* expects the env file from the es setup script (in elastic-start-local) to be in src/main/resources
* setup of https done separately
