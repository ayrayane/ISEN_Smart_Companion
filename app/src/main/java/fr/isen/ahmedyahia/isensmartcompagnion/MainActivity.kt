package fr.isen.ahmedyahia.isensmartcompagnion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import fr.isen.ahmedyahia.isensmartcompagnion.database.ConversationHistoryManager
import fr.isen.ahmedyahia.isensmartcompagnion.notification.NotificationHelper
import fr.isen.ahmedyahia.isensmartcompagnion.ui.theme.ISENSmartCompagnionTheme

class MainActivity : ComponentActivity() {
    
    // Gestionnaire de l'historique des conversations
    lateinit var historyManager: ConversationHistoryManager
    
    // Gestionnaire de notifications
    lateinit var notificationHelper: NotificationHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialisation de la base de données et du gestionnaire d'historique
        historyManager = ConversationHistoryManager(this)
        
        // Initialisation de GeminiAI avec le contexte pour accéder à la base de données
        GeminiAI.initialize(this)
        
        // Initialisation du gestionnaire de notifications
        notificationHelper = NotificationHelper(this)
        
        // Demander la permission pour les notifications sur Android 13+
        requestNotificationPermission()
        
        // Initialiser AgendaRepository avec le contexte pour les notifications
        AgendaRepository.initialize(this)
        
        setContent {
            ISENSmartCompagnionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    MainScreen(navController)
                }
            }
        }
    }
    
    /**
     * Méthode pour envoyer une notification pour un événement
     */
    fun sendEventNotification(event: Event) {
        notificationHelper.showEventNotification(event)
    }
    
    /**
     * Demande la permission pour les notifications sur Android 13+
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Enregistrer le launcher pour demander la permission
                val requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    // Permission accordée ou refusée
                }
                
                // Demander la permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
