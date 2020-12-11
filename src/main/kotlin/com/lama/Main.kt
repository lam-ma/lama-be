package com.lama

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lama.api.HttpApi
import com.lama.api.WsApi
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await

val picUrl = "https://pbs.twimg.com/profile_images/1311712724708188161/EfsqxEuP_400x400.jpg"

suspend fun main() {
    val port = (System.getenv("LAMA_BE_PORT") ?: "8080").toInt()
    val vertx = Vertx.vertx()
    val mapper = createObjectMapper()
    val quizzService = QuizzServiceImpl()
    val gameService = GameServiceImpl(quizzService)
    val httpApi = HttpApi(vertx, mapper, quizzService, gameService)
    val wsApi = WsApi()
    vertx.createHttpServer()
        .requestHandler(httpApi.createApi())
        .webSocketHandler(wsApi::handle)
        .exceptionHandler { it.printStackTrace() }
        .listen(port)
        .await()
    println("Server started at http://localhost:$port")
}

fun createObjectMapper(): ObjectMapper =
    ObjectMapper()
        .registerModule(KotlinModule())
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .registerModule(JavaTimeModule())
