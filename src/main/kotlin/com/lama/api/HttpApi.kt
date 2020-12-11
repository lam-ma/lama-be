package com.lama.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.lama.CreateQuizzDto
import com.lama.GameId
import com.lama.GameService
import com.lama.QuizzId
import com.lama.QuizzService
import com.lama.StateChange
import com.lama.http.HttpApiBase
import com.lama.http.HttpStatus
import com.lama.picUrl
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class HttpApi(
    vertx: Vertx,
    mapper: ObjectMapper,
    val quizzService: QuizzService,
    val gameService: GameService
) : HttpApiBase(vertx, mapper) {

    override fun Router.api() {
        get("/").handler { ctx ->
            val body = """<html><body><img src="$picUrl" style="height: 100%"/></body></html>"""
            ctx.response().putHeader("content-type", "text/html").end(body)
        }
        get("/health").handler {
            it.response().end("Healthy")
        }
        post("/quizzes").handler { ctx ->
            val quizzDto = ctx.bodyAs<CreateQuizzDto>()
            val quizz = quizzService.create(quizzDto)
            ctx.response().setStatusCode(HttpStatus.CREATED.code).endWithJson(quizz)
        }
        put("/quizzes/:id").handler { ctx ->
            val quizzId = ctx.getQuizzId()
            val quizzDto = ctx.bodyAs<CreateQuizzDto>()
            val quizz = quizzService.edit(quizzId, quizzDto)
            ctx.response().endWithJson(quizz)
        }
        get("/quizzes/:id").handler { ctx ->
            val quizzId = ctx.getQuizzId()
            val quizz = quizzService.get(quizzId)
            ctx.response().endWithJson(quizz)
        }
        post("/quizzes/:id/start").handler { ctx ->
            val quizzId = ctx.getQuizzId()
            val game = gameService.startGame(quizzId)
            ctx.response().setStatusCode(HttpStatus.CREATED.code).endWithJson(game)
        }
        get("/games/:id").handler { ctx ->
            val gameId = ctx.getGameId()
            val game = gameService.get(gameId)
            ctx.response().endWithJson(game)
        }
        post("/games/:id").handler { ctx ->
            val gameId = ctx.getGameId()
            val stateChange = ctx.bodyAs<StateChange>()
            val updatedGame = gameService.update(gameId, stateChange)
            ctx.response().endWithJson(updatedGame)
        }
        get("/games/:id/scores").handler { ctx ->
            val gameId = ctx.getGameId()
            val limit = ctx.request().getParam("limit")?.toInt() ?: 5
            val score = gameService.getHighScore(gameId, limit)
            ctx.response().endWithJson(score)
        }
        get("/ws").handler {
            val payload = """
                <script>
                    var socket = new WebSocket("ws://localhost:8080")
    
                    socket.onmessage = function(event) {
                        alert("Received data from websocket: " + event.data)
                    }
    
                    socket.onopen = function(event) {
                        alert("Web Socket opened")
                        socket.send("Blah")
                    }
    
                    socket.onclose = function(event) {
                        alert("Web Socket closed")
                    }
                </script>
            """
            it.response().putHeader("content-type", "text/html").end(payload)
        }
    }

    private fun RoutingContext.getGameId() = GameId(request().getParam("id"))

    private fun RoutingContext.getQuizzId() = QuizzId(request().getParam("id"))
}

