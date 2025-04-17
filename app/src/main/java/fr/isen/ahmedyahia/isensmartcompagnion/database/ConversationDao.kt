package fr.isen.ahmedyahia.isensmartcompagnion.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour accu00e9der aux conversations stocku00e9es dans la base de donnu00e9es.
 */
@Dao
interface ConversationDao {
    
    /**
     * Insu00e8re une nouvelle conversation dans la base de donnu00e9es.
     * @param conversation La conversation u00e0 insu00e9rer.
     * @return L'ID de la conversation insu00e9ru00e9e.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation): Long
    
    /**
     * Ru00e9cupu00e8re toutes les conversations triu00e9es par date (plus ru00e9centes d'abord).
     * @return Un Flow contenant la liste des conversations.
     */
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<Conversation>>
    
    /**
     * Ru00e9cupu00e8re une conversation par son ID.
     * @param id L'ID de la conversation u00e0 ru00e9cupu00e9rer.
     * @return La conversation correspondant u00e0 l'ID.
     */
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): Conversation?
    
    /**
     * Supprime une conversation de la base de donnu00e9es.
     * @param conversation La conversation u00e0 supprimer.
     */
    @Delete
    suspend fun deleteConversation(conversation: Conversation)
    
    /**
     * Supprime toutes les conversations de la base de donnu00e9es.
     */
    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
    
    /**
     * Recherche des conversations contenant un texte spu00e9cifique dans la question ou la ru00e9ponse.
     * @param searchQuery Le texte u00e0 rechercher.
     * @return Une liste de conversations correspondant u00e0 la recherche.
     */
    @Query("SELECT * FROM conversations WHERE question LIKE '%' || :searchQuery || '%' OR answer LIKE '%' || :searchQuery || '%' ORDER BY timestamp DESC")
    fun searchConversations(searchQuery: String): Flow<List<Conversation>>
}
