package hu.bme.aut.android.demo.feature.auth

import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseUser

// A navigációs események kezelésére szolgáló lambda (pl. navigálás a főképernyőre)
typealias OnAuthSuccess = (FirebaseUser) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    // A Hilt automatikusan biztosítja a ViewModel-t
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: OnAuthSuccess
) {
    // Figyeli a ViewModel állapotát
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Ha a felhasználó hitelesítése sikeres, navigáljunk
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated && state.currentUser != null) {
            onAuthSuccess(state.currentUser!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bejelentkezés / Regisztráció") }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // E-mail beviteli mező
                OutlinedTextField(
                    value = state.emailInput,
                    onValueChange = viewModel::updateEmail,
                    label = { Text("E-mail cím") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "E-mail") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Jelszó beviteli mező
                OutlinedTextField(
                    value = state.passwordInput,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Jelszó") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Jelszó") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                // Hibaüzenet
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

                // Bejelentkezés gomb
                Button(
                    onClick = viewModel::signIn,
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Bejelentkezés")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Regisztráció gomb
                OutlinedButton(
                    onClick = viewModel::register,
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Regisztráció")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kijelentkezés gomb

            }
        }
    )
}
