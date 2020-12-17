package com.lama.domain

import com.lama.AnswerId
import com.lama.GameId
import com.lama.GameState
import com.lama.PlayerId
import com.lama.Question
import com.lama.QuestionId
import com.lama.QuizzId

sealed class ServerMessage(val type: String)

data class LoginMessage(val id: PlayerId) : ServerMessage("login")

data class GameStateMessage(
    val gameId: GameId,
    val state: GameState,
    val title: String,
    val question: Question?,
    val rightAnswerIds: List<AnswerId>?,
    val selectedAnswerId: AnswerId?
) : ServerMessage("game_state")

data class PlayerJoinedMessage(
    val id: PlayerId,
    val name: String,
    val totalPlayers: Int
) : ServerMessage("player_joined")

sealed class ClientCommand

data class CreateGameCommand(val quizzId: QuizzId) : ClientCommand()

data class ChangeGameStateCommand(
    val gameId: GameId, //TODO: remove and rely on host's game id
    val questionId: QuestionId,
    val state: GameState
) : ClientCommand()

data class JoinGameCommand(val name: String, val gameId: GameId) : ClientCommand()

data class PickAnswerCommand(val questionId: QuestionId, val answerId: AnswerId) : ClientCommand()

object LeaveGameCommand : ClientCommand()

