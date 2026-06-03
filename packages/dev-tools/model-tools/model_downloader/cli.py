import click
import os
import sys
import signal
import time
import threading
from pathlib import Path
from typing import Optional

from .config import Config
from .downloader import ModelDownloader
from .scheduler import AutoScheduler
from .notifications import NotificationManager
from .speech import SpeechManager
from .chat import ChatInterface
from .web_ui import WebUI

# Global variables for signal handling
scheduler_instance = None
notification_manager_instance = None

def signal_handler(signum, frame):
    """Handle shutdown signals gracefully."""
    global scheduler_instance, notification_manager_instance
    
    print("\nReceived shutdown signal. Stopping gracefully...")
    
    if scheduler_instance:
        scheduler_instance.stop()
    
    if notification_manager_instance:
        notification_manager_instance.notify_shutdown()
    
    sys.exit(0)

@click.group()
@click.option('--config', '-c', help='Path to configuration file')
@click.option('--verbose', '-v', is_flag=True, help='Enable verbose logging')
@click.pass_context
def cli(ctx, config, verbose):
    """Automated Background LLM Model Downloader
    
    A system for autonomously downloading LLM models with minimal human involvement
    while keeping users informed about the process.
    """
    ctx.ensure_object(dict)
    
    # Initialize configuration
    config_obj = Config(config)
    
    if verbose:
        config_obj.set('notifications.log_level', 'DEBUG')
    
    ctx.obj['config'] = config_obj

@cli.command()
@click.pass_context
def start(ctx):
    """Start the automated model downloader service."""
    global scheduler_instance, notification_manager_instance
    
    config = ctx.obj['config']
    
    try:
        # Initialize components
        notification_manager = NotificationManager(config)
        notification_manager_instance = notification_manager
        
        downloader = ModelDownloader(config)
        scheduler = AutoScheduler(config, downloader, notification_manager)
        scheduler_instance = scheduler
        
        # Setup signal handlers
        signal.signal(signal.SIGINT, signal_handler)
        signal.signal(signal.SIGTERM, signal_handler)
        
        # Send startup notification
        notification_manager.notify_startup()
        
        # Start the scheduler
        scheduler.start()
        
        click.echo("Model downloader service started.")
        click.echo("Press Ctrl+C to stop the service.")
        
        # Keep the main thread alive
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            signal_handler(signal.SIGINT, None)
            
    except Exception as e:
        click.echo(f"Error starting service: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--model', '-m', required=True, help='Model name to download')
@click.option('--source', '-s', default='huggingface', help='Model source (default: huggingface)')
@click.option('--add-to-config', is_flag=True, help='Add model to configuration for automatic downloads')
@click.pass_context
def download(ctx, model, source, add_to_config):
    """Download a specific model immediately."""
    config = ctx.obj['config']
    
    try:
        notification_manager = NotificationManager(config)
        downloader = ModelDownloader(config)
        
        if add_to_config:
            config.add_model(model, source)
            click.echo(f"Added {model} to configuration for automatic downloads.")
        
        click.echo(f"Downloading {model} from {source}...")
        
        success = downloader.download_model(model, source)
        
        if success:
            click.echo(f"✓ Successfully downloaded {model}")
        else:
            click.echo(f"✗ Failed to download {model}", err=True)
            sys.exit(1)
            
    except Exception as e:
        click.echo(f"Error downloading model: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.pass_context
def list_models(ctx):
    """List configured models and their status."""
    config = ctx.obj['config']
    
    try:
        downloader = ModelDownloader(config)
        
        # Show configured models
        click.echo("Configured Models:")
        click.echo("-" * 50)
        
        models = config.get_models()
        if not models:
            click.echo("No models configured.")
        else:
            for model in models:
                auto_status = "✓" if model.get('auto_download', True) else "✗"
                priority = model.get('priority', 1)
                source = model.get('source', 'huggingface')
                click.echo(f"  {auto_status} {model['name']} (source: {source}, priority: {priority})")
        
        click.echo()
        
        # Show downloaded models
        click.echo("Downloaded Models:")
        click.echo("-" * 50)
        
        downloaded = downloader.list_downloaded_models()
        if not downloaded:
            click.echo("No models downloaded.")
        else:
            for model in downloaded:
                status = "✓ Complete" if model['complete'] else "✗ Incomplete"
                size_mb = model['size_bytes'] / (1024 * 1024)
                date_str = model.get('downloaded_date', 'Unknown')
                click.echo(f"  {status} {model['name']} ({size_mb:.1f} MB, {date_str})")
                
    except Exception as e:
        click.echo(f"Error listing models: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--model', '-m', required=True, help='Model name to add')
@click.option('--source', '-s', default='huggingface', help='Model source (default: huggingface)')
@click.option('--priority', '-p', type=int, default=1, help='Download priority (lower = higher priority)')
@click.option('--auto/--no-auto', default=True, help='Enable automatic downloads')
@click.pass_context
def add_model(ctx, model, source, priority, auto):
    """Add a model to the configuration."""
    config = ctx.obj['config']
    
    try:
        config.add_model(model, source, auto, priority)
        auto_status = "enabled" if auto else "disabled"
        click.echo(f"Added {model} with priority {priority} (auto-download {auto_status})")
    except Exception as e:
        click.echo(f"Error adding model: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--model', '-m', required=True, help='Model name to remove')
@click.pass_context
def remove_model(ctx, model):
    """Remove a model from the configuration."""
    config = ctx.obj['config']
    
    try:
        config.remove_model(model)
        click.echo(f"Removed {model} from configuration")
    except Exception as e:
        click.echo(f"Error removing model: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.pass_context
def status(ctx):
    """Show service status and statistics."""
    config = ctx.obj['config']
    
    try:
        notification_manager = NotificationManager(config)
        downloader = ModelDownloader(config)
        
        # Show configuration status
        click.echo("Service Configuration:")
        click.echo("-" * 50)
        click.echo(f"Download Directory: {config.get_download_directory()}")
        click.echo(f"Auto Downloads: {'Enabled' if config.is_auto_download_enabled() else 'Disabled'}")
        click.echo(f"Check Interval: {config.get_check_interval_hours()} hours")
        click.echo(f"Max Daily Downloads: {config.get('schedule.max_downloads_per_day', 5)}")
        click.echo(f"Max Concurrent: {config.get('download_settings.max_concurrent_downloads', 2)}")
        
        click.echo()
        
        # Show download statistics
        stats = downloader.get_download_stats()
        click.echo("Download Statistics:")
        click.echo("-" * 50)
        click.echo(f"Total Downloads: {stats['total_downloads']}")
        click.echo(f"Successful: {stats['successful_downloads']}")
        click.echo(f"Failed: {stats['failed_downloads']}")
        click.echo(f"Bytes Downloaded: {stats['bytes_downloaded'] / (1024*1024):.1f} MB")
        
        click.echo()
        
        # Show recent notifications
        click.echo("Recent Notifications (last 5):")
        click.echo("-" * 50)
        history = notification_manager.get_notification_history(5)
        if not history:
            click.echo("No notifications.")
        else:
            for notif in history:
                timestamp = notif['timestamp'][:19]  # Remove microseconds
                level = notif['level'].upper()
                click.echo(f"  [{timestamp}] {level}: {notif['title']}")
                
    except Exception as e:
        click.echo(f"Error getting status: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.pass_context
def check_now(ctx):
    """Force an immediate model check and download."""
    config = ctx.obj['config']
    
    try:
        notification_manager = NotificationManager(config)
        downloader = ModelDownloader(config)
        scheduler = AutoScheduler(config, downloader, notification_manager)
        
        click.echo("Starting immediate model check...")
        scheduler.force_check()
        click.echo("Check initiated. Monitor logs for progress.")
        
    except Exception as e:
        click.echo(f"Error starting check: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--lines', '-n', type=int, default=50, help='Number of log lines to show')
@click.pass_context
def logs(ctx, lines):
    """Show recent log entries."""
    config = ctx.obj['config']
    
    try:
        notification_manager = NotificationManager(config)
        log_content = notification_manager.get_log_tail(lines)
        
        click.echo(f"Last {lines} lines from log file:")
        click.echo("-" * 50)
        click.echo(log_content)
        
    except Exception as e:
        click.echo(f"Error reading logs: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.pass_context
def config_show(ctx):
    """Show current configuration."""
    config = ctx.obj['config']
    
    try:
        import yaml
        
        click.echo("Current Configuration:")
        click.echo("-" * 50)
        click.echo(yaml.dump(config.config, default_flow_style=False, indent=2))
        
    except Exception as e:
        click.echo(f"Error showing configuration: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--key', '-k', required=True, help='Configuration key (supports dot notation)')
@click.option('--value', '-v', required=True, help='Configuration value')
@click.pass_context
def config_set(ctx, key, value):
    """Set a configuration value."""
    config = ctx.obj['config']
    
    try:
        # Try to parse value as appropriate type
        if value.lower() in ('true', 'false'):
            value = value.lower() == 'true'
        elif value.isdigit():
            value = int(value)
        elif '.' in value and value.replace('.', '').isdigit():
            value = float(value)
        
        config.set(key, value)
        click.echo(f"Set {key} = {value}")
        
    except Exception as e:
        click.echo(f"Error setting configuration: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--model', '-m', help='Model name to use for chat')
@click.option('--mode', type=click.Choice(['text', 'voice']), default='text', help='Chat mode')
@click.pass_context
def chat(ctx, model, mode):
    """Start interactive chat with a downloaded model."""
    config = ctx.obj['config']
    
    try:
        chat_interface = ChatInterface(config)
        models = chat_interface.list_models()
        
        if not models:
            click.echo("❌ No models found. Download a model first with 'model-downloader download'")
            return
        
        # If no model specified, let user choose
        if not model:
            click.echo("📦 Available models:")
            for i, model_name in enumerate(models, 1):
                click.echo(f"  {i}. {model_name}")
            
            try:
                choice = click.prompt("Select a model number", type=int)
                model = models[choice - 1]
            except (IndexError, ValueError):
                click.echo("❌ Invalid selection")
                return
        
        if model not in models:
            click.echo(f"❌ Model '{model}' not found. Available models: {', '.join(models)}")
            return
        
        click.echo(f"🚀 Starting {mode} chat with model: {model}")
        
        if mode == 'voice':
            capabilities = chat_interface.get_speech_capabilities()
            if not capabilities['tts_available'] or not capabilities['stt_available']:
                click.echo("❌ Voice mode requires TTS and STT capabilities")
                click.echo("Install dependencies: pip install speechrecognition pyttsx3")
                return
            chat_interface.chat_voice_mode(model)
        else:
            chat_interface.chat_text_mode(model)
            
    except Exception as e:
        click.echo(f"Error starting chat: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--text', '-t', required=True, help='Text to convert to speech')
@click.option('--save', '-s', help='Save audio to file instead of playing')
@click.pass_context
def speak(ctx, text, save):
    """Convert text to speech using TTS."""
    config = ctx.obj['config']
    
    try:
        speech_manager = SpeechManager(config.config)
        
        if not speech_manager.is_tts_available():
            click.echo("❌ TTS not available. Install with: pip install pyttsx3")
            return
        
        if save:
            success = speech_manager.tts.save_to_file(text, save)
            if success:
                click.echo(f"✅ Audio saved to: {save}")
            else:
                click.echo("❌ Failed to save audio")
        else:
            success = speech_manager.speak(text)
            if success:
                click.echo("✅ Text spoken successfully")
            else:
                click.echo("❌ Failed to speak text")
                
    except Exception as e:
        click.echo(f"Error with TTS: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--timeout', type=float, default=5.0, help='Listening timeout in seconds')
@click.option('--file', '-f', help='Audio file to process instead of microphone')
@click.pass_context
def listen(ctx, timeout, file):
    """Convert speech to text using STT."""
    config = ctx.obj['config']
    
    try:
        speech_manager = SpeechManager(config.config)
        
        if not speech_manager.is_stt_available():
            click.echo("❌ STT not available. Install with: pip install speechrecognition")
            return
        
        if file:
            text = speech_manager.stt.recognize_from_file(file)
        else:
            click.echo(f"🎤 Listening for {timeout} seconds...")
            text = speech_manager.listen(timeout=timeout)
        
        if text:
            click.echo(f"📝 Recognized: {text}")
        else:
            click.echo("❌ No speech recognized")
            
    except Exception as e:
        click.echo(f"Error with STT: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.pass_context
def speech_test(ctx):
    """Test speech capabilities (TTS and STT)."""
    config = ctx.obj['config']
    
    try:
        speech_manager = SpeechManager(config.config)
        capabilities = speech_manager.get_capabilities()
        
        click.echo("🎯 Speech Capabilities Test")
        click.echo("-" * 30)
        
        click.echo(f"TTS Available: {'✅' if capabilities['tts_available'] else '❌'}")
        click.echo(f"STT Available: {'✅' if capabilities['stt_available'] else '❌'}")
        click.echo(f"Voices Available: {'✅' if capabilities['voices_available'] else '❌'}")
        
        if capabilities['tts_available']:
            click.echo("\n🔊 Testing TTS...")
            speech_manager.speak("Text to speech is working correctly")
            
            # Show available voices
            voices = speech_manager.tts.get_available_voices()
            if voices:
                click.echo(f"\n🎭 Available voices ({len(voices)}):")
                for voice in voices[:5]:  # Show first 5 voices
                    click.echo(f"  - {voice.get('name', 'Unknown')}")
        
        if capabilities['stt_available']:
            if click.confirm("\n🎤 Test STT? (requires microphone)"):
                click.echo("🎤 Say something...")
                text = speech_manager.listen(timeout=5.0)
                if text:
                    click.echo(f"📝 You said: {text}")
                else:
                    click.echo("❌ No speech detected")
        
        click.echo("\n✅ Speech test completed")
        
    except Exception as e:
        click.echo(f"Error testing speech: {e}", err=True)
        sys.exit(1)

@cli.command()
@click.option('--host', default='127.0.0.1', help='Host to bind to')
@click.option('--port', default=5000, help='Port to listen on')
@click.option('--debug', is_flag=True, help='Enable debug mode')
@click.pass_context
def web_ui(ctx, host, port, debug):
    """Start the web-based user interface."""
    config = ctx.obj['config']
    
    try:
        ui = WebUI(config, host=host, port=port)
        click.echo(f"🌐 Starting web UI on http://{host}:{port}")
        click.echo("📱 Open your browser to access the interface")
        click.echo("⏹️  Press Ctrl+C to stop")
        ui.run(debug=debug)
    except ImportError as e:
        click.echo(f"❌ Web UI requires Flask: {e}", err=True)
        click.echo("Install with: pip install flask")
        sys.exit(1)
    except Exception as e:
        click.echo(f"Error starting web UI: {e}", err=True)
        sys.exit(1)

def main():
    """Main entry point."""
    cli()

if __name__ == '__main__':
    main()