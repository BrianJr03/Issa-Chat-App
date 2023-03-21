# Issa-AI-App

A simple way to use chatGPT on Android

## Features
- ### Chat with chatGPT
  - Only an OpenAI API Key is needed to chat with chatGPT (gpt-3.5-turbo)
  - Add Conversational Context to personalize responses from chatGPT
    - Example: "You are my sarcastic assistant"
  
- ### Long Press to copy chats
  - Long-Pressing will copy a chat's text
  
- ### Dark / Light mode
  - Based on your system settings, this app supports both dark and light mode
  
- ### How to build and run the project
  - 1. Once the code is loaded into Android Studio, you'll need to provide an OpenAI API Key 
  which can be found at https://platform.openai.com/account/api-keys
  - 2. Navigate to `MainViewModel` and update `key` with your API Key
  - 3. If you wish to hide your API Key in `BuildConfig.java`, instructions on how to do so can be found here
  https://www.youtube.com/watch?v=-2ckvIzs0nU&t=524s

### Tech Stack
 - Kotlin
 - Jetpack Compose
 - MVVM
 - Coroutines
