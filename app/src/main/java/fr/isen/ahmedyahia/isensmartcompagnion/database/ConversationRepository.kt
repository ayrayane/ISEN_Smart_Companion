package fr.isen.ahmedyahia.isensmartcompagnion.database

import kotlinx.coroutines.flow.Flow

/**
 * Repository pour gu00e9rer les opu00e9rations sur les conversations dans la base de donnu00e9es.
 */
class ConversationRepository(private val conversationDao: ConversationDao) {
    
    /**
     * Ru00e9cupu00e8re toutes les conversations triu00e9es par date (plus ru00e9centes d'abord).
     */
    val allConversations: Flow<List<Conversation>> = conversationDao.getAllConversations()
    
    /**
     * Insu00e8re une nouvelle conversation dans la base de donnu00e9es.
     * @param question La question posu00e9e par l'utilisateur.
     * @param answer La ru00e9ponse gu00e9nu00e9ru00e9e par l'IA.
     * @return L'ID de la conversation insu00e9ru00e9e.
     */
    suspend fun insertConversation(question: String, answer: String): Long {
        val conversation = Conversation(question = question, answer = answer)
        return conversationDao.insertConversation(conversation)
    }
    
    /**
     * Ru00e9cupu00e8re une conversation par son ID.
     * @param id L'ID de la conversation u00e0 ru00e9cupu00e9rer.
     * @return La conversation correspondant u00e0 l'ID, ou null si non trouvu00e9e.
     */
    suspend fun getConversationById(id: Long): Conversation? {
        return conversationDao.getConversationById(id)
    }
    
    /**
     * Supprime une conversation de la base de donnu00e9es.
     * @param conversation La conversation u00e0 supprimer.
     */
    suspend fun deleteConversation(conversation: Conversation) {
        conversationDao.deleteConversation(conversation)
    }
    
    /**
     * Supprime toutes les conversations de la base de donnu00e9es.
     */
    suspend fun deleteAllConversations() {
        conversationDao.deleteAllConversations()
    }
    
    /**
     * Recherche des conversations contenant un texte spu00e9cifique.
     * @param searchQuery Le texte u00e0 rechercher.
     * @return Un Flow contenant la liste des conversations correspondant u00e0 la recherche.
     */
    fun searchConversations(searchQuery: String): Flow<List<Conversation>> {
        return conversationDao.searchConversations(searchQuery)
    }
}
