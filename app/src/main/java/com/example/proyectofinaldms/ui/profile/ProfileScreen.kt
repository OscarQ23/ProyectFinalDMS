package com.example.proyectofinaldms.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectofinaldms.ui.auth.AuthViewModel
import com.example.proyectofinaldms.ui.events.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser = authViewModel.currentUser
    val myEvents by eventViewModel.myEvents.collectAsState()
    val history by eventViewModel.userHistory.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            if (!currentUser?.photoUrl.toString().isNullOrEmpty()) {
                AsyncImage(
                    model = currentUser?.photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                currentUser?.displayName ?: "Usuario",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                currentUser?.email ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(24.dp))

            // Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("Mis eventos", myEvents.size.toString(), Icons.Default.Event)
                StatCard("Confirmados", history.size.toString(), Icons.Default.EventAvailable)
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Mis eventos creados
            Text("Mis eventos creados (${myEvents.size})",
                fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            if (myEvents.isEmpty()) {
                Text("Aún no has creado eventos.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start))
            } else {
                myEvents.take(3).forEach { event ->
                    Text("• ${event.title}", fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start).padding(vertical = 2.dp))
                }
                if (myEvents.size > 3) {
                    Text("...y ${myEvents.size - 3} más",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start))
                }
            }

            Spacer(Modifier.weight(1f))

            // Botón logout
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Se cerrará tu sesión actual.") },
            confirmButton = {
                Button(
                    onClick = {
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = Modifier.width(140.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
