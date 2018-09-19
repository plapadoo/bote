#!/usr/bin/env nix-shell
#!nix-shell -i bash -p openssh

set -eu
set -o pipefail

docker build -t "$HOST_REGISTRY/bote:latest" .
docker login -u "$USER_REGISTRY" -p "$PW_REGISTRY" "$HOST_REGISTRY"
docker push "$HOST_REGISTRY/bote:latest"
ssh "$SSH_USER@$HOST_WWW" "docker rm -f $INSTANCES || true"
ssh "$SSH_USER@$HOST_WWW" "docker rmi $HOST_REGISTRY/bote:latest || true"
ssh "$SSH_USER@$HOST_WWW" "docker login -u $USER_REGISTRY -p '$PW_REGISTRY' $HOST_REGISTRY"
port=12649
for instance in $INSTANCES
do
  ssh "$SSH_USER@$HOST_WWW" "docker run --publish $port:8080 --volume /srv/$instance:/etc/bote --volume /srv/$instance:/var/lib/bote --name=$instance --restart=always --detach $HOST_REGISTRY/bote"
  ((port++))
done
