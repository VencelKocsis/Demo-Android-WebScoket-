package hu.bme.aut.android.demo.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Az egységes hitelesítési képernyő, amely kezeli a bejelentkezést és a regisztrációt.
 *
 * @param viewModel Az AuthViewModel példánya.
 * @param onAuthSuccess Esemény, ami a sikeres hitelesítés után fut le.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: OnAuthSuccess // Az AuthViewModel.kt fájlból importálva/elérve
) {
    // A ViewModel állapotának figyelése
    // Átváltás: collectAsStateWithLifecycle() helyett collectAsState()
    val state by viewModel.uiState.collectAsState()

    // Lokális UI állapot a bejelentkezés/regisztráció mód váltásához
    var isLoginMode by remember { mutableStateOf(true) } // true = Bejelentkezés, false = Regisztráció

    // Navigáció kezelése sikeres hitelesítés esetén
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated && state.currentUser != null) {
            // Töröljük az űrlap mezőit a sikeres hitelesítés után
            viewModel.updateEmail("")
            viewModel.updatePassword("")
            onAuthSuccess(state.currentUser!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLoginMode) "Bejelentkezés" else "Regisztráció") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- Cím megjelenítése a módtól függően ---
            AnimatedContent(
                targetState = isLoginMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "Title Transition"
            ) { targetLoginMode ->
                Text(
                    text = if (targetLoginMode) "Üdvözöljük újra!" else "Hozzon létre fiókot",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }


            // --- E-mail beviteli mező ---
            OutlinedTextField(
                value = state.emailInput,
                onValueChange = viewModel::updateEmail,
                label = { Text("E-mail cím") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "E-mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Jelszó beviteli mező ---
            OutlinedTextField(
                value = state.passwordInput,
                onValueChange = viewModel::updatePassword,
                label = { Text("Jelszó") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Jelszó") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // --- Hibaüzenet ---
            state.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Fő Műveleti Gomb (Bejelentkezés/Regisztráció) ---
            Button(
                onClick = {
                    if (isLoginMode) {
                        viewModel.signIn()
                    } else {
                        viewModel.register()
                    }
                },
                enabled = !state.isLoading && state.emailInput.isNotBlank() && state.passwordInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(if (isLoginMode) "BEJELENTKEZÉS" else "REGISZTRÁCIÓ")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Módváltó Gomb ---
            TextButton(
                onClick = {
                    // Átváltás a másik módra és a hibaüzenet törlése
                    isLoginMode = !isLoginMode
                    viewModel.clearError() // A frissen hozzáadott funkció meghívása
                },
                enabled = !state.isLoading
            ) {
                Text(
                    if (isLoginMode) {
                        "Nincs még fiókja? Regisztráljon most!"
                    } else {
                        "Van már fiókja? Jelentkezzen be!"
                    }
                )
            }
        }
    }
}
