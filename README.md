# Issa-AI-App

A simple way to use chatGPT on Android

## Features
- ### Chat with chatGPT
  - Only an OpenAI API Key is needed to chat with chatGPT (gpt-3.5-turbo)
  - Add `Conversational Context` to personalize responses from chatGPT
    - Example: "You are my sarcastic assistant"
  
- ### Long Press to copy chats
  - Long-Pressing will copy a chat's text
  
- ### Dark / Light mode
  - Based on your system settings, this app supports both dark and light mode
  
- ### Persistent Chat
  - Chats will be saved automatically
  
- ### Settings Dialog
  - You can do a few things in `Settings`
    - Clear API Key
    - Remove all chats
    - Toggle being greeted on app start
    - Toggle a random `Conversational Context` being set on app start
    - Update your API Key
  
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
