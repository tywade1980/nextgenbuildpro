#!/bin/bash
# ARIA Voice Agent - Startup Script

echo "========================================"
echo "  ARIA Voice Agent v2.0"
echo "  Complete Standalone API"
echo "========================================"

export MODELS_DIR=${MODELS_DIR:-/app/models}
export DATA_DIR=${DATA_DIR:-/app/data}
export PYTHONUNBUFFERED=1

# Create directories
mkdir -p $MODELS_DIR $DATA_DIR/voices

# Show GPU info
echo ""
echo "Hardware:"
python -c "
import torch
if torch.cuda.is_available():
    print(f'  GPU: {torch.cuda.get_device_name(0)}')
    print(f'  VRAM: {torch.cuda.get_device_properties(0).total_memory / 1e9:.1f} GB')
else:
    print('  GPU: None (CPU mode)')
"

# Show settings
echo ""
echo "Configuration:"
if [ -f "$DATA_DIR/settings.json" ]; then
    python -c "
import json
with open('$DATA_DIR/settings.json') as f:
    s = json.load(f)
    print(f'  Persona: {s.get(\"persona_name\", \"ARIA\")}')
    print(f'  LLM: {s.get(\"llm_model\", \"mistral-uncensored\")}')
    print(f'  TTS: {s.get(\"tts_engine\", \"xtts\")}')
"
fi

echo ""
echo "Starting API server on port 8000..."
echo "========================================"
echo ""

# Start server
exec python /app/server.py
