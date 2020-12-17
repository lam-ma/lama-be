package com.lama.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.lama.GameService
import com.lama.PlayerId
import com.lama.domain.ChangeGameStateCommand
import com.lama.domain.ClientCommand
import com.lama.domain.CreateGameCommand
import com.lama.domain.JoinGameCommand
import com.lama.domain.LeaveGameCommand
import com.lama.domain.LoginMessage
import com.lama.domain.PickAnswerCommand
import com.lama.domain.ServerMessage
import com.lama.nextId
import com.lama.service.PlayerGateway
import io.vertx.core.http.ServerWebSocket
import mu.KLogging

class WsApi(
    private val mapper: ObjectMapper,
) : PlayerGateway {
    lateinit var gameService: GameService
    private val clients = mutableMapOf<PlayerId, ConnectedClient>()

    fun handle(ws: ServerWebSocket) {
        ws.accept()
        val playerId = PlayerId(nextId())
        val client = ConnectedClient(playerId, ws)
        clients[playerId] = client
        logger.info("Joined $playerId on ${ws.path()}")
        ws.textMessageHandler { msg ->
            handleClientCommand(msg, playerId)
        }
        ws.closeHandler {
            logger.info("Disconnected $playerId")
            gameService.handle(playerId, LeaveGameCommand)
            clients.remove(playerId)
        }
        send(playerId, LoginMessage(playerId))
    }

    private fun handleClientCommand(msg: String, playerId: PlayerId) {
        logger.info("Got $msg from $playerId")
        val command = parseClientCommand(msg)
        if (command == null) {
            logger.warn("Unknown command: $msg")
            return
        }
        gameService.handle(playerId, command)
    }

    private fun parseClientCommand(msg: String): ClientCommand? {
        val json = mapper.readTree(msg)
        return when (json["type"].asText()) {
            "create_game" -> mapper.treeToValue<CreateGameCommand>(json)
            "change_game" -> mapper.treeToValue<ChangeGameStateCommand>(json)
            "join_game" -> mapper.treeToValue<JoinGameCommand>(json)
            "pick_answer" -> mapper.treeToValue<PickAnswerCommand>(json)
            else -> null
        }
    }

    override fun send(playerId: PlayerId, message: ServerMessage) {
        clients[playerId]?.ws?.writeTextMessage(mapper.writeValueAsString(message))
    }

    companion object : KLogging()
}

class ConnectedClient(
    val id: PlayerId,
    val ws: ServerWebSocket
)

