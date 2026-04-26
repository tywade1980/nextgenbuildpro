import logging
import time
import threading
from datetime import datetime, timedelta
from typing import Callable, Optional, Dict, Any
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.interval import IntervalTrigger
from apscheduler.triggers.cron import CronTrigger

from .config import Config
from .downloader import ModelDownloader
from .notifications import NotificationManager

logger = logging.getLogger(__name__)

class AutoScheduler:
    """Automated scheduler for background model downloads."""
    
    def __init__(self, config: Config, downloader: ModelDownloader, notification_manager: NotificationManager):
        """Initialize the scheduler.
        
        Args:
            config: Configuration instance
            downloader: Model downloader instance
            notification_manager: Notification manager instance
        """
        self.config = config
        self.downloader = downloader
        self.notification_manager = notification_manager
        self.scheduler = BackgroundScheduler()
        self.is_running = False
        
        # Track daily download counts
        self.daily_downloads = 0
        self.last_reset_date = datetime.now().date()
        
        # Setup download progress callback
        self.downloader.add_progress_callback(self._on_download_progress)
    
    def start(self):
        """Start the automated scheduler."""
        if self.is_running:
            logger.warning("Scheduler is already running")
            return
        
        if not self.config.is_auto_download_enabled():
            logger.info("Automated downloads are disabled in configuration")
            return
        
        try:
            # Schedule the main check job
            interval_hours = self.config.get_check_interval_hours()
            self.scheduler.add_job(
                func=self._check_and_download,
                trigger=IntervalTrigger(hours=interval_hours),
                id='model_check_job',
                name='Model Check and Download Job',
                max_instances=1,
                coalesce=True
            )
            
            # Schedule daily reset job at midnight
            self.scheduler.add_job(
                func=self._reset_daily_counters,
                trigger=CronTrigger(hour=0, minute=0),
                id='daily_reset_job',
                name='Daily Counter Reset Job'
            )
            
            self.scheduler.start()
            self.is_running = True
            
            logger.info(f"Scheduler started. Will check for models every {interval_hours} hours.")
            self.notification_manager.notify(
                "Scheduler Started",
                f"Automated model downloader started. Checking every {interval_hours} hours.",
                level="info"
            )
            
            # Run initial check if configured
            if self.config.get('schedule.run_initial_check', True):
                logger.info("Running initial model check...")
                threading.Thread(target=self._check_and_download, daemon=True).start()
                
        except Exception as e:
            logger.error(f"Error starting scheduler: {e}")
            self.notification_manager.notify(
                "Scheduler Error",
                f"Failed to start scheduler: {str(e)}",
                level="error"
            )
    
    def stop(self):
        """Stop the automated scheduler."""
        if not self.is_running:
            logger.warning("Scheduler is not running")
            return
        
        try:
            self.scheduler.shutdown(wait=True)
            self.is_running = False
            logger.info("Scheduler stopped")
            self.notification_manager.notify(
                "Scheduler Stopped",
                "Automated model downloader has been stopped.",
                level="info"
            )
        except Exception as e:
            logger.error(f"Error stopping scheduler: {e}")
    
    def _check_and_download(self):
        """Main job function to check and download models."""
        try:
            self._reset_daily_counters_if_needed()
            
            max_daily_downloads = self.config.get('schedule.max_downloads_per_day', 5)
            
            if self.daily_downloads >= max_daily_downloads:
                logger.info(f"Daily download limit reached ({max_daily_downloads}). Skipping download check.")
                return
            
            models = self.config.get_models()
            auto_download_models = [m for m in models if m.get('auto_download', True)]
            
            if not auto_download_models:
                logger.info("No models configured for automatic download")
                return
            
            logger.info(f"Checking {len(auto_download_models)} models for updates/downloads")
            self.notification_manager.notify(
                "Download Check Started",
                f"Checking {len(auto_download_models)} models for updates",
                level="info"
            )
            
            # Filter models that need downloading
            models_to_download = []
            for model in auto_download_models:
                if self._should_download_model(model):
                    models_to_download.append(model)
                    
                    # Respect daily limit
                    if len(models_to_download) >= (max_daily_downloads - self.daily_downloads):
                        break
            
            if not models_to_download:
                logger.info("No models need downloading at this time")
                return
            
            logger.info(f"Starting download of {len(models_to_download)} models")
            
            # Download models
            results = self.downloader.download_models(models_to_download)
            
            # Update daily download count
            successful_downloads = sum(1 for success in results.values() if success)
            self.daily_downloads += successful_downloads
            
            # Send notification with results
            self._notify_download_results(results)
            
        except Exception as e:
            logger.error(f"Error in scheduled download check: {e}")
            self.notification_manager.notify(
                "Download Check Error",
                f"Error during scheduled download: {str(e)}",
                level="error"
            )
    
    def _should_download_model(self, model: Dict[str, Any]) -> bool:
        """Determine if a model should be downloaded.
        
        Args:
            model: Model configuration
            
        Returns:
            bool: True if model should be downloaded
        """
        model_name = model['name']
        
        # Check if model already exists and is complete
        downloaded_models = self.downloader.list_downloaded_models()
        for downloaded in downloaded_models:
            if downloaded['name'] == model_name.replace('/', '_') and downloaded['complete']:
                # Check if we should re-download based on age
                download_age_days = self._get_model_age_days(downloaded)
                max_age_days = model.get('max_age_days', self.config.get('schedule.max_model_age_days', 30))
                
                if download_age_days < max_age_days:
                    logger.debug(f"Model {model_name} is recent (age: {download_age_days} days), skipping")
                    return False
                else:
                    logger.info(f"Model {model_name} is old (age: {download_age_days} days), will re-download")
                    return True
        
        # Model doesn't exist or is incomplete
        logger.info(f"Model {model_name} not found or incomplete, will download")
        return True
    
    def _get_model_age_days(self, model_info: Dict[str, Any]) -> float:
        """Get the age of a downloaded model in days.
        
        Args:
            model_info: Model information dictionary
            
        Returns:
            float: Age in days
        """
        try:
            if 'downloaded_date' in model_info:
                download_date = datetime.strptime(model_info['downloaded_date'], '%Y-%m-%d %H:%M:%S')
                age = datetime.now() - download_date
                return age.total_seconds() / 86400  # Convert to days
        except Exception as e:
            logger.warning(f"Error calculating model age: {e}")
        
        # If we can't determine age, assume it's old
        return 999
    
    def _reset_daily_counters_if_needed(self):
        """Reset daily counters if it's a new day."""
        current_date = datetime.now().date()
        if current_date > self.last_reset_date:
            self._reset_daily_counters()
    
    def _reset_daily_counters(self):
        """Reset daily download counters."""
        self.daily_downloads = 0
        self.last_reset_date = datetime.now().date()
        logger.debug("Daily download counters reset")
    
    def _on_download_progress(self, message: str, progress: Optional[float] = None):
        """Handle download progress updates.
        
        Args:
            message: Progress message
            progress: Progress percentage (0-100) if available
        """
        if self.config.get('notifications.notify_on_progress', False):
            self.notification_manager.notify(
                "Download Progress",
                message,
                level="info"
            )
    
    def _notify_download_results(self, results: Dict[str, bool]):
        """Send notification with download results.
        
        Args:
            results: Dictionary mapping model names to success status
        """
        successful = [name for name, success in results.items() if success]
        failed = [name for name, success in results.items() if not success]
        
        if successful:
            message = f"Successfully downloaded {len(successful)} model(s):\n"
            message += "\n".join(f"✓ {name}" for name in successful)
            
            if failed:
                message += f"\n\nFailed to download {len(failed)} model(s):\n"
                message += "\n".join(f"✗ {name}" for name in failed)
            
            self.notification_manager.notify(
                "Download Completed",
                message,
                level="info" if not failed else "warning"
            )
        elif failed:
            message = f"Failed to download {len(failed)} model(s):\n"
            message += "\n".join(f"✗ {name}" for name in failed)
            
            self.notification_manager.notify(
                "Download Failed",
                message,
                level="error"
            )
    
    def get_status(self) -> Dict[str, Any]:
        """Get scheduler status information.
        
        Returns:
            Dict containing scheduler status
        """
        return {
            'running': self.is_running,
            'daily_downloads': self.daily_downloads,
            'max_daily_downloads': self.config.get('schedule.max_downloads_per_day', 5),
            'last_reset_date': self.last_reset_date.isoformat(),
            'check_interval_hours': self.config.get_check_interval_hours(),
            'next_run_time': str(self.scheduler.get_job('model_check_job').next_run_time) if self.is_running else None
        }
    
    def force_check(self):
        """Force an immediate model check and download."""
        logger.info("Forcing immediate model check...")
        self.notification_manager.notify(
            "Manual Check Started",
            "Starting manual model check and download",
            level="info"
        )
        
        # Run in separate thread to avoid blocking
        threading.Thread(target=self._check_and_download, daemon=True).start()