package com.lama

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
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

data class PlayerId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val value: String) {
    override fun toString() = value
}

data class CreateQuizzDto(
    val title: String,
    val questions: List<Question>
)

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
    val quizz: Quizz,
    var currentQuestionId: QuestionId,
    var state: GameState,
    @JsonIgnore //TODO: make GameDto
    val playerIds: MutableSet<PlayerId> = LinkedHashSet()
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

