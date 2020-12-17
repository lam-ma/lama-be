package com.lama

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jayway.jsonpath.JsonPath
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import kotlinx.coroutines.delay
import org.assertj.core.api.IterableAssert
import org.assertj.core.api.ObjectAssert
import java.io.InputStream
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.milliseconds

val mapper = ObjectMapper()
    .registerModule(KotlinModule())
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerModule(JavaTimeModule())

fun randomId() = Integer.toHexString(Random.nextInt())

fun env(name: String, defaultValue: String) = System.getenv(name) ?: defaultValue

fun gobbleStream(input: InputStream) {
    thread(isDaemon = true) {
        val reader = input.bufferedReader()
        generateSequence { reader.readLine() }.forEach { println(it) }
    }
}

suspend fun repeatUntilSucceed(timeout: Duration = 200.milliseconds, maxTries: Int = 15, f: suspend () -> Unit) {
    repeat(maxTries - 1) {
        runCatching {
            f()
            return
        }
        delay(timeout)
    }
    f()
}

val vertx = Vertx.vertx()

val httpClient = WebClient.create(vertx)

fun <T> JsonNode.extract(path: String): T = JsonPath.compile(path).read<T>(toString())

fun ObjectAssert<JsonNode?>.hasProperty(path: String, value: Any?): ObjectAssert<JsonNode?> {
    extracting { JsonPath.compile(path).read<Any>(it.toString()) }.isEqualTo(value)
    return this
}

fun assertThatJson(json: JsonNode?): ObjectAssert<JsonNode?> = ObjectAssert(json)

