# FTP Server

The FTP Server that uses Amazon S3 as file system.

### Build

Build the project with gradle:

```bash
./gradlew build

```

### Build Docker image

```shell
./gradlew docker
```

### Run docker-compose

Docker-compose configuration has a pre-configured MySQL 

```shell
docker-compose up -d --wait
```

Down containers

```shell
docker-compose down
```
