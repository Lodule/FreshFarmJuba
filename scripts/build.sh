#!/bin/bash
set -e

echo "Starting build process..."

# Clean and Build
./gradlew clean build -x test

# Build Docker image
docker build -t freshfarmjuba:latest .

echo "Build completed successfully!"
