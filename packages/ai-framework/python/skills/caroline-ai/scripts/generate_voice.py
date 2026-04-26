#!/usr/bin/env python3
"""
generate_voice.py - Generate a voice response using Caroline's ElevenLabs voice.

Usage:
    python3 generate_voice.py "Your text here" --output /tmp/caroline_response.mp3
    python3 generate_voice.py "Your text here"  # saves to /tmp/caroline_<timestamp>.mp3
"""

import argparse
import os
import sys
from datetime import datetime
from pathlib import Path

CAROLINE_VOICE_ID = "wvVfSeWpAEhEqciDp1gK"
CAROLINE_MODEL = "eleven_turbo_v2_5"  # Low-latency model for real-time use


def generate_voice(text: str, output_path: str, voice_id: str = CAROLINE_VOICE_ID) -> bool:
    """Generate speech using ElevenLabs and save to output_path."""
    try:
        from elevenlabs import ElevenLabs
    except ImportError:
        print("[!] ElevenLabs SDK not installed. Installing ...")
        os.system("sudo pip3 install elevenlabs -q")
        from elevenlabs import ElevenLabs
    
    api_key = os.environ.get("ELEVENLABS_API_KEY")
    if not api_key:
        print("[✗] ELEVENLABS_API_KEY environment variable not set.")
        return False
    
    client = ElevenLabs(api_key=api_key)
    
    print(f"[→] Generating voice for: {text[:80]}...")
    
    try:
        audio_generator = client.text_to_speech.convert(
            voice_id=voice_id,
            text=text,
            model_id=CAROLINE_MODEL,
            output_format="mp3_44100_128"
        )
        
        # Collect all audio chunks
        audio_bytes = b"".join(audio_generator)
        
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        
        with open(output_file, "wb") as f:
            f.write(audio_bytes)
        
        size_kb = len(audio_bytes) / 1024
        print(f"[✓] Voice generated: {output_file} ({size_kb:.1f} KB)")
        return True
        
    except Exception as e:
        print(f"[✗] Voice generation failed: {e}")
        return False


def main():
    parser = argparse.ArgumentParser(
        description="Generate a voice response using Caroline's ElevenLabs voice."
    )
    parser.add_argument("text", help="Text to convert to speech")
    parser.add_argument(
        "--output",
        default=None,
        help="Output file path (default: /tmp/caroline_<timestamp>.mp3)"
    )
    parser.add_argument(
        "--voice-id",
        default=CAROLINE_VOICE_ID,
        help=f"ElevenLabs voice ID (default: {CAROLINE_VOICE_ID})"
    )
    
    args = parser.parse_args()
    
    if not args.output:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        args.output = f"/tmp/caroline_{timestamp}.mp3"
    
    success = generate_voice(args.text, args.output, args.voice_id)
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
