package com.lama.domain

import com.lama.AnswerId
import com.lama.GameId
import com.lama.PlayerId
import com.lama.Question
import com.lama.QuestionId

sealed class ServerMessage(val type: String)

data class LoginMessage(val id: PlayerId) : ServerMessage("login")

data class ShowQuestionMessage(val question: Question) : ServerMessage("show_question")

data class RevealAnswerMessage(
    val question: Question,
    val rightAnswerIds: List<AnswerId>,
    val selectedAnswerId: AnswerId?
) : ServerMessage("reveal_right_answer")

object FinishMessage : ServerMessage("finish")


sealed class ClientCommand

data class JoinGameCommand(val name: String, val gameId: GameId) : ClientCommand()

data class PickAnswerCommand(val questionId: QuestionId, val answerId: AnswerId) : ClientCommand()

