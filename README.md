<div align="center">
  <img width="100" src="./docs/assets/chat.png">
  <br><br>
</div>

[![Github All Releases](https://img.shields.io/github/downloads/BrianJr03/Issa-Chat-App/total.svg)](https://github.com/BrianJr03/Issa-Chat-App/releases/latest)

# Issa Chat App

<div align="center">
  <a href='https://play.google.com/store/apps/details?id=jr.brian.issaaiapp&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
      <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height='80px'/>
    </a>

  <a href='https://github.com/BrianJr03/Issa-Chat-App/releases/latest'>
    <img alt='Get it on Github' src='https://raw.githubusercontent.com/ismartcoding/plain-app/main/assets/get-it-on-github.png' height='80px'/>
  </a>
  
</div>

## About the Project

This is a simple chat app that uses OpenAI's `GPT-3.5-turbo` API to generate responses.  

<div align="center">
  <img src="./docs/assets/main.jpeg" width="300"  />
  <img style="margin-left: 300px" src="./docs/assets/how-to-use.jpeg" width="300"/>
  <img style="margin-left: 300px" src="./docs/assets/settings.jpeg" width="300"/>
  <img style="margin-left: 300px" src="./docs/assets/multiple-convos.jpeg" width="300"/>
  <img style="margin-left: 300px" src="./docs/assets/select-theme.jpeg" width="300"/>
</div>

## Features

- Chat with ChatGPT
  - An OpenAI `API Key` is needed to chat with ChatGPT
  - Add `Conversational Context` to personalize responses from ChatGPT
    - Example: "You are my sarcastic assistant who only speaks Dutch"

- Chat Taps

  - Single tap to `toggle` a Chat's info

  - Double tap to `play` a Chat's text as audio
  
  - Long press to `copy` a Chat's text
  
- Settings Dialog

  - You can do a few things in `Settings`
    - Add & Update API Key
    - Remove all chats
    - Toggle `Auto-play`, which allows one to automatically play an incoming Chat's text

- Export Conversations - Download an entire `Conversation` as a JSON and PDF

- Theme Dialog - Select between 3 themes in `Theme`  

- Dark / Light Mode - Applied based on your system settings

- Persistent Chat - Chats will be saved automatically


## Prerequisites

- [Android Studio](https://developer.android.com/studio)
- Android SDK
- OpenAI API Key can be found at <https://platform.openai.com/account/api-keys>

## Installation

Feel free to download the latest release from one of the sources above.  
If you want to build it yourself, follow the steps below.

1. Clone the repo

   ```sh
   git clone https://github.com/BrianJr03/Issa-Chat-App.git
   ```

2. Open in Android Studio
3. Run on emulator or device

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM with Repository
- Coroutines
- RoomDB
- DaggerHilt

App Icon: <a href="https://www.flaticon.com/free-icons/question" title="question icons">Question icons created by dmitri13 - Flaticon</a>
