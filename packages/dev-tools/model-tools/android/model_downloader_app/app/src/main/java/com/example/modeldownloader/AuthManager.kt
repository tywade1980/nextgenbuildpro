package com.example.modeldownloader

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

/**
 * AuthManager centralizes authentication logic for providers that require
 * user authorization.  In a production application you would integrate
 * proper OAuth flows here, leveraging the provider's authorization
 * endpoint.  This implementation provides a minimal stub that simulates
 * successful authentication and demonstrates where an OAuth flow would
 * begin.
 */
object AuthManager {
    private const val PREF_NAME = "auth_tokens"

    /**
     * Returns whether the user has previously authenticated for the given
     * provider.  In this simple implementation we check for the presence
     * of an access token in SharedPreferences.
     */
    fun isAuthenticated(provider: Provider): Boolean {
        val prefs = AppGlobals.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.contains(provider.name)
    }

    /**
     * Retrieves a stored access token for the given provider, or null
     * if none is present.
     */
    fun getAccessToken(provider: Provider): String? {
        val prefs = AppGlobals.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(provider.name, null)
    }

    /**
     * Saves an access token for the given provider.
     */
    private fun saveAccessToken(provider: Provider, token: String) {
        val prefs = AppGlobals.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(provider.name, token).apply()
    }

    /**
     * Initiates a login flow for the given provider.  Once the user
     * completes authentication, [onComplete] will be invoked with the
     * outcome.  A real implementation would handle the OAuth redirect
     * callbacks and extract an access token.  Here we simply simulate a
     * successful login and store a fake token.
     */
    fun login(context: Context, provider: Provider, onComplete: (Boolean) -> Unit) {
        when (provider) {
            Provider.HUGGING_FACE -> loginHuggingFace(context, onComplete)
            Provider.GITHUB -> loginGitHub(context, onComplete)
            else -> onComplete(true) // Ollama does not require authentication
        }
    }

    private fun loginHuggingFace(context: Context, onComplete: (Boolean) -> Unit) {
        // In a real application, launch the provider's OAuth authorization URL.
        // Example: https://huggingface.co/login/oauth/authorize?client_id=...&redirect_uri=...
        // Here we simulate success by storing a dummy token after launching a custom tab.
        launchLoginWebpage(context, "https://huggingface.co/login") {
            // Simulate retrieving an access token
            saveAccessToken(Provider.HUGGING_FACE, "dummy_hf_token")
            onComplete(true)
        }
    }

    private fun loginGitHub(context: Context, onComplete: (Boolean) -> Unit) {
        // Launch GitHub's login page.  Replace with your GitHub OAuth
        // client details and redirect handling in a real implementation.
        launchLoginWebpage(context, "https://github.com/login") {
            saveAccessToken(Provider.GITHUB, "dummy_gh_token")
            onComplete(true)
        }
    }

    /**
     * Opens a web page using Custom Tabs for the user to perform
     * authentication.  This method accepts a callback that is invoked
     * once the tab is closed.  In a production app you would listen
     * for an OAuth redirect URI to indicate completion; here we simply
     * invoke the callback immediately after launching the tab.
     */
    private fun launchLoginWebpage(context: Context, url: String, onLoaded: () -> Unit) {
        try {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
            // Immediately invoke the callback; in a real implementation you
            // would wait for a redirect to your app scheme.
            onLoaded()
        } catch (e: Exception) {
            Log.e("AuthManager", "Failed to launch login page", e)
            Toast.makeText(context, "Failed to launch login page", Toast.LENGTH_LONG).show()
            onLoaded()
        }
    }
}