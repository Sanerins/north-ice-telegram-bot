# Telegram Booking Bot

A Spring Boot-based Telegram bot for handling booking requests and customer interactions, built with Java 21 and Docker containerization.

## Features

- **Interactive Booking System**: Guided conversation flow for different types of bookings
- **Multiple Booking Types**: Support for personal bookings, child bookings, and call requests
- **Admin Controls**: System and report hooks for monitoring and management
- **Persistence**: MapDB database with Docker volume persistence
- **Dockerized**: Easy deployment with Docker Compose
- **Spring Boot**: Full Spring framework integration with dependency injection

## Prerequisites

- Java 21 JDK
- Docker and Docker Compose
- Telegram Bot Token (from [BotFather](https://t.me/BotFather))
- Gradle (for building)

## Setup Instructions

### 1. Create Your Telegram Bot

1. Message [@BotFather](https://t.me/BotFather) on Telegram
2. Use `/newbot` command to create a new bot
3. Copy the bot token and username provided

### 2. Configure Environment

Create a `.env` file in the project root:

```env
TELEGRAM_BOT_TOKEN=your_actual_bot_token_here
TELEGRAM_BOT_USERNAME=your_bot_username_here
TELEGRAM_BOT_CREATOR_ID=your_telegram_user_id_here
```

### 3. Build and Run

```bash
# Make the run script executable (first time only)
chmod +x run.sh

# Build and start the application
./run.sh
```

### 4. Verify Deployment

Check if the container is running:
```bash
docker-compose ps
```

View logs:
```bash
docker-compose logs -f
```

## Bot Commands

### User Commands
- `/start` - Begin the booking process
- Interactive buttons throughout the conversation flow

### Admin Commands (Implicit from AbilityBot)
- `/claim` - Claim bot ownership (creator only)
- `/report` - View all available commands
- `/commands` - List commands based on user privacy level
- `/backup` - Backup bot database
- `/recover` - Restore database from backup
- `/promote @username` - Promote user to admin
- `/demote @username` - Demote admin to user
- `/ban @username` - Ban user from bot access
- `/unban @username` - Unban user
- `/report_hook` - Set up report channel (creator only)
- `/system_hook` - Set up system monitoring channel (admin only)

## Booking Flow

The bot guides users through a structured conversation:

1. **Start**: User initiates with `/start`
2. **Booking Type Selection**: Choose between:
    - Personal booking
    - Child booking
    - Call request
3. **Privacy Agreement**: User must accept privacy terms
4. **Information Collection**:
    - Age verification
    - Contact number
    - Name
5. **Confirmation**: Final confirmation and data submission

## Database Persistence

The bot uses MapDB with persistent storage in a Docker volume:
- Database file: `/data/bot.db`
- Persists across container restarts
- Automatic backups via `/backup` command

## Project Structure

```
src/main/java/
├── BookingBotApplication.java      # Main application class
├── BookingBot.java                 # Main bot handler (extends AbilityBot)
├── BookingHandler.java            # Conversation logic handler
├── services/                      # Service classes
├── model/                         # Data models
└── utils/                         # Utility classes
```

## Docker Configuration

- **Port**: 8080 (Spring Boot actuator health checks)
- **Volume**: `bot-data` for database persistence
- **Restart Policy**: Automatic restart on failure

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `TELEGRAM_BOT_TOKEN` | Bot authentication token | Yes |
| `TELEGRAM_BOT_USERNAME` | Bot username (@ prefix) | Yes |
| `TELEGRAM_BOT_CREATOR_ID` | Your Telegram user ID | Yes |

## Development

### Building Manually

```bash
# Build with Gradle
./gradlew clean build

# Run tests
./gradlew test

# Build without tests
./gradlew build -x test
```

### Docker Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f

# Rebuild container
docker-compose build --no-cache

# Access container shell
docker-compose exec telegram-bot sh
```

### Database Management

```bash
# Backup database (from within bot)
/backup

# Check volume contents
docker run --rm -v telegram-bot_bot-data:/data alpine ls -la /data
```

## Troubleshooting

### Common Issues

1. **Bot doesn't respond**:
    - Check if token is correct in `.env`
    - Verify Docker container is running

2. **Database permissions error**:
    - Ensure Docker volume is created properly
    - Check `/data` directory permissions in container

3. **Build failures**:
    - Verify Java 21 is installed
    - Check Gradle wrapper permissions

### Logs and Monitoring

- Application logs: `./logs/` directory
- Docker logs: `docker-compose logs -f`
- Health check: `http://localhost:8080/actuator/health`

## Support

For issues and questions:
1. Check application logs
2. Verify environment variables
3. Ensure Docker daemon is running
4. Confirm Telegram bot token is valid

## License

This project is proprietary software. All rights reserved.

---

**Note**: Remember to keep your `.env` file secure and never commit it to version control. The `.gitignore` file is configured to exclude sensitive files.