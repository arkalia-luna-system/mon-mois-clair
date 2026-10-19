package be.monmoisclair.app

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Stub d'activité — UI métier Compose réservée à l'étape 3.x.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val label = TextView(this).apply {
            text = "Mon Mois Clair — étape 1.1 (données)"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        }
        setContentView(label)
    }
}
