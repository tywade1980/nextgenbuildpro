# Model Downloader App

This Android application acts as a **silent installer** and guided downloader for large language model (LLM) files.  
When installed and launched, the app presents a minimal UI to guide the user through choosing a model provider (e.g. Ollama, Hugging Face or GitHub), entering a model name and performing any necessary authentication.  
Once authorized, it downloads the selected model and saves it in the device's internal storage.  
After download completes the app exits, allowing other applications (e.g. the Telephony Agent) to locate and load the model later.

## Features

- **Provider selection** – choose between multiple model providers (currently Ollama, Hugging Face and GitHub) from a simple list.  
- **Guided login** – for providers that require authentication, the app launches the provider’s login page using a custom tab and stores an access token on your device.  Public models on Hugging Face or GitHub do not require a token.  
- **Model identifier input** – after selecting a provider you can enter the name or path of the desired model; the app constructs an appropriate download URL for you.  
- **Safe storage** – stores the downloaded file in `filesDir/models/` within the app’s private storage.  
- **Simple and modular** – designed to be used as a separate module or installed alongside other applications.

## Usage

1. Build and install the APK on your device.  
2. When you launch the app, it will immediately show a provider selection dialog. Pick your model provider (e.g. “Hugging Face”) and follow the prompts:  
   - If the provider requires authentication (Hugging Face or GitHub), sign in when the login page appears and grant access.  You only need to do this once.  
   - Enter the model identifier when prompted. For Hugging Face this is the repository ID (e.g. `microsoft/Phi-3-mini-4k-instruct`). For GitHub it’s the owner/repo/path (e.g. `myuser/myrepo/models/model.onnx`). For Ollama it’s the model name (e.g. `phi3`).
3. The app will download the model to `context.filesDir/models/<fileName>` and notify you when complete.  
4. Other applications can access the downloaded model by constructing the path: `context.filesDir/models/<fileName>`.

## Technical Details

- **Kotlin 2.2.20** and **AndroidX** libraries.  
- Uses **OkHttp** for downloading models and **Custom Tabs** to launch provider login pages.  
- Provides simple stubs for OAuth flows via an `AuthManager`; in a production app you would integrate the full OAuth process and handle redirect URIs.  
- Minimum SDK is 29 to align with the Telephony Agent project.  
- Requires the `INTERNET` permission; no storage permission is needed since the model is stored in private app storage.

## Limitations

- The OAuth flows in `AuthManager` are simplified for illustration.  You must implement proper client IDs, redirect URIs and token handling for real production use.  
- The app guesses the file name for Hugging Face by appending `.onnx` to the repository name.  If your model file has a different name or lives in a subdirectory, the download may fail.  
- Progress notifications, retries and integrity checks (e.g. checksum verification) are not implemented but can be added using WorkManager.