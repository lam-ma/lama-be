package com.lama

import com.lama.domain.GameNotFoundException
import com.lama.domain.GameUpdateException
import com.lama.service.GameStateListener
import java.lang.Integer.toHexString
import kotlin.random.Random.Default.nextInt

interface GameService {
    fun startGame(quizzId: QuizzId): Game
    fun get(gameId: GameId): Game
    fun update(gameId: GameId, stateChange: StateChange): Game
    fun getHighScore(gameId: GameId, limit: Int): HighScore

    fun joinGame(gameId: GameId, playerId: PlayerId, name: String)
    fun pickAnswer(playerId: PlayerId, questionId: QuestionId, answerId: AnswerId)
    fun leaveGame(playerId: PlayerId)
}

class GameServiceImpl(
    private val quizzService: QuizzService,
    private val gameStateListener: GameStateListener
) : GameService {
    private val gameStorage = mutableMapOf<GameId, Game>()
    private val playersStorage = mutableMapOf<PlayerId, Player>()

    override fun startGame(quizzId: QuizzId): Game {
        val quizz = quizzService.get(quizzId)
        val game = Game(GameId(nextId()), quizz, quizz.questions.first().id, GameState.QUESTION)
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
        val oldQuestion = stateChange.questionId
        val oldState = game.state
        game.currentQuestionId = stateChange.questionId
        game.state = stateChange.state
        // TODO: increment score

        gameStateListener.stateChanged(game, game.playerIds.map { playersStorage[it]!! })
        //        TODO: clean up the game after finish
        return game
    }

    override fun getHighScore(gameId: GameId, limit: Int): HighScore {
        get(gameId)
        return HighScore(List(limit) { PlayerScore(nextId(), nextInt(100)) }.sortedBy { it.score }.reversed())
    }

    override fun joinGame(gameId: GameId, playerId: PlayerId, name: String) {
        val game = get(gameId)
        val newPlayer = Player(playerId, name, gameId, 0, null, null)
        game.playerIds += playerId
        playersStorage[playerId] = newPlayer
        gameStateListener.stateChanged(game, listOf(newPlayer))
//        TODO: handle error
    }

    override fun leaveGame(playerId: PlayerId) {
        val player = playersStorage.remove(playerId)!!
        get(player.gameId).playerIds.remove(playerId)
    }

    override fun pickAnswer(playerId: PlayerId, questionId: QuestionId, answerId: AnswerId) {
        val player = playersStorage[playerId]!!
        player.lastQuestionId = questionId
        player.lastAnswerId = answerId
    }
}

fun nextId(): String = toHexString(nextInt()).toString()

fun Game.getCurrentQuestion(): Question = quizz.questions.find { it.id == currentQuestionId }!!

data class Player(
    val id: PlayerId,
    val name: String,
    val gameId: GameId,
    var score: Int,
    var lastQuestionId: QuestionId?,
    var lastAnswerId: AnswerId?
)

