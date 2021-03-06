package integration

import com.lama.gobbleStream
import com.lama.httpClient
import com.lama.repeatUntilSucceed
import io.vertx.kotlin.coroutines.await
import org.assertj.core.api.Assertions.assertThat
import java.lang.RuntimeException
import java.net.URL
import kotlin.time.seconds

class LamaServer(val port: Int) {
    suspend fun start() {
        val buildResult = Runtime.getRuntime().exec("./gradlew shadowJar -x test").waitFor()
        if (buildResult != 0) {
            throw RuntimeException("Gradle build returned code $buildResult")
        }
        val cmd = listOf("java", "-jar", "build/libs/lama.jar")
        val process = ProcessBuilder(*cmd.toTypedArray()).apply { environment() += createEnv() }.start()
        Runtime.getRuntime().addShutdownHook(Thread { process.destroy() })
        gobbleStream(process.inputStream)
        gobbleStream(process.errorStream)

        waitForServerToStart()
        println("Server started")
    }

    private fun createEnv() = mapOf("LAMA_BE_PORT" to "$port")

    private suspend fun waitForServerToStart() {
        repeatUntilSucceed(1.seconds) {
            println("Calling GET /health")
            val responseCode = httpClient.get(port, "localhost", "/health").send().await().statusCode()
            assertThat(responseCode).isEqualTo(200)
        }
    }
}
