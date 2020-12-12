package com.lama.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lama.AnswerId
import com.lama.Game
import com.lama.GameId
import com.lama.GameService
import com.lama.GameState
import com.lama.Player
import com.lama.PlayerId
import com.lama.QuestionId
import com.lama.domain.ClientCommand
import com.lama.domain.FinishMessage
import com.lama.domain.JoinGameCommand
import com.lama.domain.LoginMessage
import com.lama.domain.PickAnswerCommand
import com.lama.domain.RevealAnswerMessage
import com.lama.domain.ServerMessage
import com.lama.domain.ShowQuestionMessage
import com.lama.getCurrentQuestion
import com.lama.nextId
import com.lama.service.GameStateListener
import io.vertx.core.http.ServerWebSocket
import mu.KLogging

class WsApi(
    val mapper: ObjectMapper,
) : GameStateListener {
    lateinit var gameService: GameService
    val clients = mutableMapOf<PlayerId, ConnectedClient>()

    fun handle(ws: ServerWebSocket) {
        ws.accept()
        val playerId = PlayerId(nextId())
        val client = ConnectedClient(playerId, ws)
        clients[playerId] = client
        logger.info("Joined $playerId on ${ws.path()}")
        ws.textMessageHandler { msg -> handleClientCommand(msg, playerId) }
        ws.closeHandler {
            logger.info("Disconnected $playerId")
            gameService.leaveGame(playerId)
            clients.remove(playerId)
        }
        client.send(LoginMessage(playerId))
    }

    private fun handleClientCommand(msg: String, playerId: PlayerId) {
        logger.info("Got $msg from $playerId")
        val json = mapper.readTree(msg)
        val command = when (json["type"].asText()) {
            "join_game" -> JoinGameCommand(json["name"].asText(), GameId(json["game_id"].asText()))
            "pick_answer" -> PickAnswerCommand(QuestionId(json["question_id"].asText()), AnswerId(json["answer_id"].asText()))
            else -> null
        }
        when (command) {
            null -> logger.warn("Unknown command: $msg")
            is JoinGameCommand -> gameService.joinGame(command.gameId, playerId, command.name)
            is PickAnswerCommand -> gameService.pickAnswer(playerId, command.questionId, command.answerId)
        }
    }

    override fun stateChanged(game: Game, players: List<Player>) {
        players.forEach { player ->
            val client = clients[player.id]
            if (client != null) {
                val msg = getMessage(game, player)
                client.send(msg)
            }
        }
    }

    private fun getMessage(game: Game, player: Player): ServerMessage =
        when (game.state) {
            GameState.FINISH -> FinishMessage
            GameState.QUESTION -> ShowQuestionMessage(game.getCurrentQuestion()) //TODO: do not send right answer
            GameState.ANSWER -> {
                val question = game.getCurrentQuestion()
                val rightAnswerIds = question.answers.filter { it.isRight }.map { it.id }
                val lastAnswerId = player.lastAnswerId?.takeIf {  player.lastQuestionId == question.id }
                RevealAnswerMessage(question, rightAnswerIds, lastAnswerId)
            }
        }

    private fun ConnectedClient.send(cmd: ServerMessage) {
        ws.writeTextMessage(mapper.writeValueAsString(cmd))
    }

    companion object : KLogging()
}

class ConnectedClient(
    val id: PlayerId,
    val ws: ServerWebSocket
)

