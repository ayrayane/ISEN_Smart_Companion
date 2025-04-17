package fr.isen.ahmedyahia.isensmartcompagnion.database

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Gestionnaire pour l'historique des conversations.
 * Fournit des mu00e9thodes pratiques pour accu00e9der u00e0 l'historique des conversations.
 */
class ConversationHistoryManager(context: Context) {
    
    private val repository: ConversationRepository
    
    init {
        val database = AppDatabase.getDatabase(context)
        repository = ConversationRepository(database.conversationDao())
    }
    
    /**
     * Ru00e9cupu00e8re toutes les conversations triu00e9es par date (plus ru00e9centes d'abord).
     */
    val allConversations: Flow<List<Conversation>> = repository.allConversations
    
    /**
     * Ajoute une nouvelle conversation u00e0 l'historique.
     * @param question La question posu00e9e par l'utilisateur.
     * @param answer La ru00e9ponse gu00e9nu00e9ru00e9e par l'IA.
     */
    suspend fun addConversation(question: String, answer: String): Long {
        return repository.insertConversation(question, answer)
    }
    
    /**
     * Supprime une conversation de l'historique.
     * @param conversation La conversation u00e0 supprimer.
     */
    suspend fun deleteConversation(conversation: Conversation) {
        repository.deleteConversation(conversation)
    }
    
    /**
     * Efface tout l'historique des conversations.
     */
    suspend fun clearHistory() {
        repository.deleteAllConversations()
    }
    
    /**
     * Recherche des conversations dans l'historique.
     * @param query Le texte u00e0 rechercher.
     */
    fun searchConversations(query: String): Flow<List<Conversation>> {
        return repository.searchConversations(query)
    }
}
