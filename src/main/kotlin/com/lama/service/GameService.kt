package com.lama

import com.lama.domain.AnswerGivenMessage
import com.lama.domain.ChangeGameStateCommand
import com.lama.domain.ClientCommand
import com.lama.domain.CreateGameCommand
import com.lama.domain.GameNotFoundException
import com.lama.domain.GameStateMessage
import com.lama.domain.GameUpdateException
import com.lama.domain.JoinGameCommand
import com.lama.domain.LeaveGameCommand
import com.lama.domain.PickAnswerCommand
import com.lama.domain.PlayerJoinedMessage
import com.lama.domain.ServerMessage
import com.lama.service.PlayerGateway
import java.lang.Integer.toHexString
import java.util.TreeSet
import kotlin.random.Random.Default.nextInt

interface GameService {
    fun startGame(quizzId: QuizzId, hostId: PlayerId?): Game
    fun get(gameId: GameId): Game
    fun update(gameId: GameId, stateChange: StateChange): Game
    fun getHighScore(gameId: GameId, limit: Int): List<PlayerScore>

    fun handle(playerId: PlayerId, command: ClientCommand)
}

class GameServiceImpl(
    private val quizzService: QuizzService,
    private val playerGateway: PlayerGateway
) : GameService {
    private val gameStorage = mutableMapOf<GameId, Game>()
    private val playersStorage = mutableMapOf<PlayerId, Player>()
    private val highScore = TreeSet(compareByDescending<Pair<Int, PlayerId>> { it.first }.thenBy { it.second.value })

    override fun startGame(quizzId: QuizzId, hostId: PlayerId?): Game {
        val quizz = quizzService.get(quizzId)
        val game = Game(GameId(nextId()), quizz, quizz.questions.first().id, GameState.QUESTION, hostId)
        gameStorage[game.id] = game
        if (hostId != null) {
            playerGateway.send(hostId, getMessage(game, hostId))
        }
        return game
    }

    override fun get(gameId: GameId): Game =
        gameStorage[gameId] ?: throw GameNotFoundException(gameId)

    override fun update(gameId: GameId, stateChange: StateChange): Game {
        val game = get(gameId)
        if (game.state == GameState.FINISH) {
            throw GameUpdateException("Game $gameId is already finished")
        }
        if (game.quizz.questions.none { it.id == stateChange.questionId }) {
            throw GameUpdateException("Question ${stateChange.questionId} does not belong to game $gameId")
        }
        if (game.currentQuestionId != stateChange.questionId) {
            game.answersGiven.clear()
        }
        game.currentQuestionId = stateChange.questionId
        game.state = stateChange.state

        game.playerIds.forEach {
            playerGateway.send(it, getMessage(game, it))
        }
        if (game.hostId != null) {
            playerGateway.send(game.hostId, getMessage(game, game.hostId))
        }
        //        TODO: clean up the game after finish
        return game
    }

    private fun getMessage(game: Game, playerId: PlayerId): ServerMessage {
        val currentQuestion = game.getCurrentQuestion()
        val rightAnswerIds = currentQuestion.answers.filter { it.isRight }.map { it.id }
        val player = playersStorage[playerId]
        val selectedAnswerId = if (player?.lastQuestionId == game.currentQuestionId) player.lastAnswerId else null
        val isHost = playerId == game.hostId
        val isAnswerOrFinish = game.state in setOf(GameState.ANSWER, GameState.FINISH)
        val highScore = if (isAnswerOrFinish && isHost) getHighScore(game.id, 10) else null
        return GameStateMessage(
            game.id,
            game.state,
            game.quizz.title,
            currentQuestion,
            rightAnswerIds.takeIf { game.state == GameState.ANSWER },
            selectedAnswerId,
            highScore,
            game.answersGiven.takeIf { isHost }
        )
    }

    override fun getHighScore(gameId: GameId, limit: Int): List<PlayerScore> =
        highScore.take(limit).map { (score, id) ->
            PlayerScore(playersStorage[id]?.name.orEmpty(), score)
        }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun handle(playerId: PlayerId, command: ClientCommand) {
        val res = when (command) {
            is JoinGameCommand -> joinGame(command.gameId, playerId, command.name)
            is PickAnswerCommand -> pickAnswer(playerId, command.questionId, command.answerId)
            is LeaveGameCommand -> leaveGame(playerId)
            is CreateGameCommand -> startGame(command.quizzId, playerId)
            is ChangeGameStateCommand -> update(command.gameId, StateChange(command.questionId, command.state))
        }
    }

    private fun joinGame(gameId: GameId, playerId: PlayerId, name: String) {
        val game = get(gameId)
        val newPlayer = Player(playerId, name, gameId, 0, null, null)
        game.playerIds += playerId
        highScore += newPlayer.score to playerId
        playersStorage[playerId] = newPlayer
        playerGateway.send(playerId, getMessage(game, playerId))
        if (game.hostId != null) {
            playerGateway.send(game.hostId, PlayerJoinedMessage(newPlayer.id, newPlayer.name, game.playerIds.size))
        }
//        TODO: handle errors
    }

    private fun pickAnswer(playerId: PlayerId, questionId: QuestionId, answerId: AnswerId) {
        val player = playersStorage[playerId] ?: return
        player.lastQuestionId = questionId
        player.lastAnswerId = answerId
        val game = get(player.gameId)
        val currentQuestion = game.getCurrentQuestion()
        if (game.state == GameState.QUESTION && questionId == currentQuestion.id) {
            game.answersGiven.merge(answerId, 1) { old, new -> old + new }
            if (game.hostId != null) {
                playerGateway.send(game.hostId, AnswerGivenMessage(currentQuestion.id, game.answersGiven.values.sum()))
            }
            if (currentQuestion.answers.any { it.id == answerId && it.isRight }) {
                highScore -=  player.score to playerId
                player.score++
                highScore += player.score to playerId
            }
        }
    }

    private fun leaveGame(playerId: PlayerId) {
        val player = playersStorage[playerId]
        if (player != null) {
            gameStorage[player.gameId]?.playerIds?.remove(playerId)
        }
    }
}

fun Game.getCurrentQuestion(): Question = quizz.questions.find { it.id == currentQuestionId }!!

fun nextId(): String = toHexString(nextInt()).toString()

data class Player(
    val id: PlayerId,
    val name: String,
    val gameId: GameId,
    var score: Int,
    var lastQuestionId: QuestionId?,
    var lastAnswerId: AnswerId?
)

