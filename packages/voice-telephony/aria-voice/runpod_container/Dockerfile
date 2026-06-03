# ARIA Voice Agent - Runpod Container
# Complete standalone: Fish Speech 1.5, XTTS v2, StyleTTS2, Llama/Mistral Uncensored

FROM nvidia/cuda:12.1.1-cudnn8-devel-ubuntu22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV PYTHONUNBUFFERED=1
ENV CUDA_HOME=/usr/local/cuda
ENV PATH="${CUDA_HOME}/bin:${PATH}"
ENV LD_LIBRARY_PATH="${CUDA_HOME}/lib64:${LD_LIBRARY_PATH}"
ENV MODELS_DIR=/app/models
ENV DATA_DIR=/app/data

# System dependencies
RUN apt-get update && apt-get install -y \
    python3.10 \
    python3.10-venv \
    python3-pip \
    git \
    git-lfs \
    wget \
    curl \
    ffmpeg \
    libsndfile1 \
    libportaudio2 \
    portaudio19-dev \
    espeak-ng \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Set Python 3.10 as default
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.10 1 \
    && update-alternatives --install /usr/bin/pip pip /usr/bin/pip3 1

WORKDIR /app

# Upgrade pip
RUN pip install --upgrade pip setuptools wheel

# Install PyTorch with CUDA
RUN pip install torch==2.1.2 torchaudio==2.1.2 --index-url https://download.pytorch.org/whl/cu121

# Install TTS engines
RUN pip install coqui-tts styletts2 fish-audio-sdk

# Install LLM dependencies (vLLM for fast inference)
RUN pip install vllm transformers accelerate bitsandbytes

# Install Whisper for STT
RUN pip install openai-whisper

# Install API framework and utilities
RUN pip install fastapi uvicorn python-multipart aiofiles pydantic
RUN pip install soundfile librosa numpy scipy requests huggingface_hub

# Create directories
RUN mkdir -p /app/models /app/data/voices

# Copy server
COPY server.py /app/server.py
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Default settings
RUN echo '{"persona_name": "ARIA", "llm_model": "mistral-uncensored", "tts_engine": "xtts", "temperature": 0.8}' > /app/data/settings.json

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=30s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

CMD ["/app/start.sh"]
