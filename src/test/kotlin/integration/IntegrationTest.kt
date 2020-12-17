package integration

import com.lama.assertThatJson
import com.lama.extract
import com.lama.hasProperty
import com.lama.httpClient
import com.lama.mapper
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class IntegrationTest {
    val port = 18080
    val server = LamaServer(port)
    val client = LamaClient(port)

    @BeforeAll
    fun setup() = runBlocking {
        server.start()
        client.connect()
    }

    @Test
    fun `quizz CRUD`() = runBlocking<Unit> {
        val response = httpClient.post(port, "localhost", "/quizzes").sendBuffer(Buffer.buffer(quizzJson)).await()
        assertThat(response.statusCode()).isEqualTo(201)
        val quizzBody = mapper.readTree(response.body().toString())
        assertThatJson(quizzBody).hasProperty("$.title", "New quizz")
        assertThatJson(quizzBody).hasProperty("$.questions[0].id", "q1")
        val quizzId = quizzBody.extract<String>("$.id")
        
        val response2 = httpClient.get(port, "localhost", "/quizzes/$quizzId").send().await()
        assertThat(response2.statusCode()).isEqualTo(200)
        assertThat(response2.body().toString()).isEqualTo(response.body().toString())

        val updatedQuizzJson = quizzJson.replace("\"q1\"", "\"q_new\"")
        val response3 = httpClient
            .put(port, "localhost", "/quizzes/$quizzId")
            .sendBuffer(Buffer.buffer(updatedQuizzJson))
            .await()
        assertThat(response3.statusCode()).isEqualTo(200)
        val updatedBody = mapper.readTree(response3.body().toString())
        assertThatJson(updatedBody).hasProperty("$.id", quizzId)
        assertThatJson(updatedBody).hasProperty("$.questions[0].id", "q_new")
    }

    @Test
    fun `create and update game via rest`() = runBlocking<Unit> {
        val response = httpClient.post(port, "localhost", "/quizzes").sendBuffer(Buffer.buffer(quizzJson)).await()
        val quizzId = mapper.readTree(response.body().toString()).extract<String>("$.id")

        val response2 = httpClient.post(port, "localhost", "/quizzes/$quizzId/start").send().await()
        assertThat(response2.statusCode()).isEqualTo(201)
        val gameBody = mapper.readTree(response2.body().toString())
        val gameId = gameBody.extract<String>("$.id")
        assertThatJson(gameBody).hasProperty("$.state", "QUESTION")
        assertThatJson(gameBody).hasProperty("$.quizz.id", quizzId)
        assertThatJson(gameBody).hasProperty("$.current_question_id", "q1")

        val response3 = httpClient.get(port, "localhost", "/games/$gameId").send().await()
        assertThat(response3.statusCode()).isEqualTo(200)
        assertThat(response3.body().toString()).isEqualTo(response2.body().toString())

        val response4 = httpClient
            .post(port, "localhost", "/games/$gameId")
            .sendBuffer(Buffer.buffer(mapper.writeValueAsString(mapOf("question_id" to "q2", "state" to "FINISH"))))
            .await()
        assertThat(response4.statusCode()).isEqualTo(200)
        val updatedGame = mapper.readTree(response4.body().toString())
        assertThatJson(updatedGame).hasProperty("$.id", gameId)
        assertThatJson(updatedGame).hasProperty("$.current_question_id", "q2")
        assertThatJson(updatedGame).hasProperty("$.state", "FINISH")
    }

    @Test
    fun `create and update game via ws`() = runBlocking<Unit> {
        val hostClient = client
        val playerClient = LamaClient(port).apply { connect() }

        val msg3 = playerClient.getMessage()
        assertThatJson(msg3).hasProperty("$.type", "login")
        val playerId = msg3.extract<String>("$.id")

        val msg = hostClient.getMessage()
        assertThatJson(msg).hasProperty("$.type", "login")
        val hostId = msg.extract<String>("$.id")
        assertThat(hostId).isNotNull

        hostClient.send(mapOf("type" to "create_game", "quizz_id" to "123"))
        val msg2 = hostClient.getMessage()
        assertThatJson(msg2).hasProperty("$.type", "game_state")
        assertThatJson(msg2).hasProperty("$.state", "QUESTION")
        val gameId = msg2.extract<String?>("$.game_id")

        playerClient.send(mapOf("type" to "join_game", "game_id" to gameId, "name" to "Aviv"))


        val msg4 = hostClient.getMessage()
        assertThatJson(msg4).hasProperty("$.type", "player_joined")
        assertThatJson(msg4).hasProperty("$.name", "Aviv")
        assertThatJson(msg4).hasProperty("$.id", playerId)

        hostClient.send(mapOf("type" to "change_game", "game_id" to gameId, "question_id" to "qid_2", "state" to "ANSWER"))
        val msg5 = playerClient.getMessage()
        assertThatJson(msg5).hasProperty("$.type", "game_state")
        assertThatJson(msg5).hasProperty("$.game_id", gameId)
        println(msg5)

    }
}
