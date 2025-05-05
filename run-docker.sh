#!/bin/bash

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install Docker."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose is not installed. Please install Docker Compose."
    exit 1
fi

# Build and run the application using Docker Compose
echo "Building and running the application using Docker Compose..."
docker-compose up --build -d

# Wait for the application to start
echo "Waiting for the application to start..."
sleep 10

# Check if the application is running
if docker-compose ps | grep -q "app.*Up"; then
    echo "Application is running. You can access it at http://localhost:8080/api"
    echo "To view logs, run: docker-compose logs -f app"
    echo "To stop the application, run: docker-compose down"
else
    echo "Application failed to start. Check logs with: docker-compose logs app"
fi
