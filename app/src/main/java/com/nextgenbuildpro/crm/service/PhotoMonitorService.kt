package com.nextgenbuildpro.crm.service

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.nextgenbuildpro.core.DateUtils
import com.nextgenbuildpro.crm.data.model.PhotoLocation
import com.nextgenbuildpro.crm.data.repository.PhotoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.Locale

/**
 * Service to scan device photos and automatically assign them to clients based on location
 * Modified to only scan at app launch instead of continuous monitoring
 */
class PhotoMonitorService : Service() {
    private val TAG = "PhotoMonitorService"
    private lateinit var photoRepository: PhotoRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PhotoMonitorService created")

        // Initialize repository
        photoRepository = PhotoRepository(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "PhotoMonitorService started")

        // Scan for photos when service starts
        if (intent?.action == ACTION_SCAN_PHOTOS) {
            serviceScope.launch {
                scanPhotos()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PhotoMonitorService destroyed")
    }

    /**
     * Scan for new photos
     */
    suspend fun scanPhotos() {
        Log.d(TAG, "Scanning for new photos")
        val lastScanTimestamp = getLastScanTimestamp()
        val currentTime = System.currentTimeMillis()

        // Look for photos added since last scan
        val selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
        val selectionArgs = arrayOf((lastScanTimestamp / 1000).toString())

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
            ),
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val count = cursor.count
            Log.d(TAG, "Found $count new photos since last scan")

            while (cursor.moveToNext()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                val id = cursor.getLong(idColumn)
                val filePath = cursor.getString(dataColumn)

                val photoUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                processPhoto(photoUri, filePath)
            }
        }

        // Update last scan timestamp
        saveLastScanTimestamp(currentTime)
        Log.d(TAG, "Photo scan completed")
    }

    /**
     * Get the timestamp of the last photo scan
     */
    private fun getLastScanTimestamp(): Long {
        val prefs = applicationContext.getSharedPreferences("photo_monitor_prefs", MODE_PRIVATE)
        return prefs.getLong("last_scan_timestamp", 0)
    }

    /**
     * Save the timestamp of the current photo scan
     */
    private fun saveLastScanTimestamp(timestamp: Long) {
        val prefs = applicationContext.getSharedPreferences("photo_monitor_prefs", MODE_PRIVATE)
        prefs.edit().putLong("last_scan_timestamp", timestamp).apply()
    }

    /**
     * Process a single photo
     */
    private suspend fun processPhoto(photoUri: Uri, filePath: String) {
        try {
            Log.d(TAG, "Processing photo: $filePath")

            // Extract location from EXIF data
            val photoLocation = extractLocationFromExif(filePath)
            if (photoLocation == null) {
                Log.d(TAG, "No location data found for photo")
                return
            }

            // Find matching project location
            val projectLocation = photoRepository.findMatchingProjectLocation(photoLocation)
            if (projectLocation == null) {
                Log.d(TAG, "No matching project location found for photo")
                return
            }

            // Copy photo to app storage and associate with lead
            val clientPhoto = photoRepository.copyPhotoToAppStorage(
                sourceUri = photoUri,
                leadId = projectLocation.leadId,
                photoLocation = photoLocation,
                description = "Auto-detected at ${projectLocation.name}"
            )

            if (clientPhoto != null) {
                Log.d(TAG, "Photo successfully associated with lead ${projectLocation.leadId}")
            } else {
                Log.e(TAG, "Failed to copy photo to app storage")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing photo", e)
        }
    }

    /**
     * Extract location data from EXIF metadata
     */
    private fun extractLocationFromExif(filePath: String): PhotoLocation? {
        try {
            val exifInterface = ExifInterface(filePath)

            // Get GPS coordinates
            val latLong = FloatArray(2)
            if (exifInterface.getLatLong(latLong)) {
                val latitude = latLong[0].toDouble()
                val longitude = latLong[1].toDouble()

                // Get address from coordinates
                val address = getAddressFromLocation(latitude, longitude)

                return PhotoLocation(
                    latitude = latitude,
                    longitude = longitude,
                    address = address ?: ""
                )
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading EXIF data", e)
        }

        return null
    }

    /**
     * Get address from latitude and longitude
     */
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        try {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var addressLine: String? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        addressLine = address.getAddressLine(0)
                    }
                }
                // Wait a bit for the async operation to complete
                Thread.sleep(500)
                return addressLine
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    return address.getAddressLine(0)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error getting address from location", e)
        }

        return null
    }

    companion object {
        const val ACTION_SCAN_PHOTOS = "com.nextgenbuildpro.action.SCAN_PHOTOS"

        /**
         * Start the photo monitor service
         */
        fun startService(context: Context) {
            val intent = Intent(context, PhotoMonitorService::class.java)
            context.startService(intent)
        }

        /**
         * Scan for photos (to be called at app launch)
         */
        fun scanPhotos(context: Context) {
            val intent = Intent(context, PhotoMonitorService::class.java).apply {
                action = ACTION_SCAN_PHOTOS
            }
            context.startService(intent)
        }

        /**
         * Stop the photo monitor service
         */
        fun stopService(context: Context) {
            val intent = Intent(context, PhotoMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
