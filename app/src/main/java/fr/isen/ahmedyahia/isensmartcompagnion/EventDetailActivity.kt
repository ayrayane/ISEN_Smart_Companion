package fr.isen.ahmedyahia.isensmartcompagnion

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import fr.isen.ahmedyahia.isensmartcompagnion.notification.NotificationHelper
import fr.isen.ahmedyahia.isensmartcompagnion.notification.PermissionHandler
import fr.isen.ahmedyahia.isensmartcompagnion.notification.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.isen.ahmedyahia.isensmartcompagnion.ui.theme.ISENSmartCompagnionTheme

@OptIn(ExperimentalMaterial3Api::class)
class EventDetailActivity : ComponentActivity() {
    
    // Making these public so they can be accessed from composables
    lateinit var preferencesManager: PreferencesManager
    lateinit var notificationHelper: NotificationHelper
    lateinit var permissionHandler: PermissionHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialisation des gestionnaires
        preferencesManager = PreferencesManager(this)
        notificationHelper = NotificationHelper(this)
        permissionHandler = PermissionHandler(this)
        permissionHandler.initialize()
        
        // Récupération de l'objet Event transmis via Serializable
        val event = intent.getSerializableExtra("event") as? Event

        setContent {
            ISENSmartCompagnionTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Détails de l'événement") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Retour"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                ) { paddingValues ->
                    EventDetailScreen(event, Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(event: Event?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as EventDetailActivity
    var showContent by remember { mutableStateOf(false) }
    
    // État pour le toggle de notification
    var isNotificationEnabled by remember { 
        mutableStateOf(event?.id?.let { activity.preferencesManager.isEventNotificationEnabled(it) } ?: false)
    }

    LaunchedEffect(Unit) {
        // Déclencher l'animation après un court délai
        showContent = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (event != null) {
            Text(text = event.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Date : ${event.date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Lieu : ${event.location}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Catégorie : ${event.category}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Switch pour activer/désactiver les notifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isNotificationEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                    contentDescription = "Notification",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "  Recevoir une notification pour cet événement",
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                Switch(
                    checked = isNotificationEnabled,
                    onCheckedChange = { checked ->
                        isNotificationEnabled = checked
                        event?.id?.let { eventId ->
                            // Sauvegarder la préférence
                            activity.preferencesManager.saveEventNotificationPreference(eventId, checked)
                            
                            // Si activé, envoyer une notification après 10 secondes
                            if (checked) {
                                Toast.makeText(context, "Vous recevrez une notification dans 10 secondes", Toast.LENGTH_SHORT).show()
                                CoroutineScope(Dispatchers.IO).launch {
                                    delay(10000) // 10 secondes
                                    activity.notificationHelper.showEventNotification(event)
                                }
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            // Bouton pour ajouter l'événement à l'agenda
            Button(
                onClick = {
                    AgendaRepository.addEvent(event)
                    Toast.makeText(context, "Événement ajouté à l'agenda", Toast.LENGTH_SHORT).show()
                    
                    // Envoyer une notification immédiatement
                    try {
                        // Utiliser directement le MainActivity pour accéder au NotificationHelper
                        val mainActivity = context.applicationContext as? MainActivity
                        mainActivity?.notificationHelper?.showEventNotification(event)
                            ?: Toast.makeText(context, "Impossible d'envoyer la notification", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter à l'agenda")
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visibleState = remember { MutableTransitionState(showContent) },
                enter = fadeIn() + expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut()
            ) {
                // Autres informations complémentaires
            }
        } else {
            Text(text = "Aucun détail disponible pour cet événement.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
