package fr.isen.ahmedyahia.isensmartcompagnion

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object GeminiAI {
    private val apiKey: String = BuildConfig.apiKey

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    suspend fun analyzeText(text: String): String {
        val input = " $text"
        return withContext(Dispatchers.IO) {
            try {

                val response = model.generateContent(content { text(input)})
                response.text ?: "Pas de réponse générée"
            } catch (e: Exception) {
                "Erreur: ${e.message}"
            }
        }
    }
}