package com.lama

import kotlin.random.Random

interface GameService {
    fun startGame(quizzId: QuizzId): GameId
    fun get(gameId: GameId): Game?
}

class GameServiceImpl(
    val quizzService: QuizzService
) : GameService {
    private val gameStorage = mutableMapOf<Game, QuizzId>()

    override fun startGame(quizzId: QuizzId): GameId {
        val quizz = quizzService.get(quizzId)

        val gameId = GameId(Integer.toHexString(Random.nextInt()).toString())
        val game = Game(
            gameId,
            quizz.questions.first().id,
            quizz,
            GameState.QUESTION
        )
        gameStorage[game] = quizzId
        return gameId
    }

    override fun get(gameId: GameId): Game? {
        return gameStorage.filterKeys { it.id == gameId }.keys.firstOrNull()
    }
}
