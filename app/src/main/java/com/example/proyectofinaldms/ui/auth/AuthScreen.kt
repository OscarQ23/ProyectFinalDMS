package com.example.proyectofinaldms.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }

    // Campos Login
    var loginEmail    by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPassVisible by remember { mutableStateOf(false) }

    // Campos Registro
    var regName     by remember { mutableStateOf("") }
    var regEmail    by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirm  by remember { mutableStateOf("") }
    var regPassVisible    by remember { mutableStateOf(false) }
    var regConfirmVisible by remember { mutableStateOf(false) }

    // Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("292897120366-hc4lqf7d86jpkcd4r6omtbj3o2sv482k.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleClient  = remember { GoogleSignIn.getClient(context, gso) }
    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                viewModel.loginWithGoogle(account)
            } catch (_: ApiException) { }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onAuthSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo degradado en la parte superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // ─── Icono y título ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Eventos Comunitarios",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Conecta con tu comunidad",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(32.dp))

            // ─── Card principal ───────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text("Iniciar Sesión", modifier = Modifier.padding(12.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text("Registrarse", modifier = Modifier.padding(12.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (selectedTab == 0) {
                        // ─── Login ────────────────────────────────────────
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Correo electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { loginPassVisible = !loginPassVisible }) {
                                    Icon(
                                        if (loginPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (loginPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.login(loginEmail.trim(), loginPassword) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = authState !is AuthState.Loading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Iniciar Sesión", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                    } else {
                        // ─── Registro ─────────────────────────────────────
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Nombre completo") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Correo electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { regPassVisible = !regPassVisible }) {
                                    Icon(
                                        if (regPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (regPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = regConfirm,
                            onValueChange = { regConfirm = it },
                            label = { Text("Confirmar contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { regConfirmVisible = !regConfirmVisible }) {
                                    Icon(
                                        if (regConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (regConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = regConfirm.isNotEmpty() && regPassword != regConfirm,
                            supportingText = {
                                if (regConfirm.isNotEmpty() && regPassword != regConfirm) {
                                    Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (regPassword == regConfirm) {
                                    viewModel.register(regName.trim(), regEmail.trim(), regPassword)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = authState !is AuthState.Loading &&
                                    regName.isNotBlank() && regEmail.isNotBlank() &&
                                    regPassword.isNotBlank() && regPassword == regConfirm,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Crear Cuenta", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ─── Divider ──────────────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            "  o continúa con  ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    // ─── Botón Google ─────────────────────────────────────
                    OutlinedButton(
                        onClick = { googleLauncher.launch(googleClient.signInIntent) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔵", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text("Continuar con Google", fontWeight = FontWeight.Medium)
                    }

                    // ─── Error ────────────────────────────────────────────
                    if (authState is AuthState.Error) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    if (authState is AuthState.Loading) {
                        Spacer(Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Al registrarte aceptas nuestros términos de uso",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
