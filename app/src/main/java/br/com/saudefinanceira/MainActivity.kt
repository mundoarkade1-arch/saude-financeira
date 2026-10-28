package br.com.saudefinanceira

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.saudefinanceira.ui.AppRoot
import br.com.saudefinanceira.ui.theme.SaudeFinanceiraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = (application as FinanceApp).db.dao()
        setContent {
            SaudeFinanceiraTheme {
                AppRoot(dao)
            }
        }
    }
}
