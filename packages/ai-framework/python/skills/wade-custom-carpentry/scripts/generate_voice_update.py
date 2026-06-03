import os
import sys
from openai import OpenAI

def generate_tts(text, output_path):
    """
    Generates an audio file from text using OpenAI's TTS model.
    """
    client = OpenAI()
    
    try:
        response = client.audio.speech.create(
            model="tts-1",
            voice="alloy", # Professional and clear voice
            input=text
        )
        
        response.stream_to_file(output_path)
        print(f"Successfully generated voice update at: {output_path}")
        return True
    except Exception as e:
        print(f"Error generating TTS: {e}")
        return False

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 generate_voice_update.py '<text>' [output_path]")
        sys.exit(1)
        
    text_to_speak = sys.argv[1]
    output_file = sys.argv[2] if len(sys.argv) > 2 else "/home/ubuntu/voice_update.mp3"
    
    generate_tts(text_to_speak, output_file)
