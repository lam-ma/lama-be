package com.lama

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class HttpApi(
    val vertx: Vertx,
    val quizzService: QuizzService,
    val gameService: GameService,
    val mapper: ObjectMapper
) {
    fun createApi(): Router {
        val router = Router.router(vertx)
        router.route().handler {
            it.response().putHeader("Access-Control-Allow-Origin", "*")
            it.next()
        }
        router.get("/").handler { ctx ->
            val body = """<html><body><img src="$picUrl" style="height: 100%"/></body></html>"""
            ctx.response().putHeader("content-type", "text/html").end(body)
        }
        router.get("/health").handler {
            it.response().end("Healthy")
        }
        router.get("/quizzes/:id").handler { ctx ->
            val quizzId = QuizzId(ctx.request().getParam("id"))
            val quizz = quizzService.get(quizzId)
            if (quizz != null) {
                ctx.response().endWithJson(quizz)
            } else {
                ctx.response().statusCode = 404
            }
        }
        router.post("/quizzes/:id/start").handler { ctx ->
            val quizzId = QuizzId(ctx.request().getParam("id"))
            val gameId = gameService.startGame(quizzId)
            ctx.response().endWithJson(GameIdResponse(gameId))
        }
        return router
    }

    private fun HttpServerResponse.endWithJson(body: Any) {
        putHeader("Content-Type", "application/json").end(mapper.writeValueAsString(body))
    }

    private inline fun <reified T> RoutingContext.bodyAs(): T =
        runCatching {
            mapper.readValue<T>(bodyAsString)
        }.getOrElse {
            throw IllegalArgumentException("Can't parse request body: ${it.message}")
        }
}

