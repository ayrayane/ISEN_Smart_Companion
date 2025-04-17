package fr.isen.ahmedyahia.isensmartcompagnion

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import fr.isen.ahmedyahia.isensmartcompagnion.database.ConversationHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object GeminiAI {
    private val apiKey: String = BuildConfig.apiKey

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )
    
    private var historyManager: ConversationHistoryManager? = null
    
    /**
     * Initialise le gestionnaire d'historique pour la sauvegarde des conversations.
     * Cette méthode doit être appelée au démarrage de l'application.
     */
    fun initialize(context: Context) {
        historyManager = ConversationHistoryManager(context)
    }

    /**
     * Analyse le texte fourni et génère une réponse via l'API Gemini.
     * La question et la réponse sont sauvegardées dans la base de données locale.
     * 
     * @param text La question posée par l'utilisateur
     * @return La réponse générée par l'IA
     */
    suspend fun analyzeText(text: String): String {
        val input = text.trim()
        return withContext(Dispatchers.IO) {
            try {
                val response = model.generateContent(content { text(input) })
                
                if (response.text != null) {
                    val answer = response.text ?: "Pas de réponse générée"
                    
                    // Sauvegarde de la conversation dans la base de données
                    historyManager?.addConversation(text, answer)
                    
                    return@withContext answer
                } else {
                    val errorMsg = "Pas de réponse générée par l'IA"
                    historyManager?.addConversation(text, errorMsg)
                    return@withContext errorMsg
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMessage = "Une erreur s'est produite: ${e.message ?: "something unexpected happened"}"
                
                // Sauvegarde également les erreurs dans l'historique
                historyManager?.addConversation(text, errorMessage)
                
                return@withContext errorMessage
            }
        }
    }
    
    /**
     * Récupère le gestionnaire d'historique des conversations.
     * Utile pour accéder à l'historique des conversations depuis d'autres composants.
     */
    fun getHistoryManager(): ConversationHistoryManager? {
        return historyManager
    }
}