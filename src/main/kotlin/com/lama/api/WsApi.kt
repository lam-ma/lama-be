package com.lama.api

import com.lama.nextId
import io.vertx.core.http.ServerWebSocket
import mu.KLogging

class WsApi {
    val clients = mutableMapOf<String, ServerWebSocket>()

    fun handle(ws: ServerWebSocket) {
        ws.accept()
        val id = nextId()
        clients[id] = ws
        println("Joined $id on ${ws.path()}")
        ws.textMessageHandler { msg ->
            logger.info("Got $msg from $id")
            ws.writeTextMessage("server replied: ${msg.reversed()}")
        }
        ws.closeHandler {
            logger.info("Disconnected $id")
        }
        ws.writeTextMessage("Hello, you are $id")
    }
    companion object : KLogging()
}
