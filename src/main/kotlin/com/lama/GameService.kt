package com.lama

import java.lang.Integer.toHexString
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

interface GameService {
    fun startGame(quizzId: QuizzId): Game
    fun get(gameId: GameId): Game?
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
}

fun nextId(): String = toHexString(nextInt()).toString()
