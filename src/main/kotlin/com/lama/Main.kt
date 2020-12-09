package com.lama

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await

val picUrl = "https://static.wixstatic.com/media/a612c9_44860425bc124b9390735854ed91f32e~mv2.gif"

suspend fun main() {
    val port = (System.getenv("LAMA_BE_PORT") ?: "8080").toInt()
    val vertx = Vertx.vertx()
    vertx.createHttpServer().requestHandler { req ->
        val body = """<html><body><img src="$picUrl" style="height: 100%"/></body></html>"""
        req.response().putHeader("content-type", "text/html").end(body)
    }.listen(port).await()
    println("Server started at http://0.0.0.0:$port")
}
