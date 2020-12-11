package com.lama

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.net.URL
import com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING as embed

data class QuizzId @JsonCreator(mode = embed) constructor(@JsonValue val value: String) {
    override fun toString() = value
}

data class QuestionId @JsonCreator(mode = embed) constructor(@JsonValue val value: String) {
    override fun toString() = value
}

data class AnswerId @JsonCreator(mode = embed) constructor(@JsonValue val value: String) {
    override fun toString() = value
}

data class GameId @JsonCreator(mode = embed) constructor(@JsonValue val value: String) {
    override fun toString() = value
}

data class Quizz(
    val id: QuizzId,
    val title: String,
    val questions: List<Question>
)

data class Question(
    val id: QuestionId,
    val description: String,
    val imageUrl: URL?,
    val answers: List<Answer>
)

data class Answer(
    val id: AnswerId,
    val description: String,
    val isRight: Boolean
)

data class Game(
    val id: GameId,
    val currentQuestionId: QuestionId,
    val quizz: Quizz,
    val state: GameState
)

enum class GameState {
    QUESTION,
    ANSWER,
    FINISH
}

class QuizzNotFoundException(message: String) : RuntimeException(message)

class GameNotFoundException(message: String): RuntimeException(message)
