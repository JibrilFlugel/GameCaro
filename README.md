# Gomoku Game

A **Gomoku (Caro)** game implemented using **LibGDX**, supporting both **multiplayer over a local network** (via UDP sockets) and **single-player mode with AI**. The AI uses the **Minimax algorithm** with **Alpha-Beta Pruning** for efficient decision-making.

---

## Features

- **AI Mode**: Play against an intelligent AI using Minimax with Alpha-Beta pruning.
- **Multiplayer Mode**: Play with friends over a **local network** using sockets.
- **Modern UI**: Clean game board with animated X and O pieces.
- **Win Detection**: Automatic detection of win conditions.

---

## Screenshots

_Will add some later_

---

## Technologies Used

- **Java**
- **LibGDX** (game framework)
- **Java Sockets** (for local multiplayer)
- **Minimax + Alpha-Beta Pruning** (for AI)

---

## AI Logic

The AI uses the Minimax algorithm to evaluate the game board, searching possible future moves up to a certain depth, with optimizations from Alpha-Beta pruning to reduce the number of explored nodes and improve performance.

---

## Multiplayer Setup

The game supports **local network multiplayer** via UDP sockets.

### Host:
- Binds a `DatagramSocket` to a port and listens for messages.
- Manages turn order and game state.

### Client:
- Sends messages to the host's IP and port.
- Synchronizes game state based on responses.

---

## Future improvements:
- Sound effects and music
- Zobrist hashing
