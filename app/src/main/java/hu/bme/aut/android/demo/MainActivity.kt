package hu.bme.aut.android.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import hu.bme.aut.android.demo.feature.list_players.DemoScreen
import hu.bme.aut.android.demo.feature.list_players.PlayersViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = PlayersViewModel()
        setContent {
            MaterialTheme {
                DemoScreen(viewModel = vm)
            }
        }
    }
}
