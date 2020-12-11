package com.lama.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lama.GameNotFoundException
import com.lama.GameUpdateException
import com.lama.QuizzNotFoundException
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import mu.KLogging

abstract class HttpApiBase(
    val vertx: Vertx,
    val mapper: ObjectMapper,
) {

    abstract fun Router.api()

    fun createApi(): Router {
        val router = Router.router(vertx).installErrorHandler()
        router.route().handler(BodyHandler.create(false))
        router.route().handler {
            it.response().putHeader("Access-Control-Allow-Origin", "*")
            it.next()
        }
        router.api()
        return router
    }

    fun HttpServerResponse.endWithJson(body: Any) {
        putHeader("Content-Type", JSON_CONTENT_TYPE).end(mapper.writeValueAsString(body))
    }

    inline fun <reified T> RoutingContext.bodyAs(): T =
        runCatching {
            mapper.readValue<T>(bodyAsString)
        }.getOrElse {
            throw IllegalArgumentException("Can't parse request body: ${it.message}")
        }

    fun RoutingContext.sendError(status: HttpStatus, detail: String? = null) {
        val body = mapOf("status" to status.code, "title" to status.name, "detail" to detail)
        response().setStatusCode(status.code).endWithJson(body)
    }

    fun Router.installErrorHandler(): Router {
        errorHandler(HttpStatus.INTERNAL_SERVER_ERROR.code) { ctx ->
            val exception = ctx.failure()
            val (status, detail) = when (exception) {
                is QuizzNotFoundException -> HttpStatus.NOT_FOUND to exception.message
                is GameNotFoundException -> HttpStatus.NOT_FOUND to exception.message
                is GameUpdateException -> HttpStatus.UNPROCESSABLE_ENTITY to exception.message
                else -> HttpStatus.INTERNAL_SERVER_ERROR to null
            }
            if (status in CLIENT_ERROR_STATUSES) {
                logger.warn("Got exception:", exception)
            } else {
                logger.error("Got exception:", exception)
            }
            ctx.sendError(status, detail)
        }
        errorHandler(HttpStatus.NOT_FOUND.code) { ctx ->
            ctx.sendError(HttpStatus.NOT_FOUND, "Path ${ctx.request().path()} doesn't exist")
        }
        return this
    }

    companion object : KLogging() {
        const val JSON_CONTENT_TYPE = "application/json"
    }
}
