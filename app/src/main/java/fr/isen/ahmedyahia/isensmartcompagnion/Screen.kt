@file:OptIn(ExperimentalMaterial3Api::class)

package fr.isen.ahmedyahia.isensmartcompagnion

import android.content.Context
import android.content.Intent
import fr.isen.ahmedyahia.isensmartcompagnion.database.Conversation
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import fr.isen.ahmedyahia.isensmartcompagnion.api.EventRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import java.util.Locale
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

// ------------------------------
// 0) Thème personnalisé avec toutes les couleurs en rouge et fond blanc
// ------------------------------
private val RedColorScheme = lightColorScheme(
    primary = Color.Red,
    onPrimary = Color.White,
    secondary = Color.Red,
    onSecondary = Color.White,
    background = Color.White,        // Fond en blanc
    onBackground = Color.Black,        // Texte par défaut sur fond blanc
    surface = Color.Red,
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.White
)

@Composable
fun RedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RedColorScheme,
        typography = typography,
        content = content
    )
}

// ------------------------------
// 1) Définition unique des routes pour la navigation
// ------------------------------
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Accueil")
    object Events : Screen("events", "Événements")
    object Agenda : Screen("agenda", "Agenda")
    object Historique : Screen("historique", "Historique")
}

// Classe pour les éléments de la barre de navigation
data class TabBarItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

// ------------------------------
// 2) Repositories pour stocker l'état global
// ------------------------------
object AgendaRepository {
    val agendaEvents = mutableStateListOf<Event>()
    private var context: Context? = null
    
    /**
     * Initialize the repository with a context for notifications
     */
    fun initialize(context: Context) {
        this.context = context
    }

    fun addEvent(event: Event) {
        if (!agendaEvents.contains(event)) {
            agendaEvents.add(event)
            
            // Send notification when event is added
            val ctx = context
            if (ctx is MainActivity) {
                ctx.sendEventNotification(event)
            }
        }
    }
}

// Nouveau repository pour le "chat"
object ChatRepository {
    // Classe pour représenter un message avec sa date
    data class ChatMessage(
        val id: String = UUID.randomUUID().toString(),
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    val messages = mutableStateListOf<ChatMessage>()
    
    fun addMessage(message: String) {
        messages.add(ChatMessage(content = message))
    }
    
    fun deleteMessage(messageId: String) {
        messages.removeIf { it.id == messageId }
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
}

// ------------------------------
// 3) Écran principal (MainScreen) avec la NavBar
// ------------------------------
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Définition des onglets
    val homeTab = TabBarItem(
        screen = Screen.Home,
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Default.Home
    )
    val eventsTab = TabBarItem(
        screen = Screen.Events,
        selectedIcon = Icons.Default.DateRange,
        unselectedIcon = Icons.Default.DateRange
    )
    val agendaTab = TabBarItem(
        screen = Screen.Agenda,
        selectedIcon = Icons.Default.CalendarToday,
        unselectedIcon = Icons.Default.CalendarToday,
        badgeAmount = AgendaRepository.agendaEvents.size
    )
    val infoTab = TabBarItem(
        screen = Screen.Historique,
        selectedIcon = Icons.Default.History,
        unselectedIcon = Icons.Default.History
    )

    val tabBarItems = listOf(homeTab, eventsTab, agendaTab, infoTab)

    // Gestion de la navigation courante
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    // 4) Nouvel écran d'accueil = assistant (avec GeminiAI)
                    HomeScreen()
                }
                composable(Screen.Events.route) {
                    EventListScreen()
                }
                composable(Screen.Agenda.route) {
                    AgendaScreen()
                }
                composable(Screen.Historique.route) {
                    // 5) On affiche l'historique des requêtes
                    HistoryScreen()
                }
            }
        }

        // Barre de navigation
        NavigationBar {
            tabBarItems.forEach { item ->
                val selected = currentRoute == item.screen.route

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.screen.route) {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        TabBarIconView(
                            isSelected = selected,
                            selectedIcon = item.selectedIcon,
                            unselectedIcon = item.unselectedIcon,
                            title = item.screen.title,
                            badgeAmount = item.badgeAmount
                        )
                    },
                    label = { Text(item.screen.title) }
                )
            }
        }
    }
}

// Composant pour afficher l'icône avec badge dans la barre de navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = {
        if (badgeAmount != null && badgeAmount > 0) {
            Badge {
                Text(badgeAmount.toString())
            }
        }
    }) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = title
        )
    }
}

// ------------------------------
// 4) HomeScreen = assistant (chat minimaliste avec GeminiAI et logo ISEN)
// ------------------------------
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val activity = context as? MainActivity
    var userInput by remember { mutableStateOf("") }
    
    // Utiliser la base de données Room pour récupérer les conversations
    val historyManager = activity?.historyManager
    var conversations by remember { mutableStateOf(emptyList<Conversation>()) }
    
    // Collecter les conversations depuis le Flow
    LaunchedEffect(historyManager) {
        historyManager?.allConversations?.collect { convList ->
            conversations = convList
        }
    }
    
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background) // Fond blanc
            .padding(16.dp)
    ) {
        // Affichage du logo de l'ISEN
        Image(
            painter = painterResource(id = R.drawable.isen), // Vérifiez que l'image "isen.png" est présente dans drawable
            contentDescription = "Logo de l'ISEN",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Assistant virtuel",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary // Texte en rouge
        )

        // Affichage des conversations depuis la base de données Room
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(conversations) { conversation ->
                // Message de l'utilisateur (question)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(conversation.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Vous: ${conversation.question}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                
                // Réponse de l'IA
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(conversation.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Assistant: ${conversation.answer}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Indicateur de chargement
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Champ de texte et bouton pour envoyer la requête
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userInput,
                onValueChange = { newValue -> userInput = newValue },
                modifier = Modifier.weight(1f),
                label = { Text("Posez votre question...", color = MaterialTheme.colorScheme.primary) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        val currentInput = userInput  // stocke la valeur actuelle
                        userInput = ""               // vide ensuite le champ
                        isLoading = true              // affiche l'indicateur de chargement
                        
                        coroutineScope.launch {
                            try {
                                val answer = GeminiAI.analyzeText(currentInput)
                                if (answer.contains("Une erreur s'est produite")) {
                                    // Afficher un message d'erreur plus convivial
                                    Toast.makeText(context, "Un problème est survenu avec l'IA. Veuillez réessayer.", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                // Gestion des exceptions imprévues
                                Toast.makeText(context, "Erreur inattendue: ${e.message}", Toast.LENGTH_LONG).show()
                                e.printStackTrace()
                            } finally {
                                isLoading = false          // cache l'indicateur de chargement dans tous les cas
                            }
                            // La sauvegarde dans la base de données est déjà faite dans GeminiAI.analyzeText
                        }
                    } else {
                        Toast.makeText(context, "Veuillez saisir une question", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Envoyer", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}


// ------------------------------
// 5) Écran Historique = affiche l'historique des conversations depuis la base de données Room
// ------------------------------
@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val activity = context as? MainActivity
    
    if (activity != null) {
        // Utiliser le gestionnaire d'historique de l'activité principale
        fr.isen.ahmedyahia.isensmartcompagnion.database.ConversationHistoryScreen(
            historyManager = activity.historyManager,
            onBackClick = { /* Ne rien faire car c'est un onglet de navigation */ }
        )
    } else {
        // Fallback si l'activité n'est pas disponible
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Impossible de charger l'historique",
                style = typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ------------------------------
// 6) Écran EventListScreen et EventCard (autres écrans existants)
// ------------------------------
@Composable
fun EventListScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { EventRepository() }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    fun loadEvents() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            repository.getEvents()
                .onSuccess { eventList ->
                    events = eventList
                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "Une erreur s'est produite"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        loadEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background) // Fond blanc
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Événements à venir",
                style = typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = { loadEvents() },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chargement des événements...", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Erreur: $errorMessage",
                            style = typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadEvents() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Réessayer", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
            events.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun événement disponible",
                        style = typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(events) { event ->
                        EventCard(event)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Ici, toujours rouge (selon le thème)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    text = event.title,
                    style = typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.category,
                        style = typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.date,
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Lieu",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.location,
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                style = typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Détail", color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Voir détails",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            val visibilityState = remember { MutableTransitionState(false) }
            visibilityState.targetState = expanded

            AnimatedVisibility(
                visibleState = visibilityState,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.onBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Description complète",
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(context, EventDetailActivity::class.java)
                            intent.putExtra("event", event)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Voir plus", color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Voir plus de détails",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            AgendaRepository.addEvent(event)
                            Toast.makeText(context, "Événement ajouté à l'agenda", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Ajouter à l'agenda", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaScreen() {
    val agendaEvents = AgendaRepository.agendaEvents
    val currentMonth = remember { mutableStateOf(YearMonth.now()) }
    val selectedDate = remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    
    // Convertir les dates des événements en LocalDate
    // La clé "agendaEvents" assure que cette valeur est recalculée chaque fois que agendaEvents change
    val eventDates = remember(agendaEvents) {
        agendaEvents.mapNotNull { it.getLocalDate() }.toSet()
    }
    
    // Filtrer les événements pour la date sélectionnée
    val eventsForSelectedDate = remember(selectedDate.value, agendaEvents) {
        if (selectedDate.value != null) {
            agendaEvents.filter { event ->
                event.getLocalDate() == selectedDate.value
            }
        } else {
            emptyList()
        }
    }

    // Effet pour mettre à jour la date sélectionnée si un nouvel événement est ajouté pour aujourd'hui
    LaunchedEffect(agendaEvents.size) {
        val today = LocalDate.now()
        val hasEventToday = agendaEvents.any { it.getLocalDate() == today }
        
        // Si un événement a été ajouté pour aujourd'hui et aucune date n'est sélectionnée, sélectionner aujourd'hui
        if (hasEventToday && selectedDate.value == null) {
            selectedDate.value = today
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background) // Fond blanc
            .padding(16.dp)
    ) {
        Text(
            text = "Agenda",
            style = typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Affichage du mois et des boutons de navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth.value = currentMonth.value.minusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Mois précédent"
                )
            }
            
            Text(
                text = currentMonth.value.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = typography.titleMedium
            )
            
            IconButton(onClick = {
                currentMonth.value = currentMonth.value.plusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Mois suivant"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Affichage des jours de la semaine
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Affichage du calendrier
        val firstDayOfMonth = currentMonth.value.atDay(1)
        val lastDayOfMonth = currentMonth.value.atEndOfMonth()
        
        // Déterminer le premier jour à afficher (lundi de la semaine contenant le 1er du mois)
        val firstDayToShow = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        
        // Déterminer le dernier jour à afficher (dimanche de la semaine contenant le dernier jour du mois)
        val lastDayToShow = lastDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        
        // Calculer le nombre de semaines à afficher
        val numWeeks = ChronoUnit.WEEKS.between(firstDayToShow, lastDayToShow) + 1
        
        // Afficher le calendrier
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            for (weekIndex in 0 until numWeeks.toInt()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dayIndex in 0..6) {
                        val day = firstDayToShow.plusDays((weekIndex * 7 + dayIndex).toLong())
                        val isCurrentMonth = day.month == currentMonth.value.month
                        val isSelected = day == selectedDate.value
                        val hasEvents = eventDates.contains(day)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        hasEvents -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = if (isSelected) Color.Transparent else 
                                           if (hasEvents) MaterialTheme.colorScheme.primary 
                                           else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedDate.value = day
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                style = typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    !isCurrentMonth -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    hasEvents -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Affichage des événements pour la date sélectionnée
        if (selectedDate.value != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Événements du ${selectedDate.value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        style = typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (eventsForSelectedDate.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucun événement pour cette date",
                                style = typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(eventsForSelectedDate) { event ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = event.title,
                                            style = typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Lieu",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = event.location,
                                                style = typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Text(
                                            text = event.description,
                                            style = typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(MaterialTheme.colorScheme.primary)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .align(Alignment.End)
                                        ) {
                                            Text(
                                                text = event.category,
                                                style = typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        } else {
            if (agendaEvents.isEmpty()) {
                Text(
                    text = "Aucun événement ajouté à l'agenda",
                    style = typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Text(
                    text = "Sélectionnez une date pour voir les événements",
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}
