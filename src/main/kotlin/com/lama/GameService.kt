package com.lama

import java.lang.Integer.toHexString
import kotlin.random.Random.Default.nextInt

interface GameService {
    fun startGame(quizzId: QuizzId): Game
    fun get(gameId: GameId): Game
    fun update(gameId: GameId, stateChange: StateChange): Game
    fun getHighScore(gameId: GameId, limit: Int): HighScore
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
        gameStorage[game.id] = game
        return game
    }

    override fun get(gameId: GameId): Game =
        gameStorage[gameId] ?: throw GameNotFoundException(gameId)

    override fun update(gameId: GameId, stateChange: StateChange): Game {
        val game = get(gameId)
        if (game.quizz.questions.none { it.id == stateChange.questionId }) {
            throw GameUpdateException("Question ${stateChange.questionId} does not belong to game $gameId")
        }
        val updatedGame = game.copy(currentQuestionId = stateChange.questionId, state = stateChange.state)
        gameStorage[gameId] = updatedGame
        return updatedGame
    }

    override fun getHighScore(gameId: GameId, limit: Int): HighScore {
        get(gameId)
        return HighScore(List(limit) { PlayerScore(nextId(), nextInt(100)) }.sortedBy { it.score }.reversed())
    }
}

fun nextId(): String = toHexString(nextInt()).toString()
