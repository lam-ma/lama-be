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

data class StateChange(
    val questionId: QuestionId,
    val state: GameState
)

data class HighScore(
    val scores: List<PlayerScore>
)

data class PlayerScore(
    val name: String,
    val score: Int
)

enum class GameState {
    QUESTION,
    ANSWER,
    FINISH
}

class QuizzNotFoundException(id: QuizzId) : RuntimeException("Quizz with $id not found")

class GameNotFoundException(id: GameId): RuntimeException("Game for gameId $id not found")

class GameUpdateException(message: String): RuntimeException(message)
