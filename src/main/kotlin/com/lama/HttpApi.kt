package com.lama

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router

class HttpApi(
    val vertx: Vertx,
    val quizzService: QuizzService,
    val mapper: ObjectMapper
) {
    fun createApi(): Router {
        val router = Router.router(vertx)
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
                ctx.response().setStatusCode(404)
            }
        }
        return router
    }

    private fun HttpServerResponse.endWithJson(body: Any) {
        putHeader("Content-Type", "application/json").end(mapper.writeValueAsString(body))
    }
}

