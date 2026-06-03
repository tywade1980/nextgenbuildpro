"""
LLM Chat Interface for interacting with downloaded models.
Supports text-based and voice-based interaction.
"""

import logging
import os
import sys
from pathlib import Path
from typing import Optional, Dict, Any, List, Generator
import json

logger = logging.getLogger(__name__)

# Try to import transformers with graceful fallback
try:
    from transformers import AutoTokenizer, AutoModelForCausalLM, pipeline
    import torch
    TRANSFORMERS_AVAILABLE = True
except ImportError:
    TRANSFORMERS_AVAILABLE = False
    logger.warning("transformers not available. LLM chat will not work.")

from .speech import SpeechManager
from .config import Config


class ModelLoader:
    """Loads and manages downloaded LLM models."""
    
    def __init__(self, model_dir: Path):
        """Initialize model loader.
        
        Args:
            model_dir: Directory containing downloaded models
        """
        self.model_dir = Path(model_dir)
        self.loaded_model = None
        self.loaded_tokenizer = None
        self.current_model_name = None
        
    def list_available_models(self) -> List[str]:
        """List all downloaded models.
        
        Returns:
            list: List of model names
        """
        if not self.model_dir.exists():
            return []
        
        models = []
        for model_path in self.model_dir.iterdir():
            if model_path.is_dir() and (model_path / '.download_complete').exists():
                models.append(model_path.name)
        
        return models
    
    def load_model(self, model_name: str) -> bool:
        """Load a specific model for inference.
        
        Args:
            model_name: Name of the model to load
            
        Returns:
            bool: True if successful, False otherwise
        """
        if not TRANSFORMERS_AVAILABLE:
            logger.error("transformers library not available")
            return False
        
        model_path = self.model_dir / model_name
        if not model_path.exists():
            logger.error(f"Model {model_name} not found at {model_path}")
            return False
        
        try:
            logger.info(f"Loading model: {model_name}")
            
            # Load tokenizer
            self.loaded_tokenizer = AutoTokenizer.from_pretrained(
                str(model_path),
                local_files_only=True
            )
            
            # Add padding token if it doesn't exist
            if self.loaded_tokenizer.pad_token is None:
                self.loaded_tokenizer.pad_token = self.loaded_tokenizer.eos_token
            
            # Load model
            self.loaded_model = AutoModelForCausalLM.from_pretrained(
                str(model_path),
                local_files_only=True,
                torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32,
                device_map="auto" if torch.cuda.is_available() else None
            )
            
            self.current_model_name = model_name
            logger.info(f"Model {model_name} loaded successfully")
            return True
            
        except Exception as e:
            logger.error(f"Error loading model {model_name}: {e}")
            self.loaded_model = None
            self.loaded_tokenizer = None
            self.current_model_name = None
            return False
    
    def is_model_loaded(self) -> bool:
        """Check if a model is currently loaded."""
        return self.loaded_model is not None and self.loaded_tokenizer is not None
    
    def unload_model(self):
        """Unload the current model to free memory."""
        self.loaded_model = None
        self.loaded_tokenizer = None
        self.current_model_name = None
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        logger.info("Model unloaded")


class ChatInterface:
    """Main chat interface for interacting with LLM models."""
    
    def __init__(self, config: Config):
        """Initialize chat interface.
        
        Args:
            config: Configuration instance
        """
        self.config = config
        self.model_loader = ModelLoader(Path(config.get_download_directory()))
        self.speech_manager = SpeechManager(config.config)
        self.conversation_history: List[Dict[str, str]] = []
        
    def generate_response(self, prompt: str, max_length: int = 512) -> str:
        """Generate a response to the given prompt.
        
        Args:
            prompt: Input text prompt
            max_length: Maximum length of generated response
            
        Returns:
            str: Generated response
        """
        if not self.model_loader.is_model_loaded():
            return "No model is currently loaded. Please load a model first."
        
        try:
            # Encode the prompt
            inputs = self.model_loader.loaded_tokenizer.encode(
                prompt, 
                return_tensors="pt",
                truncation=True,
                max_length=1024
            )
            
            # Generate response
            with torch.no_grad():
                outputs = self.model_loader.loaded_model.generate(
                    inputs,
                    max_length=len(inputs[0]) + max_length,
                    num_return_sequences=1,
                    temperature=0.7,
                    do_sample=True,
                    pad_token_id=self.model_loader.loaded_tokenizer.pad_token_id,
                    eos_token_id=self.model_loader.loaded_tokenizer.eos_token_id
                )
            
            # Decode the response
            response = self.model_loader.loaded_tokenizer.decode(
                outputs[0][len(inputs[0]):], 
                skip_special_tokens=True
            )
            
            return response.strip()
            
        except Exception as e:
            logger.error(f"Error generating response: {e}")
            return f"Error generating response: {str(e)}"
    
    def chat_text_mode(self, model_name: str):
        """Interactive text-based chat mode.
        
        Args:
            model_name: Name of the model to use for chat
        """
        print(f"🤖 Loading model: {model_name}")
        
        if not self.model_loader.load_model(model_name):
            print("❌ Failed to load model")
            return
        
        print(f"✅ Model loaded successfully!")
        print("💬 Starting text chat mode. Type 'quit' to exit.")
        print("-" * 50)
        
        try:
            while True:
                user_input = input("\n🧑 You: ").strip()
                
                if user_input.lower() in ['quit', 'exit', 'bye']:
                    break
                
                if not user_input:
                    continue
                
                print("🤖 AI: ", end="", flush=True)
                response = self.generate_response(user_input)
                print(response)
                
                # Store conversation
                self.conversation_history.append({
                    "user": user_input,
                    "assistant": response
                })
        
        except KeyboardInterrupt:
            print("\n\n👋 Chat ended by user")
        
        finally:
            self.model_loader.unload_model()
            print("🔄 Model unloaded")
    
    def chat_voice_mode(self, model_name: str):
        """Interactive voice-based chat mode.
        
        Args:
            model_name: Name of the model to use for chat
        """
        if not self.speech_manager.is_stt_available() or not self.speech_manager.is_tts_available():
            print("❌ Voice mode requires both TTS and STT capabilities")
            print("Please install: pip install speechrecognition pyttsx3")
            return
        
        print(f"🤖 Loading model: {model_name}")
        
        if not self.model_loader.load_model(model_name):
            print("❌ Failed to load model")
            return
        
        print(f"✅ Model loaded successfully!")
        print("🎤 Starting voice chat mode. Say 'quit' to exit.")
        self.speech_manager.speak("Voice chat mode started. How can I help you?")
        
        try:
            while True:
                print("\n🎤 Listening... (speak now)")
                user_input = self.speech_manager.listen(timeout=10.0)
                
                if not user_input:
                    print("😴 No speech detected")
                    continue
                
                print(f"🧑 You said: {user_input}")
                
                if user_input.lower() in ['quit', 'exit', 'goodbye', 'bye']:
                    self.speech_manager.speak("Goodbye!")
                    break
                
                print("🤖 AI is thinking...")
                response = self.generate_response(user_input)
                print(f"🤖 AI: {response}")
                
                # Speak the response
                self.speech_manager.speak(response)
                
                # Store conversation
                self.conversation_history.append({
                    "user": user_input,
                    "assistant": response
                })
        
        except KeyboardInterrupt:
            print("\n\n👋 Chat ended by user")
            self.speech_manager.speak("Chat ended. Goodbye!")
        
        finally:
            self.model_loader.unload_model()
            print("🔄 Model unloaded")
    
    def save_conversation(self, filepath: str):
        """Save conversation history to file.
        
        Args:
            filepath: Path to save the conversation
        """
        try:
            with open(filepath, 'w') as f:
                json.dump(self.conversation_history, f, indent=2)
            logger.info(f"Conversation saved to {filepath}")
        except Exception as e:
            logger.error(f"Error saving conversation: {e}")
    
    def list_models(self) -> List[str]:
        """List available models for chat."""
        return self.model_loader.list_available_models()
    
    def get_speech_capabilities(self) -> Dict[str, bool]:
        """Get speech capabilities."""
        return self.speech_manager.get_capabilities()


def main():
    """Main function for testing chat interface."""
    from .config import Config
    
    config = Config()
    chat = ChatInterface(config)
    
    models = chat.list_models()
    if not models:
        print("❌ No models found. Please download a model first.")
        return
    
    print("📦 Available models:")
    for i, model in enumerate(models, 1):
        print(f"  {i}. {model}")
    
    try:
        choice = int(input("\nSelect a model (number): ")) - 1
        model_name = models[choice]
        
        mode = input("\nChoose mode (text/voice): ").strip().lower()
        
        if mode == 'voice':
            chat.chat_voice_mode(model_name)
        else:
            chat.chat_text_mode(model_name)
            
    except (ValueError, IndexError):
        print("❌ Invalid selection")
    except KeyboardInterrupt:
        print("\n👋 Goodbye!")


if __name__ == "__main__":
    main()