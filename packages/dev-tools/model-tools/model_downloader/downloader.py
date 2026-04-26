import os
import logging
import requests
import time
from pathlib import Path
from typing import Dict, List, Optional, Any, Callable
from concurrent.futures import ThreadPoolExecutor, as_completed
from tqdm import tqdm
import hashlib

try:
    from huggingface_hub import hf_hub_download, snapshot_download, list_repo_files
    HF_AVAILABLE = True
except ImportError:
    HF_AVAILABLE = False
    logging.warning("huggingface_hub not available. Hugging Face downloads will not work.")

from .config import Config

logger = logging.getLogger(__name__)

class ModelDownloader:
    """Core model downloader with support for multiple sources."""
    
    def __init__(self, config: Config):
        """Initialize the model downloader.
        
        Args:
            config: Configuration instance
        """
        self.config = config
        self.download_dir = Path(config.get_download_directory())
        self.download_dir.mkdir(parents=True, exist_ok=True)
        
        # Download statistics
        self.download_stats = {
            'total_downloads': 0,
            'successful_downloads': 0,
            'failed_downloads': 0,
            'bytes_downloaded': 0
        }
        
        # Progress callbacks
        self.progress_callbacks: List[Callable] = []
        
    def add_progress_callback(self, callback: Callable):
        """Add a progress callback function."""
        self.progress_callbacks.append(callback)
    
    def _notify_progress(self, message: str, progress: float = None):
        """Notify all registered progress callbacks."""
        for callback in self.progress_callbacks:
            try:
                callback(message, progress)
            except Exception as e:
                logger.error(f"Error in progress callback: {e}")
    
    def download_model(self, model_name: str, source: str = 'huggingface', **kwargs) -> bool:
        """Download a single model.
        
        Args:
            model_name: Name/identifier of the model
            source: Source of the model (huggingface, url, etc.)
            **kwargs: Additional download parameters
            
        Returns:
            bool: True if download successful, False otherwise
        """
        logger.info(f"Starting download of model: {model_name} from {source}")
        self._notify_progress(f"Starting download: {model_name}")
        
        try:
            if source.lower() == 'huggingface':
                return self._download_huggingface_model(model_name, **kwargs)
            elif source.lower() == 'url':
                return self._download_from_url(model_name, kwargs.get('url'), **kwargs)
            else:
                logger.error(f"Unsupported source: {source}")
                return False
                
        except Exception as e:
            logger.error(f"Error downloading model {model_name}: {e}")
            self.download_stats['failed_downloads'] += 1
            self._notify_progress(f"Download failed: {model_name} - {str(e)}")
            return False
    
    def _download_huggingface_model(self, model_name: str, **kwargs) -> bool:
        """Download model from Hugging Face Hub.
        
        Args:
            model_name: Hugging Face model identifier
            **kwargs: Additional parameters
            
        Returns:
            bool: True if successful, False otherwise
        """
        if not HF_AVAILABLE:
            logger.error("Hugging Face Hub not available. Install huggingface_hub package.")
            return False
        
        try:
            model_dir = self.download_dir / model_name.replace('/', '_')
            model_dir.mkdir(parents=True, exist_ok=True)
            
            # Check if model already exists and is complete
            if self._is_model_complete(model_dir, model_name):
                logger.info(f"Model {model_name} already exists and is complete")
                self._notify_progress(f"Model already exists: {model_name}")
                return True
            
            logger.info(f"Downloading {model_name} to {model_dir}")
            
            # Download the entire repository
            downloaded_path = snapshot_download(
                repo_id=model_name,
                local_dir=str(model_dir),
                resume_download=self.config.get('download_settings.resume_downloads', True),
                local_files_only=False
            )
            
            # Create a completion marker
            completion_marker = model_dir / '.download_complete'
            with open(completion_marker, 'w') as f:
                f.write(f"Downloaded on: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
                f.write(f"Model: {model_name}\n")
            
            self.download_stats['successful_downloads'] += 1
            self.download_stats['total_downloads'] += 1
            
            logger.info(f"Successfully downloaded {model_name}")
            self._notify_progress(f"Download completed: {model_name}")
            
            return True
            
        except Exception as e:
            logger.error(f"Error downloading Hugging Face model {model_name}: {e}")
            self.download_stats['failed_downloads'] += 1
            return False
    
    def _download_from_url(self, model_name: str, url: str, **kwargs) -> bool:
        """Download model from a direct URL.
        
        Args:
            model_name: Name for the downloaded model
            url: URL to download from
            **kwargs: Additional parameters
            
        Returns:
            bool: True if successful, False otherwise
        """
        if not url:
            logger.error("URL not provided for URL download")
            return False
        
        try:
            model_dir = self.download_dir / model_name.replace('/', '_')
            model_dir.mkdir(parents=True, exist_ok=True)
            
            filename = kwargs.get('filename') or url.split('/')[-1]
            if not filename or filename == url:
                filename = 'model_file'
            
            filepath = model_dir / filename
            
            # Check if file already exists
            if filepath.exists() and kwargs.get('skip_existing', True):
                logger.info(f"File {filepath} already exists, skipping download")
                return True
            
            # Download with progress
            response = requests.get(url, stream=True, timeout=self.config.get('download_settings.timeout_seconds', 3600))
            response.raise_for_status()
            
            total_size = int(response.headers.get('content-length', 0))
            
            with open(filepath, 'wb') as f:
                if total_size > 0:
                    with tqdm(total=total_size, unit='B', unit_scale=True, desc=filename) as pbar:
                        for chunk in response.iter_content(chunk_size=8192):
                            if chunk:
                                f.write(chunk)
                                pbar.update(len(chunk))
                                self.download_stats['bytes_downloaded'] += len(chunk)
                else:
                    for chunk in response.iter_content(chunk_size=8192):
                        if chunk:
                            f.write(chunk)
                            self.download_stats['bytes_downloaded'] += len(chunk)
            
            self.download_stats['successful_downloads'] += 1
            self.download_stats['total_downloads'] += 1
            
            logger.info(f"Successfully downloaded {model_name} from URL")
            self._notify_progress(f"Download completed: {model_name}")
            
            return True
            
        except Exception as e:
            logger.error(f"Error downloading from URL {url}: {e}")
            self.download_stats['failed_downloads'] += 1
            return False
    
    def _is_model_complete(self, model_dir: Path, model_name: str) -> bool:
        """Check if a model download is complete.
        
        Args:
            model_dir: Directory where model should be stored
            model_name: Name of the model
            
        Returns:
            bool: True if model appears to be complete
        """
        completion_marker = model_dir / '.download_complete'
        if completion_marker.exists():
            return True
        
        # For Hugging Face models, check for common files
        if model_dir.exists():
            common_files = ['config.json', 'pytorch_model.bin', 'model.safetensors']
            has_model_files = any((model_dir / f).exists() for f in common_files)
            if has_model_files:
                # Consider it complete if it has model files (legacy check)
                return True
        
        return False
    
    def download_models(self, models: List[Dict[str, Any]], max_concurrent: int = None) -> Dict[str, bool]:
        """Download multiple models concurrently.
        
        Args:
            models: List of model configurations
            max_concurrent: Maximum concurrent downloads
            
        Returns:
            Dict mapping model names to success status
        """
        if max_concurrent is None:
            max_concurrent = self.config.get('download_settings.max_concurrent_downloads', 2)
        
        results = {}
        
        # Sort models by priority (lower number = higher priority)
        sorted_models = sorted(models, key=lambda x: x.get('priority', 999))
        
        logger.info(f"Starting download of {len(sorted_models)} models with max {max_concurrent} concurrent downloads")
        
        with ThreadPoolExecutor(max_workers=max_concurrent) as executor:
            # Submit download tasks
            future_to_model = {}
            for model in sorted_models:
                if model.get('auto_download', True):
                    future = executor.submit(
                        self.download_model,
                        model['name'],
                        model.get('source', 'huggingface'),
                        **model.get('download_params', {})
                    )
                    future_to_model[future] = model['name']
                else:
                    logger.info(f"Skipping {model['name']} (auto_download disabled)")
                    results[model['name']] = False
            
            # Collect results
            for future in as_completed(future_to_model):
                model_name = future_to_model[future]
                try:
                    success = future.result()
                    results[model_name] = success
                    if success:
                        logger.info(f"✓ Successfully downloaded: {model_name}")
                    else:
                        logger.error(f"✗ Failed to download: {model_name}")
                except Exception as e:
                    logger.error(f"✗ Exception downloading {model_name}: {e}")
                    results[model_name] = False
        
        return results
    
    def get_download_stats(self) -> Dict[str, Any]:
        """Get download statistics."""
        return self.download_stats.copy()
    
    def list_downloaded_models(self) -> List[Dict[str, Any]]:
        """List all downloaded models."""
        models = []
        
        if not self.download_dir.exists():
            return models
        
        for model_dir in self.download_dir.iterdir():
            if model_dir.is_dir():
                model_info = {
                    'name': model_dir.name,
                    'path': str(model_dir),
                    'size_bytes': self._get_directory_size(model_dir),
                    'complete': self._is_model_complete(model_dir, model_dir.name)
                }
                
                # Check for completion marker info
                completion_marker = model_dir / '.download_complete'
                if completion_marker.exists():
                    try:
                        with open(completion_marker, 'r') as f:
                            content = f.read()
                            if 'Downloaded on:' in content:
                                date_line = [line for line in content.split('\n') if 'Downloaded on:' in line][0]
                                model_info['downloaded_date'] = date_line.split('Downloaded on: ')[1]
                    except Exception:
                        pass
                
                models.append(model_info)
        
        return models
    
    def _get_directory_size(self, directory: Path) -> int:
        """Calculate total size of a directory."""
        total_size = 0
        try:
            for file_path in directory.rglob('*'):
                if file_path.is_file():
                    total_size += file_path.stat().st_size
        except Exception as e:
            logger.warning(f"Error calculating size for {directory}: {e}")
        return total_size