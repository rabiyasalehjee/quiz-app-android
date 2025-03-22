# Trivia Quiz App (Android)

An engaging Android trivia app powered by a local LLM-based backend. The app delivers dynamic, multiple-choice trivia questions in real time by calling a custom API that uses an open-source language model to generate content.

## 🎮 Features

- Interactive trivia quiz with real-time question generation
- Powered by a local LLM (Large Language Model)
- Multiple-choice answer interface with visual feedback
- "Skip" feature to move past tricky questions
- Score summary with detailed answer review
- Smooth UI transitions with animated loading messages

## 📲 How to Run This App

### 1. Clone the Android App Repo

```bash
git clone https://github.com/rabiyasalehjee/quiz-app-android
```

### 2. Set Up the LLM API

This app requires a local API that uses an LLM to generate trivia questions on demand.

👉 LLM API Source Code:
```
https://github.com/rabiyasalehjee/LLM-Trivia-API
```

Follow the setup guide in that repo to install the required model `(e.g., deepseek-r1:1.5b via ollama)` and run the server:

```
python flask_api.py
```
🛠 Tech Stack
- Java + Android SDK (App)
- Retrofit2 for networking
- Local Flask API (Backend)
- Ollama + deepseek-r1:1.5b (LLM model for trivia generation)