package com.lama

import kotlin.random.Random

interface GameService {
    fun startGame(quizzId: QuizzId): GameId
}

class GameServiceImpl(
    val quizzService: QuizzService
) : GameService {
    private val gameStorage = mutableMapOf<QuizzId, GameId>()

    override fun startGame(quizzId: QuizzId): GameId {
        quizzService.get(quizzId)

        val gameId = GameId(Integer.toHexString(Random.nextInt()).toString())
        gameStorage[quizzId] = gameId
        return gameId
    }
}
