package com.lama

import java.lang.Integer.toHexString
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

interface GameService {
    fun startGame(quizzId: QuizzId): Game
    fun get(gameId: GameId): Game?
    fun update(gameId: GameId, stateChange: StateChange): Game
}

class GameServiceImpl(
    private val quizzService: QuizzService
) : GameService {
    private val gameStorage = mutableMapOf<GameId, Game>()

    override fun startGame(quizzId: QuizzId): Game {
        val quizz = quizzService.get(quizzId)
        val game = Game(
            GameId(nextId()),
            quizz.questions.first().id,
            quizz,
            GameState.QUESTION
        )
        gameStorage[gameId] = game
        return game
    }

    override fun get(gameId: GameId): Game? {
        return gameStorage[gameId]
    }

    override fun update(gameId: GameId, stateChange: StateChange): Game {
        val game = get(gameId) ?: throw GameNotFoundException("Game for gameId $gameId not found")
        if (game.quizz.questions.none { it.id == stateChange.questionId }) {
            throw GameUpdateException("Question ${stateChange.questionId} does not belong to this game")
        }
        val updatedGame = game.copy(currentQuestionId = stateChange.questionId, state = stateChange.state)
        gameStorage[gameId] = updatedGame
        return updatedGame
    }
}

fun nextId(): String = toHexString(nextInt()).toString()
