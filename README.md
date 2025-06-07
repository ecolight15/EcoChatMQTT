# EcoChatMQTT

EcoChatMQTT is a Minecraft plugin that provides advanced chat functionality with cross-server communication using MQTT protocol. It allows multiple Minecraft servers to share chat messages, private messages, and other chat-related data in real-time.

## Features

### Core Chat System
- **Cross-server chat**: Communication between multiple Minecraft servers via MQTT
- **Channel system**: Support for multiple chat channels (Global, World, Local)
- **Private messaging**: Direct player-to-player messaging across servers
- **Chat history**: Persistent storage and retrieval of chat messages
- **Dice rolling**: Built-in dice command for gaming purposes

### Channel Management
- **Multiple channel types**:
  - **Global**: Messages visible to all players on all servers
  - **World**: Messages visible to players in the same world
  - **Local**: Messages visible to players within a defined range
- **Channel configuration**: Customizable enter/leave messages, welcome/goodbye messages
- **Permission-based access**: Control who can join specific channels
- **Password protection**: Optional password-protected channels
- **Auto-join**: Automatic channel joining on player login

### Advanced Features
- **Message filtering**: Block unwanted users and content
- **Admin spy modes**: Monitor private messages and channel activity
- **Range-based local chat**: Configurable distance for local chat
- **Database storage**: SQLite or MySQL support for persistent data
- **Real-time synchronization**: Instant message delivery across servers

## Dependencies

This plugin requires the following dependencies to be installed:

- **EcoFramework** (v0.28 or higher)
- **EcoMQTT** (v0.10 or higher)
- **EcoMQTTServerLog** (v0.7 or higher) - Optional
- **Spigot/Paper** (1.18.2 or compatible)

## Installation

1. Download the EcoChatMQTT plugin JAR file
2. Install all required dependencies (EcoFramework, EcoMQTT)
3. Place the plugin JAR in your server's `plugins/` directory
4. Configure your MQTT broker settings in the EcoMQTT plugin
5. Start the server to generate default configuration files
6. Configure the plugin as described below
7. Restart the server or use `/ecms reload`

## Configuration

### Main Configuration (config.yml)

```yaml
# Enable/disable the plugin
Enabled: false

# Date format for timestamps
DateFormat: "yyyy/MM/dd HH:mm:ss.SSS"

# MQTT topic configuration
Topic:
  Chat:
    Enable: true
    Format: "{server}/p/{plugin}/chat"
    URL: ""
  Config:
    Enable: true
    Format: "{server}/p/{plugin}/config"
    URL: ""

# MQTT Quality of Service settings
Mqtt:
  Publish:
    QoS: 1
  Subscribe:
    QoS: 1

# Database configuration
Database:
  type: "sqlite"
  name: "chat.db"
  server: "localhost:port"
  user: "user"
  pass: "pass"

# Default channel settings
ChannelDefault:
  Type: "global"
  EnterMessage: "{PLAYER} join the {NAME} channel."
  LeaveMessage: "{PLAYER} leave the {NAME} channel."
  WelcomeMessage: "Welcome, {TAG} Channel."
  GoodbyeMessage: "Goodbye, {TAG} Channel."
  AutoJoin: false
  ListEnabled: false
  AddReqPerm: false
  Activate: true
```

### Default Channels (default.yml)

The plugin comes with three predefined channels:

- **G (General)**: Global chat visible to all players on all servers
- **W (World)**: World-specific chat for players in the same world
- **L (Local)**: Local chat for players within a specific range

## Commands

### Basic Chat Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/join <channel>` | Join a chat channel | `ecochatmqtt.chat.join` |
| `/leave <channel>` | Leave a chat channel | `ecochatmqtt.chat.leave` |
| `/passjoin <channel> <password>` | Join password-protected channel | `ecochatmqtt.chat.passjoin` |
| `/pm <player> <message>` | Send private message | `ecochatmqtt.chat.pm` |
| `/rs <message>` | Reply to last private message | `ecochatmqtt.chat.rs` |
| `/cc <channel>` | Switch active channel | `ecochatmqtt.chat.cc` |
| `/ecc` | Show current channel info | `ecochatmqtt.chat.ecc` |

### Channel Management

| Command | Description | Permission |
|---------|-------------|------------|
| `/channel name <channel> <name>` | Set channel display name | `ecochatmqtt.chat.channel.name` |
| `/channel type <channel> <type>` | Set channel type | `ecochatmqtt.chat.channel.type` |
| `/channel color <channel> <color>` | Set channel color | `ecochatmqtt.chat.channel.color` |
| `/channel list` | List available channels | `ecochatmqtt.chat.channel.list` |
| `/channel welcome <channel> <message>` | Set welcome message | `ecochatmqtt.chat.channel.welcome` |
| `/channel goodbye <channel> <message>` | Set goodbye message | `ecochatmqtt.chat.channel.goodbye` |
| `/channel owner <channel> <player>` | Set channel owner | `ecochatmqtt.chat.channel.owner` |
| `/channel perm <channel> <permission>` | Set channel permissions | `ecochatmqtt.chat.channel.perm` |

### History and Information

| Command | Description | Permission |
|---------|-------------|------------|
| `/history [page/date]` | View chat history | `ecochatmqtt.chat.history` |
| `/pmhistory <player> [page]` | View private message history | `ecochatmqtt.chat.pmhistory` |
| `/delete <message_id>` | Delete a message | `ecochatmqtt.chat.delete` |
| `/dice [sides]` | Roll dice | `ecochatmqtt.chat.dice` |

### Configuration Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ecms reload` | Reload plugin configuration | `ecochatmqtt.reload` |
| `/conf channel <settings>` | Configure channel settings | `ecochatmqtt.chat.conf.channel` |
| `/conf player <settings>` | Configure player settings | `ecochatmqtt.chat.conf.player` |
| `/conf flag <settings>` | Configure flag settings | `ecochatmqtt.chat.conf.flag` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/add <player> <channel>` | Add player to channel | `ecochatmqtt.chat.add` |
| `/set <player> <channel>` | Set player's active channel | `ecochatmqtt.chat.set` |

## MQTT Communication

The plugin uses MQTT topics for cross-server communication:

- **Chat Topic**: `{server}/p/{plugin}/chat` - Used for chat messages
- **Config Topic**: `{server}/p/{plugin}/config` - Used for configuration updates

### Topic Format Variables
- `{server}`: Server name identifier
- `{plugin}`: Plugin name (EcoChatMQTT)

## Channel Types

### Global Channels
Messages are visible to all players on all connected servers.

### World Channels  
Messages are only visible to players in the same world.

### Local Channels
Messages are only visible to players within a configurable distance range.

## Database

The plugin supports both SQLite and MySQL databases for storing:
- Chat messages and history
- Channel configurations
- User preferences
- Private message logs
- Channel memberships

### SQLite (Default)
No additional setup required. Database file is created automatically.

### MySQL
Configure database connection details in `config.yml`:
```yaml
Database:
  type: "mysql"
  name: "ecochat"
  server: "localhost:3306"
  user: "username"
  pass: "password"
```

## Permissions

### Basic Permissions
- `ecochatmqtt.chat.*` - Access to all chat commands
- `ecochatmqtt.chat.join` - Join channels
- `ecochatmqtt.chat.leave` - Leave channels
- `ecochatmqtt.chat.pm` - Send private messages
- `ecochatmqtt.chat.history` - View chat history

### Admin Permissions
- `ecochatmqtt.reload` - Reload plugin configuration
- `ecochatmqtt.chat.channel.*` - Full channel management
- `ecochatmqtt.chat.conf.*` - Configuration access
- `ecochatmqtt.chat.add` - Add players to channels

## License

This project is licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0). See the [LICENSE](LICENSE) file for details.

## Author

**ecolight** - Plugin developer and maintainer

## Version

Current version: 0.7

## Support

For issues, feature requests, or support, please refer to the project repository or contact the plugin author.
