package com.example.modeldownloader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity starts the model download process immediately when the app
 * launches. It shows a Toast upon completion and finishes itself.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kick off a provider selection flow instead of downloading a hard‑coded model.
        // This activity now delegates to ProviderSelectionActivity, which
        // guides the user through choosing a provider and model name and
        // handles authentication when necessary.
        startActivity(
            ProviderSelectionActivity.createIntent(this)
        )
        // Once the selection flow is launched, we finish ourselves so that
        // the back stack remains clean.
        finish()
    }
}