#!/bin/bash

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Node.js is not installed. Please install Node.js."
    exit 1
fi

# Check Node.js version
node_version=$(node -v | cut -d 'v' -f 2)
echo "Node.js version: $node_version"

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "npm is not installed. Please install npm."
    exit 1
fi

# Kill any process running on port 3002
echo "Checking for processes on port 3002..."
pid=$(lsof -t -i:3002)
if [ -n "$pid" ]; then
    echo "Killing process $pid on port 3002..."
    kill -9 $pid
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

# Start the development server
echo "Starting the development server on port 3002..."
npm start
