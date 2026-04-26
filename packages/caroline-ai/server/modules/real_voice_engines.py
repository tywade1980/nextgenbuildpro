try:
    from flask import Blueprint, request, jsonify
    import requests
    import base64
    import os
    from datetime import datetime
    
    # Real voice engine integrations
    voice_engines_bp = Blueprint('voice_engines', __name__)
    FLASK_AVAILABLE = True
except ImportError:
    voice_engines_bp = None
    FLASK_AVAILABLE = False
    try:
        import requests
        import base64
    except ImportError:
        requests = None
        base64 = None
    import os
    from datetime import datetime

class RealVoiceEngines:
    def __init__(self):
        # API keys would be set via environment variables
        self.groq_api_key = os.getenv('GROQ_API_KEY', '')
        self.elevenlabs_api_key = os.getenv('ELEVENLABS_API_KEY', '')
        
        # Groq TTS endpoint
        self.groq_tts_url = "https://api.groq.com/openai/v1/audio/speech"
        
        # ElevenLabs TTS endpoint  
        self.elevenlabs_url = "https://api.elevenlabs.io/v1/text-to-speech"
        
        # Voice mappings
        self.groq_voices = {
            "Celeste-PlayAI": {"model": "tts-1", "voice": "nova", "quality": "premium"},
            "Arista-PlayAI": {"model": "tts-1", "voice": "alloy", "quality": "professional"},
            "Cheyenne-PlayAI": {"model": "tts-1", "voice": "echo", "quality": "energetic"},
            "Deedee-PlayAI": {"model": "tts-1", "voice": "fable", "quality": "bubbly"},
            "Gail-PlayAI": {"model": "tts-1", "voice": "onyx", "quality": "mature"}
        }
        
        self.elevenlabs_voices = {
            "rachel": "21m00Tcm4TlvDq8ikWAM",
            "domi": "AZnzlk1XvdvUeBnXmlld", 
            "bella": "EXAVITQu4vr4xnSDxMaL",
            "antoni": "ErXwobaYiN019PkySvjV",
            "elli": "MF3mGyEYCl7XYWbV9V6O"
        }

    def generate_groq_speech(self, text, voice_settings):
        """Generate speech using Groq's neural TTS"""
        try:
            voice_id = voice_settings.get('voice', 'Celeste-PlayAI')
            voice_config = self.groq_voices.get(voice_id, self.groq_voices['Celeste-PlayAI'])
            
            headers = {
                "Authorization": f"Bearer {self.groq_api_key}",
                "Content-Type": "application/json"
            }
            
            # Adjust text for emotion
            emotion = voice_settings.get('emotion', 'warm')
            processed_text = self.add_emotion_to_text(text, emotion)
            
            data = {
                "model": voice_config["model"],
                "input": processed_text,
                "voice": voice_config["voice"],
                "response_format": "mp3",
                "speed": voice_settings.get('speed', 1.0)
            }
            
            response = requests.post(self.groq_tts_url, headers=headers, json=data)
            
            if response.status_code == 200:
                audio_data = base64.b64encode(response.content).decode('utf-8')
                return {
                    "success": True,
                    "audio_data": audio_data,
                    "format": "mp3",
                    "voice_used": voice_id,
                    "quality": "neural_premium"
                }
            else:
                return {"success": False, "error": f"Groq API error: {response.status_code}"}
                
        except Exception as e:
            return {"success": False, "error": f"Groq integration error: {str(e)}"}

    def generate_elevenlabs_speech(self, text, voice_settings):
        """Generate speech using ElevenLabs ultra-realistic TTS"""
        try:
            voice_id = voice_settings.get('voice', 'rachel')
            elevenlabs_voice_id = self.elevenlabs_voices.get(voice_id, self.elevenlabs_voices['rachel'])
            
            url = f"{self.elevenlabs_url}/{elevenlabs_voice_id}"
            
            headers = {
                "Accept": "audio/mpeg",
                "Content-Type": "application/json",
                "xi-api-key": self.elevenlabs_api_key
            }
            
            # ElevenLabs emotion and stability settings
            emotion = voice_settings.get('emotion', 'warm')
            stability, similarity = self.get_elevenlabs_emotion_settings(emotion)
            
            data = {
                "text": text,
                "model_id": "eleven_monolingual_v1",
                "voice_settings": {
                    "stability": stability,
                    "similarity_boost": similarity,
                    "style": 0.5,
                    "use_speaker_boost": True
                }
            }
            
            response = requests.post(url, json=data, headers=headers)
            
            if response.status_code == 200:
                audio_data = base64.b64encode(response.content).decode('utf-8')
                return {
                    "success": True,
                    "audio_data": audio_data,
                    "format": "mp3",
                    "voice_used": voice_id,
                    "quality": "ultra_realistic"
                }
            else:
                return {"success": False, "error": f"ElevenLabs API error: {response.status_code}"}
                
        except Exception as e:
            return {"success": False, "error": f"ElevenLabs integration error: {str(e)}"}

    def add_emotion_to_text(self, text, emotion):
        """Add emotional markers to text for better TTS"""
        if emotion == "excited":
            return f"*excited* {text}"
        elif emotion == "calm":
            return f"*speaking calmly* {text}"
        elif emotion == "warm":
            return f"*warmly* {text}"
        elif emotion == "playful":
            return f"*playfully* {text}"
        return text

    def get_elevenlabs_emotion_settings(self, emotion):
        """Get ElevenLabs voice settings for different emotions"""
        emotion_settings = {
            "warm": (0.7, 0.8),
            "excited": (0.3, 0.9),
            "calm": (0.9, 0.7),
            "playful": (0.4, 0.8),
            "neutral": (0.6, 0.75)
        }
        return emotion_settings.get(emotion, (0.7, 0.8))

# Initialize voice engines
real_voice_engines = RealVoiceEngines()

# Flask routes (only if Flask is available)
if FLASK_AVAILABLE and voice_engines_bp:
    @voice_engines_bp.route('/groq/speak', methods=['POST'])
    def groq_text_to_speech():
        """Generate natural speech using Groq's neural TTS"""
        try:
            data = request.get_json()
            text = data.get('text', '')
            voice_settings = data.get('voice_settings', {})
            
            if not text:
                return jsonify({"error": "No text provided"}), 400
            
            # Generate speech with Groq
            result = real_voice_engines.generate_groq_speech(text, voice_settings)
            
            if result["success"]:
                return jsonify({
                    "audio_data": result["audio_data"],
                    "format": result["format"],
                    "voice_engine": "groq_neural",
                    "voice_used": result["voice_used"],
                    "quality": result["quality"],
                    "caroline_message": "Speaking with premium neural voice synthesis!"
                })
            else:
                # Fallback response
                return jsonify({
                    "error": result["error"],
                    "fallback_needed": True,
                    "caroline_message": "Neural voice temporarily unavailable, using fallback."
                }), 503
                
        except Exception as e:
            return jsonify({
                "error": f"Groq TTS error: {str(e)}",
                "fallback_needed": True
            }), 500

    @voice_engines_bp.route('/elevenlabs/speak', methods=['POST'])
    def elevenlabs_text_to_speech():
        """Generate ultra-realistic speech using ElevenLabs"""
        try:
            data = request.get_json()
            text = data.get('text', '')
            voice_settings = data.get('voice_settings', {})
            
            if not text:
                return jsonify({"error": "No text provided"}), 400
            
            # Generate speech with ElevenLabs
            result = real_voice_engines.generate_elevenlabs_speech(text, voice_settings)
            
            if result["success"]:
                return jsonify({
                    "audio_data": result["audio_data"],
                    "format": result["format"],
                    "voice_engine": "elevenlabs_ultra",
                    "voice_used": result["voice_used"],
                    "quality": result["quality"],
                    "caroline_message": "Speaking with ultra-realistic voice synthesis!"
                })
            else:
                return jsonify({
                    "error": result["error"],
                    "fallback_needed": True,
                    "caroline_message": "Ultra voice temporarily unavailable, using fallback."
                }), 503
                
        except Exception as e:
            return jsonify({
                "error": f"ElevenLabs TTS error: {str(e)}",
                "fallback_needed": True
            }), 500

    @voice_engines_bp.route('/voices/available', methods=['GET'])
    def get_available_voices():
        """Get all available voices across all engines"""
        return jsonify({
            "groq_neural_voices": [
                {"id": "Celeste-PlayAI", "name": "Celeste", "style": "Natural & Warm", "engine": "groq", "recommended": True},
                {"id": "Arista-PlayAI", "name": "Arista", "style": "Professional", "engine": "groq"},
                {"id": "Cheyenne-PlayAI", "name": "Cheyenne", "style": "Energetic", "engine": "groq"},
                {"id": "Deedee-PlayAI", "name": "Deedee", "style": "Bubbly & Fun", "engine": "groq"},
                {"id": "Gail-PlayAI", "name": "Gail", "style": "Mature & Wise", "engine": "groq"}
            ],
            "elevenlabs_ultra_voices": [
                {"id": "rachel", "name": "Rachel", "style": "Calm & Professional", "engine": "elevenlabs"},
                {"id": "domi", "name": "Domi", "style": "Strong & Confident", "engine": "elevenlabs"},
                {"id": "bella", "name": "Bella", "style": "Soft & Gentle", "engine": "elevenlabs"},
                {"id": "antoni", "name": "Antoni", "style": "Warm & Friendly", "engine": "elevenlabs"},
                {"id": "elli", "name": "Elli", "style": "Emotional & Expressive", "engine": "elevenlabs"}
            ],
            "voice_engines_status": {
                "groq_neural": "Premium neural synthesis",
                "elevenlabs_ultra": "Ultra-realistic voices", 
                "browser_fallback": "Basic synthesis backup"
            }
        })

    @voice_engines_bp.route('/interrupt', methods=['POST'])
    def interrupt_speech():
        """Interrupt Caroline's current speech"""
        return jsonify({
            "interrupted": True,
            "caroline_message": "I stopped talking - what did you want to say?",
            "timestamp": datetime.now().isoformat()
        })

