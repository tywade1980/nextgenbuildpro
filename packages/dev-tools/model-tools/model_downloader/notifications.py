import logging
import os
import sys
from datetime import datetime
from typing import Optional, Dict, Any, List
from pathlib import Path

logger = logging.getLogger(__name__)

class NotificationManager:
    """Manages notifications and logging for the model downloader."""
    
    def __init__(self, config):
        """Initialize the notification manager.
        
        Args:
            config: Configuration instance
        """
        self.config = config
        self.log_file = None
        self.setup_logging()
        
        # Notification history
        self.notification_history: List[Dict[str, Any]] = []
        self.max_history_size = 100
    
    def setup_logging(self):
        """Setup logging configuration."""
        log_level = getattr(logging, self.config.get('notifications.log_level', 'INFO').upper())
        
        # Create logs directory
        log_dir = Path(self.config.get_download_directory()) / 'logs'
        log_dir.mkdir(parents=True, exist_ok=True)
        
        # Setup log file
        self.log_file = log_dir / 'model_downloader.log'
        
        # Configure logging
        logging.basicConfig(
            level=log_level,
            format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
            handlers=[
                logging.FileHandler(self.log_file),
                logging.StreamHandler(sys.stdout)
            ]
        )
        
        logger.info(f"Logging initialized. Log file: {self.log_file}")
    
    def notify(self, title: str, message: str, level: str = "info"):
        """Send a notification.
        
        Args:
            title: Notification title
            message: Notification message
            level: Notification level (info, warning, error)
        """
        if not self.config.get('notifications.enabled', True):
            return
        
        # Log the notification
        log_message = f"{title}: {message}"
        
        if level.lower() == "error":
            logger.error(log_message)
        elif level.lower() == "warning":
            logger.warning(log_message)
        else:
            logger.info(log_message)
        
        # Store in history
        notification = {
            'timestamp': datetime.now().isoformat(),
            'title': title,
            'message': message,
            'level': level
        }
        
        self.notification_history.append(notification)
        
        # Limit history size
        if len(self.notification_history) > self.max_history_size:
            self.notification_history = self.notification_history[-self.max_history_size:]
        
        # Try to send desktop notification if available
        self._send_desktop_notification(title, message, level)
    
    def _send_desktop_notification(self, title: str, message: str, level: str):
        """Attempt to send a desktop notification.
        
        Args:
            title: Notification title
            message: Notification message
            level: Notification level
        """
        if not self.config.get('notifications.desktop_notifications', False):
            return
        
        try:
            # Try different notification methods based on platform
            if sys.platform == "darwin":  # macOS
                self._send_macos_notification(title, message)
            elif sys.platform == "linux":  # Linux
                self._send_linux_notification(title, message)
            elif sys.platform == "win32":  # Windows
                self._send_windows_notification(title, message)
        except Exception as e:
            logger.debug(f"Desktop notification failed: {e}")
    
    def _send_macos_notification(self, title: str, message: str):
        """Send macOS notification."""
        os.system(f'''
            osascript -e 'display notification "{message}" with title "{title}"'
        ''')
    
    def _send_linux_notification(self, title: str, message: str):
        """Send Linux notification using notify-send."""
        os.system(f'notify-send "{title}" "{message}"')
    
    def _send_windows_notification(self, title: str, message: str):
        """Send Windows notification."""
        try:
            import win10toast
            toaster = win10toast.ToastNotifier()
            toaster.show_toast(title, message, duration=5)
        except ImportError:
            logger.debug("win10toast not available for Windows notifications")
    
    def get_notification_history(self, limit: Optional[int] = None) -> List[Dict[str, Any]]:
        """Get notification history.
        
        Args:
            limit: Maximum number of notifications to return
            
        Returns:
            List of notification dictionaries
        """
        history = self.notification_history.copy()
        if limit:
            history = history[-limit:]
        return history
    
    def clear_notification_history(self):
        """Clear notification history."""
        self.notification_history.clear()
        logger.info("Notification history cleared")
    
    def get_log_file_path(self) -> str:
        """Get the path to the log file.
        
        Returns:
            str: Path to log file
        """
        return str(self.log_file) if self.log_file else ""
    
    def get_log_tail(self, lines: int = 50) -> str:
        """Get the last N lines from the log file.
        
        Args:
            lines: Number of lines to return
            
        Returns:
            str: Last N lines from log file
        """
        if not self.log_file or not self.log_file.exists():
            return "Log file not found"
        
        try:
            with open(self.log_file, 'r') as f:
                all_lines = f.readlines()
                return ''.join(all_lines[-lines:])
        except Exception as e:
            logger.error(f"Error reading log file: {e}")
            return f"Error reading log file: {e}"
    
    def notify_startup(self):
        """Send startup notification."""
        if self.config.get('notifications.notify_on_start', True):
            self.notify(
                "Model Downloader Started",
                "Automated model downloader service has started",
                level="info"
            )
    
    def notify_shutdown(self):
        """Send shutdown notification."""
        if self.config.get('notifications.notify_on_shutdown', True):
            self.notify(
                "Model Downloader Stopped",
                "Automated model downloader service has stopped",
                level="info"
            )
    
    def notify_download_start(self, model_name: str):
        """Notify that a download has started.
        
        Args:
            model_name: Name of the model being downloaded
        """
        if self.config.get('notifications.notify_on_start', True):
            self.notify(
                "Download Started",
                f"Started downloading model: {model_name}",
                level="info"
            )
    
    def notify_download_complete(self, model_name: str, success: bool):
        """Notify that a download has completed.
        
        Args:
            model_name: Name of the model
            success: Whether the download was successful
        """
        if self.config.get('notifications.notify_on_completion', True):
            if success:
                self.notify(
                    "Download Completed",
                    f"Successfully downloaded model: {model_name}",
                    level="info"
                )
            else:
                self.notify(
                    "Download Failed",
                    f"Failed to download model: {model_name}",
                    level="error"
                )
    
    def notify_error(self, error_message: str, context: str = ""):
        """Notify about an error.
        
        Args:
            error_message: Error message
            context: Additional context about the error
        """
        if self.config.get('notifications.notify_on_error', True):
            message = error_message
            if context:
                message = f"{context}: {error_message}"
            
            self.notify(
                "Error Occurred",
                message,
                level="error"
            )