package com.lama

import kotlin.random.Random

interface GameService {
    fun startGame(quizzId: QuizzId): GameId
    fun get(gameId: GameId): Game?
    fun update(gameId: GameId, stateChange: StateChange): Game
}

class GameServiceImpl(
    private val quizzService: QuizzService
) : GameService {
    private val gameStorage = mutableMapOf<GameId, Game>()

    override fun startGame(quizzId: QuizzId): GameId {
        val quizz = quizzService.get(quizzId)

        val gameId = GameId(Integer.toHexString(Random.nextInt()).toString())
        val game = Game(
            gameId,
            quizz.questions.first().id,
            quizz,
            GameState.QUESTION
        )
        gameStorage[gameId] = game
        return gameId
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
