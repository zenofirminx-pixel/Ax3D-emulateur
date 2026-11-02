package com.infinity.chat

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class InfinityApi {
    companion object {
        const val ENDPOINT = "https://aur-x-backend.vercel.app/api/chat"
    }

    @Throws(Exception::class)
    fun streamReply(
        prompt: String,
        history: List<ChatMessage>,
        onToken: (String) -> Unit
    ) {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 90000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "text/event-stream, application/json")
            setRequestProperty("Cache-Control", "no-cache")
        }

        try {
            val prior = JSONArray()
            history.filter { it.content.isNotBlank() }.forEach {
                prior.put(JSONObject().put("role", it.role).put("content", it.content))
            }
            val messages = JSONArray()
            history.filter { it.content.isNotBlank() }.forEach {
                messages.put(JSONObject().put("role", it.role).put("content", it.content))
            }
            messages.put(JSONObject().put("role", "user").put("content", prompt))

            val payload = JSONObject()
                .put("message", prompt)
                .put("history", prior)
                .put("messages", messages)
                .put("stream", true)

            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
            if (code !in 200..299) {
                val errorText = reader.readText()
                throw IllegalStateException("Serveur HTTP $code : ${errorText.take(500)}")
            }

            var data = StringBuilder()
            var receivedSse = false
            fun dispatch() {
                val raw = data.toString().trim()
                data = StringBuilder()
                if (raw.isEmpty() || raw == "[DONE]") return
                receivedSse = true
                val piece = extractText(raw)
                if (piece.isNotEmpty()) onToken(piece)
            }

            while (true) {
                val line = reader.readLine() ?: break
                when {
                    line.startsWith("data:") -> {
                        if (data.isNotEmpty()) data.append('\n')
                        data.append(line.removePrefix("data:").trimStart())
                    }
                    line.isBlank() -> dispatch()
                    line.startsWith("event:") || line.startsWith(":") || line.startsWith("id:") || line.startsWith("retry:") -> Unit
                    data.isEmpty() && !line.startsWith("data:") -> {
                        // Compatibilité avec une réponse JSON/plain text sans enveloppe SSE.
                        val piece = extractText(line)
                        if (piece.isNotEmpty()) onToken(piece)
                    }
                }
            }
            if (data.isNotEmpty()) dispatch()
            reader.close()
            if (!receivedSse && data.isEmpty()) Unit
        } finally {
            connection.disconnect()
        }
    }

    private fun extractText(raw: String): String {
        val value = raw.trim()
        if (value.isEmpty() || value == "[DONE]") return ""
        return try {
            val json = JSONObject(value)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val first = choices.optJSONObject(0) ?: return ""
                val delta = first.optJSONObject("delta")
                val deltaContent = delta?.opt("content")
                if (deltaContent is String) return deltaContent
                val message = first.optJSONObject("message")
                val messageContent = message?.opt("content")
                if (messageContent is String) return messageContent
                val text = first.optString("text", "")
                if (text.isNotEmpty()) return text
            }
            for (key in listOf("token", "content", "text", "response", "delta")) {
                val item = json.opt(key)
                if (item is String) return item
                if (item is JSONObject) {
                    val nested = item.optString("content", item.optString("text", ""))
                    if (nested.isNotEmpty()) return nested
                }
            }
            ""
        } catch (_: Exception) {
            // Les événements SSE peuvent aussi transporter directement du texte.
            value
        }
    }
}
