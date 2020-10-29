# Docker

## Why did this get built?

Before, Virtualized Machines were used as a sandbox environment to run and develop our code. These are basically whole computers running on your own computer, so the process was pretty involved.

Containers (like Docker) remove the need to host a guest OS within the environment since they focus on being able to serve one app at a time. They only bundle the libraries and settings required to run a specific app. They use the host OS compared to the guest OS, so they process is a lot quicker. This leads to microservices architecture where Kubernetes helps manage all of your different containers.

## How does it work?

Docker containers generate 'image' files that are used to bundle our application into a standalone package. The container environment is completely separate from the host environment and is dependant on the image.

[Docker Hub](https://hub.docker.com/) is similar to npm, but for docker container images.

## Dockerfile

`touch Dockerfile`

`FROM node:11.15.0` - tells Docker which image to use from docker hub

`WORKDIR /usr/src/smart-brain-api` - directory within the container we want to work out of

`COPY ./ ./` - copy everything from root directory to working directory

`RUN npm install` - you can RUN multiple scripts in the build process

`CMD ["/bin/bash"]` - what to run inside the container

`docker build -t firstcontainer .` - builds our image

`docker run -it firstcontainer` - runs image

`docker run -it -d firstcontainer` - runs image in background

`docker run -it -p 3000:3000 firstcontainer` - runs image in background and forwards the containers port 3000 to localhost 3000

`docker ps` - view all containers currently running

`docker exec -it dockerhash bash` - go back into container

`docker stop dockerhash` - stop a specific dockerfile

[More on Dockerfile commands](https://docs.docker.com/engine/reference/builder/#usage)
[More on /bin/bash](https://unix.stackexchange.com/questions/398543/what-are-the-contents-of-bin-bash-and-what-do-i-do-if-i-accidentally-overwrote)
[More on using docker for development](https://medium.com/hackernoon/a-better-way-to-develop-node-js-with-docker-cd29d3a0093)

## Docker Compose

Used to orchestrate different docker services

`touch docker-compose.yml`

`version: "3.8"` - grab latest version from docker compose docs

```
services:
    # Backend API
    smart-brain-api:
        container_name: backend
        # image: node:erbium
        build: ./
        command: npm start
        working_dir: /usr/src/smart-brain-api
        ports:
            - "3000:3000"
        environment:
            POSTGRES_URI: postgres://sally:secret@postgres:5432/smart-brain-docker
        # links:
        #     - postgres
        volumes:
            - ./:/usr/src/smart-brain-api
        
    # Postgres
    postgres:
        environment:
            POSTGRES_USER: Sally
            POSTGRES_PASSWORD: secret
            POSTGRES_DB: smart-brain-docker
            POSTGRES_HOST: postgres
        image: postgres
        ports:
            - "5432:5432"
```
[More on Volumes](https://stackoverflow.com/questions/34809646/what-is-the-purpose-of-volume-in-dockerfile)
[More on Volumes 2](https://www.linux.com/learn/docker-volumes-and-networks-compose)

[Networks replaced links](https://docs.docker.com/compose/networking/)
[More on networks](https://stackoverflow.com/questions/41294305/docker-compose-difference-between-network-and-link)

[Stopping and removing containers](https://linuxize.com/post/how-to-remove-docker-images-containers-volumes-and-networks/)

[Local Postgres vs docker](https://stackoverflow.com/questions/48593016/postgresql-docker-role-does-not-exist)
[More on Local Postgres](https://stackoverflow.com/questions/45671327/correct-way-to-start-stop-postgres-database-pg-ctl-or-service-postgres)

`docker-compose build` - rerun every time you change the .yml file [More on build](https://docs.docker.com/compose/reference/build/)

`docker-compose run smart-brain-api` - starts the container [More on run](https://docs.docker.com/compose/reference/run/)

`docker-compose down` - stops any running containers 

`docker-compose up --build` - builds and runs in one command (can only be used after initial build) [More on up](https://docs.docker.com/compose/reference/up/)

`docker-compose exec smart-brain-api bash` - enter bash terminal of a container currently running

## Setting up Postgresql tables

1. Dockerfile:

```
FROM postgres:13.0

ADD /table/ /docker-entrypoint-initdb.d/tables/
ADD deploy_schemas.sql /docker-entrypoint-initdb.d/
```

2. Create table sql files

3. deploy_schemas.sql:

```
-- Deploy fresh database tables

\i '/docker-entrypoint-initdb.d/tables/users.sql'
\i '/docker-entrypoint-initdb.d/tables/login.sql'
```

4. Update docker-compose postgres service. Replace `image: postgres` -> `build: ./postgres` (referenceing postgres folder)

[More on SQL files](http://joshualande.com/create-tables-sql)
[More on initdb.d](https://hub.docker.com/_/postgres/)

5. Add seed data

    1. deploy_schemas.sql: `\i '/docker-entrypoint-initdb.d/seed/seed.sql'`
    2. Add seed folder and .sql file:
        ```
        BEGIN TRANSACTION;

        INSERT INTO users (name, email, entries, joined) values ('Jessie', 'j@test.com', 5, '2020-02-14');
        INSERT INTO logain (hash, email) values ('$2a$10$ttbgyCnQRD0xp7mTLDOZH.U0G5KRvZyY0vp8FFVcNi6IHgidk.Qsi', 'j@test.com');

        COMMIT;
        ```
    3. Dockerfile: ADD /seed/ /docker-entrypoint-initdb.d/seed/