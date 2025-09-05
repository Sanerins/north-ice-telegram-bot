#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${RED}Error: .env file not found!${NC}"
    echo "Creating template .env file..."
    cat > .env << EOL
TELEGRAM_BOT_TOKEN=your_telegram_bot_token_here
TELEGRAM_BOT_USERNAME=your_bot_username_here
TELEGRAM_BOT_CREATOR_ID=your_creator_id_here
EOL
    echo -e "${YELLOW}Please edit the .env file with your actual values and run again.${NC}"
    exit 1
fi

echo -e "${GREEN}Building the project with Gradle...${NC}"

# Build the project using Gradle
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo -e "${YELLOW}Build failed! Check for errors above.${NC}"
    exit 1
fi

echo -e "${GREEN}Build successful! Starting Docker containers...${NC}"

# Build and start containers
docker-compose up --build -d