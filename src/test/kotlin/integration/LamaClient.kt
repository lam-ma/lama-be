package integration

import com.fasterxml.jackson.databind.JsonNode
import com.lama.mapper
import com.lama.vertx
import io.vertx.core.http.WebSocket
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.seconds

class LamaClient(val port: Int) {
    private lateinit var ws: WebSocket
    private val incoming = Channel<String>()

    suspend fun connect() {
        ws = awaitResult {
            vertx.createHttpClient().webSocket(port, "localhost", "/") { res ->
                res.result()?.textMessageHandler { msg ->
                    CoroutineScope(vertx.dispatcher()).launch {
                        incoming.send(msg)
                    }
                }
                res.result()?.closeHandler {
                    println("websocket closed")
                }
                it.handle(res)
            }
        }
    }

    fun send(msg: Any) = ws.writeTextMessage(mapper.writeValueAsString(msg))

    suspend fun getMessage(): JsonNode = withTimeout(5.seconds) { mapper.readTree(incoming.receive()) }
}
