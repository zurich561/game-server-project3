# Wordle — Full-Stack Web App - Project 3

## Team Members
Roberto Panizza
Loris Trifoglio

## Welcome
This project is Wordle with a clean architecture and a simple promise: **the server is the referee, the browser is the stage**.

You type a guess, hit Send (or Enter), and the app responds with the classic Wordle feedback:

- **Green** = correct letter in the correct position  
- **Orange** = correct letter in the wrong position  
- **Gray** = letter not in the word  

Behind the scenes, the backend generates the secret word, validates guesses, stores game state, and returns an updated game snapshot after every move.
The frontend stays lightweight: it renders what the backend decides.

## What’s Inside This Repo?
- `aigs-spring-server/` — Spring Boot REST API (users + games + engines)
- `aigs-react-frontend/` — React (Vite) client for playing Wordle

## Features
- User registration, login, logout (token based)
- Start and quit Wordle games via REST endpoints
- Difficulty selection
- Physical keyboard + on-screen keyboard
- Win/Loss popup and automatic cleanup
- Invalid word feedback when `currentWordExists` is false
- Backend testing panel (Ping + H2 console link)

## Quick Start (Run Locally)

### 1) Clone
```bash
git clone https://github.com/zurich561/game-server-project3
cd https://github.com/zurich561/game-server-project3
```

### 2) Run the Backend (Spring Boot)

**Requirements**
- Java 17+
- Maven

```bash
cd aigs-spring-server
mvn spring-boot:run
```

Expected backend URL:  
`http://localhost:50005`

### 3) Run the Frontend (React + Vite)

**Requirements**
- Node.js (LTS)
- npm

```bash
cd aigs-react-frontend
npm install
npm run dev
```

Open the printed Vite URL, usually:  
`http://localhost:5173`

## Architecture Overview
```text
[Browser]
  React (Vite)
    - Auth modal
    - Game setup
    - Wordle grid + keyboard
    - Talks to REST API
        |
        | JSON over HTTP
        v
[Spring Boot Backend]
  UserController     GameController
        |                 |
        v                 v
  UserRepository      GameRepository
        |                 |
        +------ H2 In-Memory Database -----+
```

## Backend API (High Level)

### User Endpoints
- `POST /users/register` — Create a user
- `POST /users/login` — Login and receive a token
- `POST /users/logout` — Logout (token cleared server-side)
- `GET /ping` — Health check

### Game Endpoints
- `POST /game/new` — Create a new game (Wordle)
- `POST /game/move` — Submit a guess
- `POST /game/quit` — Quit and remove the game

## The Wordle Engine (How the Backend Thinks)

### Starting a Game
When you press Start, the frontend sends for example:

```json
{
  "token": "<your-token>",
  "gameType": "Wordle",
  "difficulty": 1
}
```

On the backend:
- The token is validated (`Token.validate` checks it looks like hex and belongs to a user).
- The Wordle engine is selected (`GameEngine.getGameEngine(GameType.Wordle)`).
- `Wordle.newGame()` initializes the game:
  - sets board size (rows and columns)
  - sets `result = null` (game in progress)
  - writes an options JSON string with the Wordle state

### Word Generation
The backend fetches a random word from:  
`https://random-word-api.vercel.app`

That word becomes `solutionWord` inside the game’s options.

### Guess Evaluation (Two-Pass Scoring)
When you submit a guess, the backend scores each letter.

**Pass 1 — Exact hits (correct)**  
If `guess[i] == solution[i]`, the letter is correct.

**Pass 2 — Present elsewhere vs missing (absent)**  
For remaining letters, the engine counts the remaining letters in the solution.

- If the guessed letter exists in the remaining pool, it is marked **present** (and the count is decremented).
- Otherwise, it is marked **absent**.

This is the standard Wordle approach and correctly handles repeated letters.

### Word Existence Feedback (`currentWordExists`)
The backend checks word existence via a dictionary API and returns:

```json
{
  "currentWordExists": false
}
```

When that happens, the frontend:
- shakes the active row
- highlights tiles with a red border
- shows: “Sorry, this word doesn’t exist!”
- clears the input after a short delay

### Game End
The backend sets:
- `result = true` when the solution is guessed
- `result = false` when max tries are used

The frontend shows a popup and then quits the game automatically.

## H2 Database Console
You can open the H2 console at:  
`{backendBaseUrl}/h2-console`

Example:  
`http://localhost:50005/h2-console`

## Troubleshooting

### I can’t start a game
- Make sure the backend is running.
- If you’re not logged in, the login modal opens automatically.
- Verify the backend URL in the backend-testing section.

### Enter key doesn’t submit
- Send triggers only when the guess length matches the board word length.

## Closing
This project is intentionally focused: one authoritative engine and a frontend that stays fast by rendering server state.
The result is a Wordle that’s fun to play, easy to reason about, and straightforward to extend.
