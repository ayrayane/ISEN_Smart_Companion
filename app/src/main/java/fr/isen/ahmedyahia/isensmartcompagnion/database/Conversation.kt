package fr.isen.ahmedyahia.isensmartcompagnion.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité représentant une conversation entre l'utilisateur et l'IA.
 * Stocke la question posée, la réponse générée et la date de la conversation.
 */
@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)
