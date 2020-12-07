import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await

val picUrl = "https://static.wixstatic.com/media/a612c9_44860425bc124b9390735854ed91f32e~mv2.gif"

suspend fun main() {
    val vertx = Vertx.vertx()
    vertx.createHttpServer().requestHandler { req ->
        val body = """<html><body><img src="$picUrl" style="height: 100%"/></body></html>"""
        req.response().putHeader("content-type", "text/html").end(body)
    }.listen(8080).await()
    println("Server started at http://localhost:8080")
}
