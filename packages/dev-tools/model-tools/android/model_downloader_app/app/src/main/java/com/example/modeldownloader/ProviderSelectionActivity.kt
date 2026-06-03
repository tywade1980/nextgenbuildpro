package com.example.modeldownloader

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * ProviderSelectionActivity presents a simple guided flow to the user for selecting
 * a model provider and entering the name of the model to download.  If the
 * selected provider requires authentication and the user has not previously
 * authorized, the activity will initiate a stubbed login flow via [AuthManager].
 * After selecting a provider and model, [ModelDownloader.downloadModel] is
 * invoked with the chosen parameters.
 */
class ProviderSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Immediately show the provider selection dialog when the activity is created.
        showProviderSelectionDialog()
    }

    /**
     * Presents a dialog to the user with the list of supported providers.
     * On selection, prompts the user for a model name and kicks off the
     * download process.
     */
    private fun showProviderSelectionDialog() {
        val providers = Provider.values()
        val providerNames = providers.map { it.displayName }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Choose a model provider")
            .setItems(providerNames) { _, which ->
                val provider = providers[which]
                promptForModelName(provider)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Prompts the user to enter a model name or identifier.  Once provided,
     * performs authentication if necessary and then triggers the download.
     */
    private fun promptForModelName(provider: Provider) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(this)
            .setTitle("Enter model identifier")
            .setView(input)
            .setPositiveButton("Download") { _, _ ->
                val modelName = input.text.toString().trim()
                if (modelName.isEmpty()) {
                    Toast.makeText(this, "Model name cannot be empty", Toast.LENGTH_SHORT).show()
                    // Restart the prompt if no name provided
                    promptForModelName(provider)
                    return@setPositiveButton
                }
                beginDownload(provider, modelName)
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Handles authentication (if needed) and kicks off the model download.
     */
    private fun beginDownload(provider: Provider, modelName: String) {
        // If this provider requires authentication and we have not yet
        // authenticated, initiate the login flow.
        if (provider.requiresAuthentication && !AuthManager.isAuthenticated(provider)) {
            AuthManager.login(this, provider) { success ->
                if (success) {
                    download(provider, modelName)
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        } else {
            download(provider, modelName)
        }
    }

    /**
     * Delegates the actual download to [ModelDownloader] and shows a toast
     * indicating success or failure.
     */
    private fun download(provider: Provider, modelName: String) {
        ModelDownloader.downloadModel(this, provider, modelName) { success ->
            runOnUiThread {
                val message = if (success) {
                    "Model downloaded successfully"
                } else {
                    "Model download failed"
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    companion object {
        /**
         * Creates an intent to launch this activity.  Use this helper
         * method rather than instantiating the intent directly to make it
         * clearer where the activity is coming from.
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, ProviderSelectionActivity::class.java)
        }
    }
}