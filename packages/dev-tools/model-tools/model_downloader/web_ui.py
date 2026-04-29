"""
Web UI for model downloader and LLM chat interface.
Provides a user-friendly web interface for downloading and interacting with models.
"""

import logging
import json
import os
from pathlib import Path
from typing import Dict, List, Any
import threading
import time

try:
    from flask import Flask, render_template_string, request, jsonify, send_from_directory
    FLASK_AVAILABLE = True
except ImportError:
    FLASK_AVAILABLE = False
    logging.warning("Flask not available. Web UI will not work.")

from .config import Config
from .downloader import ModelDownloader
from .chat import ChatInterface
from .speech import SpeechManager
from .notifications import NotificationManager

logger = logging.getLogger(__name__)


# HTML Templates
HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Model Downloader & Chat</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            text-align: center;
        }
        .tabs {
            display: flex;
            background: #f8f9fa;
            border-bottom: 1px solid #dee2e6;
        }
        .tab {
            flex: 1;
            padding: 15px;
            text-align: center;
            cursor: pointer;
            border: none;
            background: none;
            font-size: 14px;
            font-weight: 500;
        }
        .tab.active {
            background: white;
            border-bottom: 2px solid #667eea;
            color: #667eea;
        }
        .content {
            display: none;
            padding: 20px;
        }
        .content.active {
            display: block;
        }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: 500;
        }
        input, select, textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            box-sizing: border-box;
        }
        button {
            background: #667eea;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
        }
        button:hover {
            background: #5a6fd8;
        }
        button:disabled {
            background: #ccc;
            cursor: not-allowed;
        }
        .chat-container {
            border: 1px solid #ddd;
            border-radius: 5px;
            height: 400px;
            overflow-y: auto;
            padding: 10px;
            background: #fafafa;
            margin-bottom: 15px;
        }
        .message {
            margin-bottom: 10px;
            padding: 8px 12px;
            border-radius: 5px;
            max-width: 80%;
        }
        .user-message {
            background: #667eea;
            color: white;
            margin-left: auto;
            text-align: right;
        }
        .bot-message {
            background: white;
            border: 1px solid #ddd;
        }
        .status {
            padding: 10px;
            margin: 10px 0;
            border-radius: 5px;
        }
        .status.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .status.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .status.info {
            background: #cce7ff;
            color: #004085;
            border: 1px solid #b8daff;
        }
        .model-list {
            list-style: none;
            padding: 0;
        }
        .model-item {
            padding: 10px;
            margin: 5px 0;
            background: #f8f9fa;
            border-radius: 5px;
            border: 1px solid #e9ecef;
        }
        .speech-controls {
            display: flex;
            gap: 10px;
            margin-top: 10px;
        }
        .loading {
            display: none;
            text-align: center;
            padding: 20px;
        }
        .spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #667eea;
            border-radius: 50%;
            width: 30px;
            height: 30px;
            animation: spin 1s linear infinite;
            margin: 0 auto;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🤖 Model Downloader & Chat Interface</h1>
            <p>Download LLM models and chat with them using text or voice</p>
        </div>
        
        <div class="tabs">
            <button class="tab active" onclick="showTab('download')">📥 Download Models</button>
            <button class="tab" onclick="showTab('chat')">💬 Chat</button>
            <button class="tab" onclick="showTab('speech')">🎤 Speech</button>
            <button class="tab" onclick="showTab('status')">📊 Status</button>
        </div>
        
        <div id="download" class="content active">
            <h3>Download Models</h3>
            <form id="downloadForm">
                <div class="form-group">
                    <label for="modelName">Model Name:</label>
                    <input type="text" id="modelName" placeholder="e.g., microsoft/DialoGPT-medium" required>
                </div>
                <div class="form-group">
                    <label for="source">Source:</label>
                    <select id="source">
                        <option value="huggingface">Hugging Face</option>
                        <option value="url">Direct URL</option>
                    </select>
                </div>
                <button type="submit">Download Model</button>
            </form>
            <div id="downloadStatus"></div>
            
            <h4>Available Models</h4>
            <ul id="modelList" class="model-list"></ul>
        </div>
        
        <div id="chat" class="content">
            <h3>Chat with Models</h3>
            <div class="form-group">
                <label for="selectedModel">Select Model:</label>
                <select id="selectedModel">
                    <option value="">Select a model...</option>
                </select>
                <button onclick="loadModel()">Load Model</button>
            </div>
            
            <div id="chatContainer" class="chat-container"></div>
            
            <div class="form-group">
                <input type="text" id="chatInput" placeholder="Type your message..." onkeypress="handleChatKeypress(event)">
                <button onclick="sendMessage()">Send</button>
            </div>
            
            <div class="speech-controls">
                <button onclick="startVoiceInput()">🎤 Voice Input</button>
                <button onclick="speakLastResponse()">🔊 Speak Response</button>
            </div>
            
            <div id="chatStatus"></div>
        </div>
        
        <div id="speech" class="content">
            <h3>Speech Testing</h3>
            
            <div class="form-group">
                <label for="ttsText">Text to Speech:</label>
                <textarea id="ttsText" rows="3" placeholder="Enter text to convert to speech..."></textarea>
                <button onclick="speakText()">🔊 Speak</button>
            </div>
            
            <div class="form-group">
                <label>Speech to Text:</label>
                <button onclick="startListening()">🎤 Start Listening</button>
                <div id="sttResult"></div>
            </div>
            
            <div id="speechCapabilities"></div>
        </div>
        
        <div id="status" class="content">
            <h3>System Status</h3>
            <div id="systemStatus"></div>
            
            <h4>Recent Logs</h4>
            <div id="recentLogs"></div>
            <button onclick="refreshLogs()">Refresh Logs</button>
        </div>
    </div>
    
    <div id="loading" class="loading">
        <div class="spinner"></div>
        <p>Processing...</p>
    </div>

    <script>
        let currentModel = null;
        let chatHistory = [];
        
        function showTab(tabName) {
            // Hide all content
            document.querySelectorAll('.content').forEach(content => {
                content.classList.remove('active');
            });
            document.querySelectorAll('.tab').forEach(tab => {
                tab.classList.remove('active');
            });
            
            // Show selected content
            document.getElementById(tabName).classList.add('active');
            event.target.classList.add('active');
            
            // Load data when tab is shown
            if (tabName === 'download') {
                loadModelList();
            } else if (tabName === 'chat') {
                loadChatModels();
            } else if (tabName === 'speech') {
                loadSpeechCapabilities();
            } else if (tabName === 'status') {
                loadSystemStatus();
            }
        }
        
        function showStatus(elementId, message, type = 'info') {
            const element = document.getElementById(elementId);
            element.innerHTML = `<div class="status ${type}">${message}</div>`;
        }
        
        function showLoading(show = true) {
            document.getElementById('loading').style.display = show ? 'block' : 'none';
        }
        
        // Download functionality
        document.getElementById('downloadForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const modelName = document.getElementById('modelName').value;
            const source = document.getElementById('source').value;
            
            showLoading(true);
            try {
                const response = await fetch('/api/download', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({model: modelName, source: source})
                });
                const result = await response.json();
                showStatus('downloadStatus', result.message, result.success ? 'success' : 'error');
                if (result.success) {
                    loadModelList();
                }
            } catch (error) {
                showStatus('downloadStatus', 'Download failed: ' + error.message, 'error');
            }
            showLoading(false);
        });
        
        async function loadModelList() {
            try {
                const response = await fetch('/api/models');
                const models = await response.json();
                const list = document.getElementById('modelList');
                list.innerHTML = models.map(model => 
                    `<li class="model-item">📦 ${model}</li>`
                ).join('');
            } catch (error) {
                console.error('Error loading models:', error);
            }
        }
        
        // Chat functionality
        async function loadChatModels() {
            try {
                const response = await fetch('/api/models');
                const models = await response.json();
                const select = document.getElementById('selectedModel');
                select.innerHTML = '<option value="">Select a model...</option>' +
                    models.map(model => `<option value="${model}">${model}</option>`).join('');
            } catch (error) {
                console.error('Error loading chat models:', error);
            }
        }
        
        async function loadModel() {
            const modelName = document.getElementById('selectedModel').value;
            if (!modelName) return;
            
            showLoading(true);
            try {
                const response = await fetch('/api/load_model', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({model: modelName})
                });
                const result = await response.json();
                showStatus('chatStatus', result.message, result.success ? 'success' : 'error');
                if (result.success) {
                    currentModel = modelName;
                }
            } catch (error) {
                showStatus('chatStatus', 'Failed to load model: ' + error.message, 'error');
            }
            showLoading(false);
        }
        
        function handleChatKeypress(event) {
            if (event.key === 'Enter') {
                sendMessage();
            }
        }
        
        async function sendMessage() {
            const input = document.getElementById('chatInput');
            const message = input.value.trim();
            if (!message || !currentModel) return;
            
            // Add user message to chat
            addMessageToChat(message, 'user');
            input.value = '';
            
            showLoading(true);
            try {
                const response = await fetch('/api/chat', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({message: message})
                });
                const result = await response.json();
                if (result.success) {
                    addMessageToChat(result.response, 'bot');
                } else {
                    addMessageToChat('Error: ' + result.message, 'bot');
                }
            } catch (error) {
                addMessageToChat('Error: ' + error.message, 'bot');
            }
            showLoading(false);
        }
        
        function addMessageToChat(message, sender) {
            const container = document.getElementById('chatContainer');
            const messageDiv = document.createElement('div');
            messageDiv.className = `message ${sender}-message`;
            messageDiv.textContent = message;
            container.appendChild(messageDiv);
            container.scrollTop = container.scrollHeight;
            
            chatHistory.push({sender, message});
        }
        
        // Speech functionality
        async function speakText() {
            const text = document.getElementById('ttsText').value;
            if (!text) return;
            
            try {
                const response = await fetch('/api/speak', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({text: text})
                });
                const result = await response.json();
                showStatus('speechCapabilities', result.message, result.success ? 'success' : 'error');
            } catch (error) {
                showStatus('speechCapabilities', 'TTS error: ' + error.message, 'error');
            }
        }
        
        async function startListening() {
            try {
                showStatus('sttResult', 'Listening...', 'info');
                const response = await fetch('/api/listen', {method: 'POST'});
                const result = await response.json();
                if (result.success) {
                    document.getElementById('sttResult').innerHTML = 
                        `<div class="status success">Recognized: "${result.text}"</div>`;
                } else {
                    showStatus('sttResult', result.message, 'error');
                }
            } catch (error) {
                showStatus('sttResult', 'STT error: ' + error.message, 'error');
            }
        }
        
        async function loadSpeechCapabilities() {
            try {
                const response = await fetch('/api/speech_capabilities');
                const caps = await response.json();
                document.getElementById('speechCapabilities').innerHTML = `
                    <div class="status info">
                        <strong>Speech Capabilities:</strong><br>
                        TTS Available: ${caps.tts_available ? '✅' : '❌'}<br>
                        STT Available: ${caps.stt_available ? '✅' : '❌'}<br>
                        Voices Available: ${caps.voices_available ? '✅' : '❌'}
                    </div>
                `;
            } catch (error) {
                console.error('Error loading speech capabilities:', error);
            }
        }
        
        async function startVoiceInput() {
            if (!currentModel) {
                showStatus('chatStatus', 'Please load a model first', 'error');
                return;
            }
            
            try {
                showStatus('chatStatus', 'Listening for voice input...', 'info');
                const response = await fetch('/api/listen', {method: 'POST'});
                const result = await response.json();
                if (result.success && result.text) {
                    document.getElementById('chatInput').value = result.text;
                    sendMessage();
                } else {
                    showStatus('chatStatus', result.message || 'No speech detected', 'error');
                }
            } catch (error) {
                showStatus('chatStatus', 'Voice input error: ' + error.message, 'error');
            }
        }
        
        async function speakLastResponse() {
            const lastBotMessage = chatHistory.filter(m => m.sender === 'bot').pop();
            if (!lastBotMessage) return;
            
            try {
                const response = await fetch('/api/speak', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({text: lastBotMessage.message})
                });
                const result = await response.json();
                showStatus('chatStatus', result.message, result.success ? 'success' : 'error');
            } catch (error) {
                showStatus('chatStatus', 'TTS error: ' + error.message, 'error');
            }
        }
        
        // Status functionality
        async function loadSystemStatus() {
            try {
                const response = await fetch('/api/status');
                const status = await response.json();
                document.getElementById('systemStatus').innerHTML = `
                    <div class="status info">
                        <strong>System Status:</strong><br>
                        Models Available: ${status.models_count}<br>
                        Current Model: ${status.current_model || 'None'}<br>
                        Download Directory: ${status.download_dir}<br>
                        TTS Available: ${status.speech_capabilities.tts_available ? '✅' : '❌'}<br>
                        STT Available: ${status.speech_capabilities.stt_available ? '✅' : '❌'}
                    </div>
                `;
                refreshLogs();
            } catch (error) {
                console.error('Error loading system status:', error);
            }
        }
        
        async function refreshLogs() {
            try {
                const response = await fetch('/api/logs');
                const result = await response.json();
                document.getElementById('recentLogs').innerHTML = 
                    `<pre style="background: #f5f5f5; padding: 10px; border-radius: 5px; max-height: 300px; overflow-y: auto;">${result.logs}</pre>`;
            } catch (error) {
                console.error('Error loading logs:', error);
            }
        }
        
        // Initialize
        document.addEventListener('DOMContentLoaded', () => {
            loadModelList();
        });
    </script>
</body>
</html>
"""


class WebUI:
    """Web-based user interface for model downloader and chat."""
    
    def __init__(self, config: Config, host: str = '127.0.0.1', port: int = 5000):
        """Initialize web UI.
        
        Args:
            config: Configuration instance
            host: Host to bind to
            port: Port to listen on
        """
        if not FLASK_AVAILABLE:
            raise ImportError("Flask is required for web UI. Install with: pip install flask")
        
        self.config = config
        self.host = host
        self.port = port
        
        # Initialize components
        self.downloader = ModelDownloader(config)
        self.chat_interface = ChatInterface(config)
        self.speech_manager = SpeechManager(config.config)
        self.notification_manager = NotificationManager(config)
        
        # Create Flask app
        self.app = Flask(__name__)
        self.setup_routes()
        
    def setup_routes(self):
        """Setup Flask routes."""
        
        @self.app.route('/')
        def index():
            return render_template_string(HTML_TEMPLATE)
        
        @self.app.route('/api/models')
        def get_models():
            models = self.chat_interface.list_models()
            return jsonify(models)
        
        @self.app.route('/api/download', methods=['POST'])
        def download_model():
            data = request.json
            model_name = data.get('model')
            source = data.get('source', 'huggingface')
            
            try:
                success = self.downloader.download_model(model_name, source)
                return jsonify({
                    'success': success,
                    'message': f"Model {'downloaded successfully' if success else 'download failed'}"
                })
            except Exception as e:
                return jsonify({'success': False, 'message': str(e)})
        
        @self.app.route('/api/load_model', methods=['POST'])
        def load_model():
            data = request.json
            model_name = data.get('model')
            
            try:
                success = self.chat_interface.model_loader.load_model(model_name)
                return jsonify({
                    'success': success,
                    'message': f"Model {'loaded successfully' if success else 'failed to load'}"
                })
            except Exception as e:
                return jsonify({'success': False, 'message': str(e)})
        
        @self.app.route('/api/chat', methods=['POST'])
        def chat():
            data = request.json
            message = data.get('message')
            
            try:
                if not self.chat_interface.model_loader.is_model_loaded():
                    return jsonify({'success': False, 'message': 'No model loaded'})
                
                response = self.chat_interface.generate_response(message)
                return jsonify({'success': True, 'response': response})
            except Exception as e:
                return jsonify({'success': False, 'message': str(e)})
        
        @self.app.route('/api/speak', methods=['POST'])
        def speak():
            data = request.json
            text = data.get('text')
            
            try:
                success = self.speech_manager.speak(text)
                return jsonify({
                    'success': success,
                    'message': 'Text spoken successfully' if success else 'TTS failed'
                })
            except Exception as e:
                return jsonify({'success': False, 'message': str(e)})
        
        @self.app.route('/api/listen', methods=['POST'])
        def listen():
            try:
                text = self.speech_manager.listen(timeout=5.0)
                if text:
                    return jsonify({'success': True, 'text': text})
                else:
                    return jsonify({'success': False, 'message': 'No speech detected'})
            except Exception as e:
                return jsonify({'success': False, 'message': str(e)})
        
        @self.app.route('/api/speech_capabilities')
        def get_speech_capabilities():
            return jsonify(self.speech_manager.get_capabilities())
        
        @self.app.route('/api/status')
        def get_status():
            models = self.chat_interface.list_models()
            return jsonify({
                'models_count': len(models),
                'current_model': self.chat_interface.model_loader.current_model_name,
                'download_dir': str(self.config.get_download_directory()),
                'speech_capabilities': self.speech_manager.get_capabilities()
            })
        
        @self.app.route('/api/logs')
        def get_logs():
            logs = self.notification_manager.get_log_tail(50)
            return jsonify({'logs': logs})
    
    def run(self, debug: bool = False):
        """Run the web UI server.
        
        Args:
            debug: Enable debug mode
        """
        logger.info(f"Starting web UI on http://{self.host}:{self.port}")
        self.app.run(host=self.host, port=self.port, debug=debug)


def main():
    """Main function for running web UI."""
    from .config import Config
    
    config = Config()
    ui = WebUI(config)
    
    print(f"🌐 Starting Model Downloader Web UI...")
    print(f"📱 Open your browser to: http://{ui.host}:{ui.port}")
    print(f"⏹️  Press Ctrl+C to stop")
    
    try:
        ui.run()
    except KeyboardInterrupt:
        print("\n👋 Web UI stopped")


if __name__ == "__main__":
    main()