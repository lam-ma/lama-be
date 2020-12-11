package com.lama.domain

import com.lama.GameId
import com.lama.QuizzId

class QuizzNotFoundException(id: QuizzId) : RuntimeException("Quizz with $id not found")

class GameNotFoundException(id: GameId): RuntimeException("Game for gameId $id not found")

class GameUpdateException(message: String): RuntimeException(message)
