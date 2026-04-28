package com.aireceptionist.app.ui.setup

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aireceptionist.app.databinding.ActivityLlmSetupBinding
import com.aireceptionist.app.ai.llm.ModelDownloader
import com.aireceptionist.app.ai.llm.OnDeviceLLM
import com.aireceptionist.app.ai.llm.NetworkHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity for setting up the on-device LLM
 * 
 * This activity handles:
 * - Checking if LLM model is available
 * - Downloading Phi-3.5-mini model if needed
 * - Initializing the LLM for first use
 * - Providing user feedback during setup
 */
@AndroidEntryPoint
class LLMSetupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLlmSetupBinding
    private val viewModel: LLMSetupViewModel by viewModels()
    
    @Inject
    lateinit var modelDownloader: ModelDownloader
    
    @Inject
    lateinit var onDeviceLLM: OnDeviceLLM
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLlmSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
        checkModelStatus()
    }
    
    private fun setupUI() {
        binding.apply {
            // Setup button click listeners
            btnDownloadModel.setOnClickListener {
                showDownloadConfirmation()
            }
            
            btnSkipSetup.setOnClickListener {
                // Skip LLM setup - app will use fallback responses
                setResult(RESULT_OK)
                finish()
            }
            
            btnTestLLM.setOnClickListener {
                testLLM()
            }
            
            btnContinue.setOnClickListener {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.setupState.observe(this) { state ->
            updateUI(state)
        }
        
        viewModel.downloadProgress.observe(this) { progress ->
            updateDownloadProgress(progress)
        }
    }
    
    private fun checkModelStatus() {
        lifecycleScope.launch {
            val isAvailable = modelDownloader.isModelAvailable()
            val modelInfo = modelDownloader.getModelInfo()
            
            if (isAvailable) {
                // Model already downloaded, try to initialize
                binding.apply {
                    tvStatus.text = "Model found. Initializing AI..."
                    progressBar.visibility = View.VISIBLE
                }
                
                val initialized = onDeviceLLM.initialize()
                if (initialized) {
                    viewModel.setSetupState(LLMSetupState.READY)
                } else {
                    viewModel.setSetupState(LLMSetupState.ERROR("Failed to initialize LLM"))
                }
            } else {
                viewModel.setSetupState(LLMSetupState.NEEDS_DOWNLOAD)
            }
        }
    }
    
    private fun showDownloadConfirmation() {
        // Check network connectivity first
        val hasInternet = NetworkHelper.hasInternetConnection(this)
        val connectionAdvice = NetworkHelper.getDownloadAdvice(this)
        
        if (!hasInternet) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Internet connection is required to download the AI model from Hugging Face. Please connect to WiFi or mobile data and try again.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🧠 Download Real AI Brain")
            .setMessage(
                "Download Microsoft Phi-3.5-mini (~2.3GB) from Hugging Face\n\n" +
                "✓ Complete privacy - runs entirely on your device\n" +
                "✓ Works offline after download\n" +
                "✓ True conversation intelligence\n" +
                "✓ Open-source model from Microsoft\n\n" +
                "$connectionAdvice\n\n" +
                "Source: huggingface.co/microsoft/Phi-3.5-mini-instruct-onnx"
            )
            .setPositiveButton("📥 Download Now") { _, _ ->
                checkHuggingFaceAndDownload()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun checkHuggingFaceAndDownload() {
        binding.tvStatus.text = "Checking Hugging Face connectivity..."
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val canAccessHF = NetworkHelper.canAccessHuggingFace()
            if (canAccessHF) {
                downloadModel()
            } else {
                binding.progressBar.visibility = View.GONE
                androidx.appcompat.app.AlertDialog.Builder(this@LLMSetupActivity)
                    .setTitle("Connection Issue")
                    .setMessage("Cannot reach Hugging Face servers. This might be due to:\n\n• Temporary server issues\n• Network restrictions\n• Firewall settings\n\nTry again in a few minutes or check your network connection.")
                    .setPositiveButton("Retry") { _, _ ->
                        checkHuggingFaceAndDownload()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    
    private fun downloadModel() {
        binding.apply {
            btnDownloadModel.isEnabled = false
            btnSkipSetup.isEnabled = false
            progressBar.visibility = View.VISIBLE
            tvStatus.text = "Connecting to Hugging Face..."
            
            // Show download info
            tvDownloadInfo.visibility = View.VISIBLE
            tvDownloadInfo.text = "Downloading from: huggingface.co/microsoft/Phi-3.5-mini-instruct-onnx"
        }
        
        lifecycleScope.launch {
            modelDownloader.downloadModel().collect { progress ->
                viewModel.setDownloadProgress(progress)
                
                when {
                    progress.isComplete -> {
                        // Download complete, initialize LLM
                        binding.apply {
                            tvStatus.text = "Download complete! Initializing AI brain..."
                            tvDownloadInfo.text = "Verifying model integrity and starting AI system..."
                        }
                        
                        val initialized = onDeviceLLM.initialize()
                        if (initialized) {
                            viewModel.setSetupState(LLMSetupState.READY)
                        } else {
                            viewModel.setSetupState(LLMSetupState.ERROR("Failed to initialize after download"))
                        }
                    }
                    progress.error != null -> {
                        viewModel.setSetupState(LLMSetupState.ERROR(progress.error))
                    }
                    else -> {
                        // Update download status
                        binding.tvStatus.text = "Downloading ${progress.fileName}..."
                    }
                }
            }
        }
    }
    
    private fun testLLM() {
        binding.apply {
            btnTestLLM.isEnabled = false
            tvTestResult.visibility = View.VISIBLE
            tvTestResult.text = "Testing AI response..."
        }
        
        lifecycleScope.launch {
            try {
                val testResponse = onDeviceLLM.generateResponse(
                    prompt = "Hello, I'd like to schedule an appointment",
                    maxTokens = 100
                )
                
                binding.apply {
                    tvTestResult.text = "Test successful!\nAI Response: $testResponse"
                    btnTestLLM.isEnabled = true
                }
                
            } catch (e: Exception) {
                binding.apply {
                    tvTestResult.text = "Test failed: ${e.message}"
                    btnTestLLM.isEnabled = true
                }
            }
        }
    }
    
    private fun updateUI(state: LLMSetupState) {
        binding.apply {
            when (state) {
                is LLMSetupState.NEEDS_DOWNLOAD -> {
                    tvTitle.text = "🧠 AI Brain Setup"
                    tvStatus.text = "Download Microsoft Phi-3.5-mini to enable true AI intelligence"
                    btnDownloadModel.visibility = View.VISIBLE
                    btnDownloadModel.text = "📥 Download Real AI Brain (~2.3GB)"
                    btnSkipSetup.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    btnTestLLM.visibility = View.GONE
                    btnContinue.visibility = View.GONE
                    
                    // Show preparation info
                    tvDownloadInfo.visibility = View.VISIBLE
                    tvDownloadInfo.text = "Source: huggingface.co/microsoft/Phi-3.5-mini-instruct-onnx"
                }
                
                is LLMSetupState.DOWNLOADING -> {
                    tvTitle.text = "📥 Downloading AI Brain"
                    tvStatus.text = "Fetching Phi-3.5-mini from Hugging Face..."
                    btnDownloadModel.visibility = View.GONE
                    btnSkipSetup.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    btnTestLLM.visibility = View.GONE
                    btnContinue.visibility = View.GONE
                    
                    // Enable download info displays
                    tvDownloadInfo.visibility = View.VISIBLE
                    tvProgress.visibility = View.VISIBLE
                }
                
                is LLMSetupState.READY -> {
                    tvTitle.text = "🧠 AI Brain Ready!"
                    tvStatus.text = "Your AI Receptionist is now powered by true intelligence"
                    btnDownloadModel.visibility = View.GONE
                    btnSkipSetup.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    btnTestLLM.visibility = View.VISIBLE
                    btnContinue.visibility = View.VISIBLE
                    
                    // Show detailed model info
                    val modelInfo = onDeviceLLM.getModelInfo()
                    tvModelInfo.visibility = View.VISIBLE
                    tvModelInfo.text = buildString {
                        append("✓ Model: ${modelInfo["model"]}\n")
                        append("✓ Provider: ${modelInfo["provider"]}\n")
                        append("✓ Size: ${modelInfo["model_size"]}\n")
                        append("✓ Optimization: ${modelInfo["optimization"]}\n")
                        append("✓ Status: ${modelInfo["status"]}")
                    }
                    
                    // Clear download info
                    tvDownloadInfo.visibility = View.GONE
                    tvEstimatedTime.visibility = View.GONE
                    tvProgress.visibility = View.GONE
                }
                
                is LLMSetupState.ERROR -> {
                    tvTitle.text = "Download Error"
                    tvStatus.text = "Error: ${state.message}"
                    btnDownloadModel.visibility = View.VISIBLE
                    btnDownloadModel.text = "🔄 Retry Download"
                    btnSkipSetup.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    btnTestLLM.visibility = View.GONE
                    btnContinue.visibility = View.VISIBLE
                    
                    // Show troubleshooting info
                    tvDownloadInfo.visibility = View.VISIBLE
                    tvDownloadInfo.text = "Check WiFi connection and try again. Model downloads from Hugging Face."
                    tvEstimatedTime.visibility = View.VISIBLE
                    tvEstimatedTime.text = "ℹ️ Tip: Use WiFi for large model downloads"
                }
            }
        }
    }
    
    private fun updateDownloadProgress(progress: ModelDownloader.DownloadProgress) {
        binding.apply {
            progressBar.progress = progress.percentage
            tvProgress.visibility = View.VISIBLE
            
            if (progress.bytesDownloaded > 0 && progress.totalBytes > 0) {
                val mbDownloaded = progress.bytesDownloaded / (1024 * 1024)
                val mbTotal = progress.totalBytes / (1024 * 1024)
                val speedMBps = calculateDownloadSpeed(progress.bytesDownloaded)
                
                tvProgress.text = "${progress.percentage}% • ${mbDownloaded}MB / ${mbTotal}MB"
                
                if (speedMBps > 0) {
                    val remainingMB = mbTotal - mbDownloaded
                    val etaMinutes = (remainingMB / speedMBps) / 60
                    tvEstimatedTime.visibility = View.VISIBLE
                    tvEstimatedTime.text = when {
                        etaMinutes < 1 -> "Almost done..."
                        etaMinutes < 60 -> "ETA: ${etaMinutes.toInt()} minutes"
                        else -> "ETA: ${(etaMinutes / 60).toInt()}h ${(etaMinutes % 60).toInt()}m"
                    }
                }
                
                tvDownloadInfo.text = "Downloading ${progress.fileName} at ${String.format("%.1f", speedMBps)}MB/s"
            } else {
                tvProgress.text = "${progress.fileName}: ${progress.percentage}%"
                tvDownloadInfo.text = "Preparing download from Hugging Face..."
            }
        }
    }
    
    private var downloadStartTime = 0L
    private fun calculateDownloadSpeed(bytesDownloaded: Long): Double {
        if (downloadStartTime == 0L) downloadStartTime = System.currentTimeMillis()
        val elapsedSeconds = (System.currentTimeMillis() - downloadStartTime) / 1000.0
        return if (elapsedSeconds > 0) (bytesDownloaded / (1024.0 * 1024.0)) / elapsedSeconds else 0.0
    }
}

/**
 * States for LLM setup process
 */
sealed class LLMSetupState {
    object NEEDS_DOWNLOAD : LLMSetupState()
    object DOWNLOADING : LLMSetupState()
    object READY : LLMSetupState()
    data class ERROR(val message: String) : LLMSetupState()
}