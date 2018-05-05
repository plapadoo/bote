#!/usr/bin/env nix-shell
#!nix-shell -i bash -p openssh

set -eu
set -o pipefail

sudo docker build -t "$HOST_REGISTRY/bote:latest" .
sudo docker login -u "$USER_REGISTRY" -p "$PW_REGISTRY" "$HOST_REGISTRY"
sudo docker push "$HOST_REGISTRY/bote:latest"
ssh "$SSH_USER@$HOST_WWW" "docker rm -f bote || true"
ssh "$SSH_USER@$HOST_WWW" "docker rmi $HOST_REGISTRY/bote:latest || true"
ssh "$SSH_USER@$HOST_WWW" "docker login -u $USER_REGISTRY -p '$PW_REGISTRY' $HOST_REGISTRY"
ssh "$SSH_USER@$HOST_WWW" "docker run --publish 12649:8080 --volume /srv/bote-monica:/etc/bote --volume /srv/bote-monica:/var/lib/bote --name=bote --restart=always --detach $HOST_REGISTRY/bote"
