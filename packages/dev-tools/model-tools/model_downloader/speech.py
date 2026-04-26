"""
Speech processing components for TTS (Text-to-Speech) and STT (Speech-to-Text).
Provides interfaces for voice interaction with downloaded LLM models.
"""

import logging
import os
import tempfile
from typing import Optional, Dict, Any
from pathlib import Path

logger = logging.getLogger(__name__)

# Try to import speech libraries with graceful fallback
try:
    import speech_recognition as sr
    STT_AVAILABLE = True
except ImportError:
    STT_AVAILABLE = False
    logger.warning("speech_recognition not available. STT will not work.")

try:
    import pyttsx3
    TTS_AVAILABLE = True
except ImportError:
    TTS_AVAILABLE = False
    logger.warning("pyttsx3 not available. TTS will not work.")


class TextToSpeech:
    """Text-to-Speech engine wrapper."""
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        """Initialize TTS engine.
        
        Args:
            config: Configuration dictionary for TTS settings
        """
        self.config = config or {}
        self.engine = None
        
        if TTS_AVAILABLE:
            try:
                self.engine = pyttsx3.init()
                self._configure_engine()
                logger.info("TTS engine initialized successfully")
            except Exception as e:
                logger.error(f"Failed to initialize TTS engine: {e}")
                self.engine = None
        
    def _configure_engine(self):
        """Configure the TTS engine with user preferences."""
        if not self.engine:
            return
            
        # Set speech rate
        rate = self.config.get('tts_rate', 200)
        self.engine.setProperty('rate', rate)
        
        # Set volume
        volume = self.config.get('tts_volume', 0.8)
        self.engine.setProperty('volume', volume)
        
        # Set voice (if specified)
        voice_id = self.config.get('tts_voice_id')
        if voice_id:
            voices = self.engine.getProperty('voices')
            for voice in voices:
                if voice_id in voice.id:
                    self.engine.setProperty('voice', voice.id)
                    break
    
    def speak(self, text: str) -> bool:
        """Convert text to speech and play it.
        
        Args:
            text: Text to convert to speech
            
        Returns:
            bool: True if successful, False otherwise
        """
        if not self.engine:
            logger.error("TTS engine not available")
            return False
            
        try:
            logger.info(f"Speaking: {text[:50]}...")
            self.engine.say(text)
            self.engine.runAndWait()
            return True
        except Exception as e:
            logger.error(f"Error speaking text: {e}")
            return False
    
    def save_to_file(self, text: str, filepath: str) -> bool:
        """Convert text to speech and save to audio file.
        
        Args:
            text: Text to convert to speech
            filepath: Path where to save the audio file
            
        Returns:
            bool: True if successful, False otherwise
        """
        if not self.engine:
            logger.error("TTS engine not available")
            return False
            
        try:
            logger.info(f"Saving speech to file: {filepath}")
            self.engine.save_to_file(text, filepath)
            self.engine.runAndWait()
            return True
        except Exception as e:
            logger.error(f"Error saving speech to file: {e}")
            return False
    
    def get_available_voices(self) -> list:
        """Get list of available voices.
        
        Returns:
            list: List of available voice information
        """
        if not self.engine:
            return []
            
        try:
            voices = self.engine.getProperty('voices')
            return [{'id': voice.id, 'name': voice.name, 'languages': voice.languages} 
                   for voice in voices]
        except Exception as e:
            logger.error(f"Error getting voices: {e}")
            return []


class SpeechToText:
    """Speech-to-Text engine wrapper."""
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        """Initialize STT engine.
        
        Args:
            config: Configuration dictionary for STT settings
        """
        self.config = config or {}
        self.recognizer = None
        self.microphone = None
        
        if STT_AVAILABLE:
            try:
                self.recognizer = sr.Recognizer()
                self.microphone = sr.Microphone()
                
                # Adjust for ambient noise
                with self.microphone as source:
                    logger.info("Adjusting for ambient noise...")
                    self.recognizer.adjust_for_ambient_noise(source)
                
                logger.info("STT engine initialized successfully")
            except Exception as e:
                logger.error(f"Failed to initialize STT engine: {e}")
                self.recognizer = None
                self.microphone = None
    
    def listen_once(self, timeout: float = 5.0, phrase_timeout: float = 1.0) -> Optional[str]:
        """Listen for speech and convert to text.
        
        Args:
            timeout: Maximum time to wait for speech to start
            phrase_timeout: Maximum time to wait for phrase to complete
            
        Returns:
            str: Recognized text, or None if recognition failed
        """
        if not self.recognizer or not self.microphone:
            logger.error("STT engine not available")
            return None
            
        try:
            logger.info("Listening for speech...")
            with self.microphone as source:
                # Listen for audio
                audio = self.recognizer.listen(
                    source, 
                    timeout=timeout, 
                    phrase_time_limit=phrase_timeout
                )
            
            logger.info("Processing speech...")
            
            # Use Google Speech Recognition (free tier)
            text = self.recognizer.recognize_google(audio)
            logger.info(f"Recognized: {text}")
            return text
            
        except sr.WaitTimeoutError:
            logger.warning("No speech detected within timeout")
            return None
        except sr.UnknownValueError:
            logger.warning("Could not understand audio")
            return None
        except sr.RequestError as e:
            logger.error(f"Error with speech recognition service: {e}")
            return None
        except Exception as e:
            logger.error(f"Unexpected error in speech recognition: {e}")
            return None
    
    def recognize_from_file(self, audio_file: str) -> Optional[str]:
        """Recognize speech from an audio file.
        
        Args:
            audio_file: Path to audio file
            
        Returns:
            str: Recognized text, or None if recognition failed
        """
        if not self.recognizer:
            logger.error("STT engine not available")
            return None
            
        try:
            with sr.AudioFile(audio_file) as source:
                audio = self.recognizer.record(source)
            
            text = self.recognizer.recognize_google(audio)
            logger.info(f"Recognized from file: {text}")
            return text
            
        except Exception as e:
            logger.error(f"Error recognizing speech from file: {e}")
            return None


class SpeechManager:
    """Unified interface for speech processing."""
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        """Initialize speech manager.
        
        Args:
            config: Configuration dictionary
        """
        self.config = config or {}
        self.tts = TextToSpeech(config)
        self.stt = SpeechToText(config)
        
    def is_tts_available(self) -> bool:
        """Check if TTS is available."""
        return TTS_AVAILABLE and self.tts.engine is not None
    
    def is_stt_available(self) -> bool:
        """Check if STT is available."""
        return STT_AVAILABLE and self.stt.recognizer is not None
    
    def speak(self, text: str) -> bool:
        """Speak the given text."""
        return self.tts.speak(text)
    
    def listen(self, timeout: float = 5.0) -> Optional[str]:
        """Listen for speech and return recognized text."""
        return self.stt.listen_once(timeout=timeout)
    
    def conversation_mode(self, prompt_text: str = "Listening...") -> Optional[str]:
        """Interactive conversation mode with voice prompts.
        
        Args:
            prompt_text: Text to speak before listening
            
        Returns:
            str: Recognized speech, or None if failed
        """
        if self.is_tts_available():
            self.speak(prompt_text)
        else:
            print(prompt_text)
        
        return self.listen()
    
    def get_capabilities(self) -> Dict[str, bool]:
        """Get capabilities of the speech system.
        
        Returns:
            dict: Dictionary of available capabilities
        """
        return {
            'tts_available': self.is_tts_available(),
            'stt_available': self.is_stt_available(),
            'voices_available': len(self.tts.get_available_voices()) > 0 if self.is_tts_available() else False
        }