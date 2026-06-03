"""
Model Download Script - Run at container startup or build time
Downloads: Fish Speech 1.5, XTTS v2, StyleTTS2, Mistral 7B
"""

import os
import torch
from huggingface_hub import snapshot_download, hf_hub_download

MODELS_DIR = "/app/models"
os.makedirs(MODELS_DIR, exist_ok=True)

def download_fish_speech():
    """Download Fish Speech 1.5 model"""
    print("Downloading Fish Speech 1.5...")
    try:
        snapshot_download(
            repo_id="fishaudio/fish-speech-1.5",
            local_dir=f"{MODELS_DIR}/fish-speech-1.5",
            local_dir_use_symlinks=False
        )
        print("Fish Speech 1.5 downloaded successfully")
    except Exception as e:
        print(f"Fish Speech download error: {e}")

def download_xtts():
    """Download XTTS v2 model - handled by coqui-tts on first use"""
    print("XTTS v2 will be downloaded on first use by coqui-tts library")
    # Pre-warm by importing
    try:
        from TTS.api import TTS
        # This triggers model download
        tts = TTS("tts_models/multilingual/multi-dataset/xtts_v2")
        print("XTTS v2 downloaded successfully")
    except Exception as e:
        print(f"XTTS v2 download error: {e}")

def download_styletts2():
    """Download StyleTTS2 model - handled by styletts2 package on first use"""
    print("StyleTTS2 will be downloaded on first use by styletts2 library")
    try:
        from styletts2 import tts
        # This triggers model download
        my_tts = tts.StyleTTS2()
        print("StyleTTS2 downloaded successfully")
    except Exception as e:
        print(f"StyleTTS2 download error: {e}")

def download_mistral():
    """Download Mistral 7B Instruct model"""
    print("Downloading Mistral 7B Instruct...")
    try:
        snapshot_download(
            repo_id="mistralai/Mistral-7B-Instruct-v0.3",
            local_dir=f"{MODELS_DIR}/mistral-7b-instruct",
            local_dir_use_symlinks=False,
            ignore_patterns=["*.bin"]  # Use safetensors
        )
        print("Mistral 7B downloaded successfully")
    except Exception as e:
        print(f"Mistral download error: {e}")

if __name__ == "__main__":
    print("=" * 50)
    print("ARIA Model Downloader")
    print("=" * 50)
    
    download_fish_speech()
    download_xtts()
    download_styletts2()
    download_mistral()
    
    print("=" * 50)
    print("All models downloaded!")
    print("=" * 50)
