package fr.isen.ahmedyahia.isensmartcompagnion.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de donnu00e9es principale de l'application.
 * Contient la table des conversations.
 */
@Database(entities = [Conversation::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Retourne le DAO pour accu00e9der aux conversations.
     */
    abstract fun conversationDao(): ConversationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Retourne l'instance unique de la base de donnu00e9es.
         * Si elle n'existe pas encore, elle est cru00e9u00e9e.
         * 
         * @param context Le contexte de l'application.
         * @return L'instance de la base de donnu00e9es.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
