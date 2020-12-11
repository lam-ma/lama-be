package com.lama

import java.lang.Integer.toHexString
import kotlin.random.Random.Default.nextInt

interface GameService {
    fun startGame(quizzId: QuizzId): Game
    fun get(gameId: GameId): Game?
    fun getHighScore(gameId: GameId, limit: Int): HighScore
}

class GameServiceImpl(
    val quizzService: QuizzService
) : GameService {
    private val gameStorage = mutableMapOf<Game, QuizzId>()

    override fun startGame(quizzId: QuizzId): Game {
        val quizz = quizzService.get(quizzId)
        val game = Game(
            GameId(nextId()),
            quizz.questions.first().id,
            quizz,
            GameState.QUESTION
        )
        gameStorage[game] = quizzId
        return game
    }

    override fun get(gameId: GameId): Game? {
        return gameStorage.filterKeys { it.id == gameId }.keys.firstOrNull()
    }

    override fun getHighScore(gameId: GameId, limit: Int): HighScore {
        get(gameId) ?: throw GameNotFoundException("Game with id $gameId doesn't exist")
        return HighScore(List(limit) { PlayerScore(nextId(), nextInt(100)) }.sortedBy { it.score }.reversed())
    }
}

fun nextId(): String = toHexString(nextInt()).toString()
