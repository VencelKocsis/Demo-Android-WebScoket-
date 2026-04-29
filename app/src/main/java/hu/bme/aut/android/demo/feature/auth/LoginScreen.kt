package hu.bme.aut.android.demo.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseUser
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.ui.theme.SuccessGreen

typealias OnAuthSuccess = (FirebaseUser) -> Unit

/**
 * A bejelentkezést és regisztrációt kezelő Compose UI képernyő.
 * * "Buta" komponens: Csak az [AuthUiState]-ből kapott adatokat jeleníti meg,
 * és a felhasználói interakciókat továbbítja a ViewModel felé.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: OnAuthSuccess
) {
    // A UI State reaktív megfigyelése életciklus-tudatosan
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Lokális UI állapot (csak a vizuális megjelenítést befolyásolja, így maradhat itt)
    var passwordVisible by remember { mutableStateOf(false) }

    // Navigáció kiváltása, ha a bejelentkezés sikeres
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated && state.currentUser != null) {
            onAuthSuccess(state.currentUser!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.login_registration)) }
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
                    label = { Text(stringResource(R.string.e_mail_address)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "E-mail") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Jelszó beviteli mező
                OutlinedTextField(
                    value = state.passwordInput,
                    onValueChange = viewModel::updatePassword,
                    label = { Text(stringResource(R.string.password)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Jelszó") },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Jelszó elrejtése" else "Jelszó megjelenítése"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
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

                // Sikerüzenet
                state.successMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = SuccessGreen,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Elfelejtett jelszó gomb
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = viewModel::forgotPassword,
                        enabled = !state.isLoading && state.emailInput.isNotBlank()
                    ) {
                        Text(stringResource(R.string.forgot_password))
                    }
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
                        Text(stringResource(R.string.login))
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
                    Text(stringResource(R.string.register))
                }
            }
        }
    )
}