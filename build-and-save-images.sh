#!/bin/bash

echo "=== 1. Building Docker Images (Target: linux/amd64) ==="

# --platform linux/amd64 옵션을 추가하여 서버(Intel/AMD) 환경에 맞춰 빌드합니다.
# 로컬이 Mac(ARM)이어도 서버용(AMD64)으로 빌드됩니다.

echo "Building Core Service..."
docker build --platform linux/amd64 -t tourai-core:latest ./core

echo "Building API Gateway Service..."
docker build --platform linux/amd64 -t tourai-api-gateway:latest ./api-gateway

echo "Building Chat Server Service..."
docker build --platform linux/amd64 -t tourai-chat:latest ./chat-server

echo "=== 2. Saving Images to tar file ==="
echo "Saving images to tourai-images.tar..."
docker save -o tourai-images.tar tourai-core:latest tourai-api-gateway:latest tourai-chat:latest

echo "=== Done! ==="
echo "You can now transfer 'tourai-images.tar' and 'docker-compose-prod.yml' to your server."
