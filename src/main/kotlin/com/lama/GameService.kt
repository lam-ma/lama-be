package com.lama

import kotlin.random.Random

interface GameService {
    fun startGame(quizzId: QuizzId): GameId
}

class GameServiceImpl : GameService {
    private val gameStorage = mutableMapOf<QuizzId, GameId>()

    override fun startGame(quizzId: QuizzId): GameId {
        val gameId = GameId(Integer.toHexString(Random.nextInt()).toString())
        gameStorage[quizzId] = gameId
        return gameId
    }
}
