#!/usr/bin/env nix-shell
#!nix-shell -i bash -p openssh

set -eu
set -o pipefail

sudo docker build -t "$HOST_REGISTRY/bote:latest" .
sudo docker login -u "$USER_REGISTRY" -p "$PW_REGISTRY" "$HOST_REGISTRY"
sudo docker push "$HOST_REGISTRY/bote:latest"
ssh "$SSH_USER@$HOST_WWW" "docker rm -f bote-dev || true"
ssh "$SSH_USER@$HOST_WWW" "docker rmi $HOST_REGISTRY/bote:latest || true"
ssh "$SSH_USER@$HOST_WWW" "docker login -u $USER_REGISTRY -p '$PW_REGISTRY' $HOST_REGISTRY"
ssh "$SSH_USER@$HOST_WWW" "docker run -p 12895:8080 -v /srv/bote:/etc/bote --name=bote-dev --restart=always -d $HOST_REGISTRY/bote"
