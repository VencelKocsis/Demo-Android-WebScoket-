package hu.bme.aut.android.demo.feature.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.demo.R
import hu.bme.aut.android.demo.domain.market.model.MarketItem

/**
 * A Piac képernyő Compose megvalósítása.
 * * "Buta" UI: Csak megjeleníti az adatokat, a műveleteket [MarketEvent] formájában
 * adja át a ViewModel-nek.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    onNavigateBack: () -> Unit,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Üzenetek (Snackbar) kezelése - Csak akkor fut le, ha a state megváltozik
    LaunchedEffect(uiState.errorMessage, uiState.inquirySuccessMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(MarketEvent.ClearMessages)
        }
        uiState.inquirySuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(MarketEvent.ClearMessages)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.market), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Visszalépés")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(MarketEvent.RefreshMarket) }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Frissítés")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.items.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_racket_for_sale),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.items, key = { it.equipment.id ?: 0 }) { item ->
                        MarketItemCard(
                            item = item,
                            onInquireClick = { equipmentId ->
                                viewModel.onEvent(MarketEvent.InquireAboutEquipment(equipmentId))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Egy eladó ütő kártyájának Compose vizuális eleme.
 */
@Composable
fun MarketItemCard(
    item: MarketItem,
    onInquireClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.ownerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(
                    R.string.racket_v3,
                    item.equipment.bladeManufacturer,
                    item.equipment.bladeModel
                ),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.forehand_v3,
                    item.equipment.fhRubberManufacturer,
                    item.equipment.fhRubberModel,
                    item.equipment.fhRubberColor
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.backhand_v3,
                    item.equipment.bhRubberManufacturer,
                    item.equipment.bhRubberModel,
                    item.equipment.bhRubberColor
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { item.equipment.id?.let { onInquireClick(it) } },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.interested))
            }
        }
    }
}