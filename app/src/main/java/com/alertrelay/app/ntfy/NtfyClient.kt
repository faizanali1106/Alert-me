package com.alertrelay.app.ntfy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object NtfyClient {
    private const val BASE_URL = "https://ntfy.sh"

    suspend fun send(topic: String, title: String, message: String, priority: Int = 4): Boolean {
        if (topic.isBlank()) return false
        val encodedTopic = URLEncoder.encode(topic.trim(), StandardCharsets.UTF_8.toString())
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$BASE_URL/$encodedTopic")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("Title", title)
                    setRequestProperty("Priority", priority.toString())
                    setRequestProperty("Tags", "bell")
                    doOutput = true
                }
                connection.outputStream.use { stream ->
                    stream.write(message.toByteArray(StandardCharsets.UTF_8))
                }
                val code = connection.responseCode
                code in 200..299
            } catch (_: Exception) {
                false
            } finally {
                connection?.disconnect()
            }
        }
    }
}
