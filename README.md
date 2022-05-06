# HLR Bot
Telegram HLR Bot. This app can verify phone number's activity.
[BSG](https://bsg.world/) service is used.


## How to register a bot in Telegram
[https://core.telegram.org/bots](https://core.telegram.org/bots)

## How to build the app
```
./mvnw clean package
```

## How to run the app
```
java -Dbot.name=<YOUR_BOT_NAME> -Dbot.token=<YOUR_BOT_TOKEN> -jar hlr-bot.jar
```

## Requirements
- Java 11 or higher 
