import os
import yaml
import logging
from typing import Dict, List, Optional, Any
from pathlib import Path

logger = logging.getLogger(__name__)

class Config:
    """Configuration management for the model downloader."""
    
    DEFAULT_CONFIG = {
        'download_directory': './models',
        'schedule': {
            'enabled': True,
            'check_interval_hours': 24,
            'max_downloads_per_day': 5
        },
        'models': [
            {
                'name': 'microsoft/DialoGPT-medium',
                'source': 'huggingface',
                'auto_download': True,
                'priority': 1
            }
        ],
        'notifications': {
            'enabled': True,
            'log_level': 'INFO',
            'notify_on_start': True,
            'notify_on_completion': True,
            'notify_on_error': True
        },
        'download_settings': {
            'max_concurrent_downloads': 2,
            'retry_attempts': 3,
            'timeout_seconds': 3600,
            'resume_downloads': True
        }
    }
    
    def __init__(self, config_path: Optional[str] = None):
        """Initialize configuration.
        
        Args:
            config_path: Path to configuration file. If None, uses default location.
        """
        self.config_path = config_path or os.path.expanduser('~/.model-downloader/config.yaml')
        self.config_dir = Path(self.config_path).parent
        self.config = self._load_config()
        
    def _load_config(self) -> Dict[str, Any]:
        """Load configuration from file or create default."""
        try:
            if os.path.exists(self.config_path):
                with open(self.config_path, 'r') as f:
                    config = yaml.safe_load(f)
                    # Merge with defaults to ensure all keys exist
                    return self._merge_configs(self.DEFAULT_CONFIG, config)
            else:
                logger.info(f"Config file not found at {self.config_path}. Creating default configuration.")
                self._ensure_config_dir()
                self._save_config(self.DEFAULT_CONFIG)
                return self.DEFAULT_CONFIG.copy()
        except Exception as e:
            logger.error(f"Error loading config: {e}. Using default configuration.")
            return self.DEFAULT_CONFIG.copy()
    
    def _merge_configs(self, default: Dict, user: Dict) -> Dict:
        """Recursively merge user config with default config."""
        result = default.copy()
        for key, value in user.items():
            if key in result and isinstance(result[key], dict) and isinstance(value, dict):
                result[key] = self._merge_configs(result[key], value)
            else:
                result[key] = value
        return result
    
    def _ensure_config_dir(self):
        """Ensure configuration directory exists."""
        self.config_dir.mkdir(parents=True, exist_ok=True)
    
    def _save_config(self, config: Dict[str, Any]):
        """Save configuration to file."""
        try:
            self._ensure_config_dir()
            with open(self.config_path, 'w') as f:
                yaml.dump(config, f, default_flow_style=False, indent=2)
        except Exception as e:
            logger.error(f"Error saving config: {e}")
    
    def get(self, key: str, default: Any = None) -> Any:
        """Get configuration value by key (supports dot notation)."""
        keys = key.split('.')
        value = self.config
        try:
            for k in keys:
                value = value[k]
            return value
        except (KeyError, TypeError):
            return default
    
    def set(self, key: str, value: Any):
        """Set configuration value by key (supports dot notation)."""
        keys = key.split('.')
        config = self.config
        for k in keys[:-1]:
            if k not in config:
                config[k] = {}
            config = config[k]
        config[keys[-1]] = value
        self._save_config(self.config)
    
    def add_model(self, name: str, source: str = 'huggingface', auto_download: bool = True, priority: int = 1):
        """Add a new model to the configuration."""
        models = self.get('models', [])
        
        # Check if model already exists
        for model in models:
            if model['name'] == name:
                logger.warning(f"Model {name} already exists in configuration")
                return
        
        new_model = {
            'name': name,
            'source': source,
            'auto_download': auto_download,
            'priority': priority
        }
        models.append(new_model)
        self.set('models', models)
        logger.info(f"Added model {name} to configuration")
    
    def remove_model(self, name: str):
        """Remove a model from the configuration."""
        models = self.get('models', [])
        models = [m for m in models if m['name'] != name]
        self.set('models', models)
        logger.info(f"Removed model {name} from configuration")
    
    def get_download_directory(self) -> str:
        """Get the download directory path."""
        return os.path.expanduser(self.get('download_directory', './models'))
    
    def get_models(self) -> List[Dict[str, Any]]:
        """Get list of configured models."""
        return self.get('models', [])
    
    def is_auto_download_enabled(self) -> bool:
        """Check if automatic downloading is enabled."""
        return self.get('schedule.enabled', True)
    
    def get_check_interval_hours(self) -> int:
        """Get the check interval in hours."""
        return self.get('schedule.check_interval_hours', 24)