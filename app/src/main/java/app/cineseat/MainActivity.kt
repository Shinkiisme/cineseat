package app.cineseat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.cineseat.ui.screen.CinemaScreen
import app.cineseat.ui.theme.CineSeatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CineSeatTheme { CinemaScreen() } }
    }
}
