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