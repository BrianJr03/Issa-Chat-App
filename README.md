# Issa Chat App

A simple way to use chatGPT on Android

Find the [latest release](https://github.com/BrianJr03/Issa-Chat-App/releases) here

[![Github All Releases](https://img.shields.io/github/downloads/BrianJr03/Issa-AI-App/total.svg)]() 

![Main](https://github.com/BrianJr03/Issa-AI-App/blob/develop/main.png)
![How to Use](https://github.com/BrianJr03/Issa-AI-App/blob/develop/howtouse.png)

## Features
- ### Chat with chatGPT
  - Only an OpenAI API Key is needed to chat with chatGPT (`gpt-3.5-turbo`)
  - Add `Conversational Context` to personalize responses from chatGPT
    - Example: "You are my sarcastic assistant"

- ### Chat taps

  - Single tap to `toggle` a Chat's date and time

  - Double tap to `play` a Chat's text as audio
  
  - Long tap to `copy` a Chat's text
  
- ### Dark / Light mode
  - Based on your system settings, this app supports both dark and light mode
  
- ### Select app theme
  - Select between 3 themes in Theme dialog  
  
- ### Persistent Chat
  - Chats will be saved automatically
  
- ### Settings Dialog
  - You can do a few things in `Settings`
    - Add & Update API Key
    - Remove all chats
    - Toggle `Auto-play`, which allows one to automatically play an incoming Chat's text
    - Toggle a random `Conversational Context` being set on app start
  
- ### How to build and run the project
  - Run the code as you usually would in Android Studio. After installation, you'll need to save your API Key within the application.
  - An OpenAI API Key can be found at https://platform.openai.com/account/api-keys

### Tech Stack
 - Kotlin
 - Jetpack Compose
 - MVVM with Repository
 - Coroutines
 - RoomDB
 - DaggerHilt
