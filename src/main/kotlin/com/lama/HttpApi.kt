package com.lama

import com.fasterxml.jackson.databind.ObjectMapper
import com.lama.web.HttpApiBase
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

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
        get("/quizzes/:id").handler { ctx ->
            val quizzId = QuizzId(ctx.request().getParam("id"))
            val quizz = quizzService.get(quizzId)
            ctx.response().endWithJson(quizz)
        }
        post("/quizzes/:id/start").handler { ctx ->
            val quizzId = QuizzId(ctx.request().getParam("id"))
            val gameId = gameService.startGame(quizzId)
            ctx.response().endWithJson(GameIdResponse(gameId))
        }
    }
}

