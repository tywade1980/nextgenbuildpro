package com.nextgenbuildpro

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Telephony
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Comprehensive Permission Manager for NextGenBuildPro
 * 
 * Features:
 * - Centralized permission handling for all required permissions
 * - Permission status tracking
 * - Permission request handling
 * - SMS sending functionality
 * - Phone call handling
 */
class PermissionManager(private val context: Context) {

    // Permission status states
    private val _hasLocationPermission = mutableStateOf(false)
    private val _hasCameraPermission = mutableStateOf(false)
    private val _hasSmsPermission = mutableStateOf(false)
    private val _hasCallPhonePermission = mutableStateOf(false)
    private val _hasBackgroundLocationPermission = mutableStateOf(false)
    private val _hasBluetoothPermission = mutableStateOf(false)
    private val _isDefaultDialer = mutableStateOf(false)
    private val _isDefaultSmsHandler = mutableStateOf(false)

    // Public access to permission states
    val hasLocationPermission: State<Boolean> = _hasLocationPermission
    val hasCameraPermission: State<Boolean> = _hasCameraPermission
    val hasSmsPermission: State<Boolean> = _hasSmsPermission
    val hasCallPhonePermission: State<Boolean> = _hasCallPhonePermission
    val hasBackgroundLocationPermission: State<Boolean> = _hasBackgroundLocationPermission
    val hasBluetoothPermission: State<Boolean> = _hasBluetoothPermission
    val isDefaultDialer: State<Boolean> = _isDefaultDialer
    val isDefaultSmsHandler: State<Boolean> = _isDefaultSmsHandler

    init {
        // Initialize permission states
        updatePermissionStates()
    }

    /**
     * Updates all permission states
     */
    fun updatePermissionStates() {
        _hasLocationPermission.value = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        _hasCameraPermission.value = checkPermission(Manifest.permission.CAMERA)
        _hasSmsPermission.value = checkPermission(Manifest.permission.SEND_SMS)
        _hasCallPhonePermission.value = checkPermission(Manifest.permission.CALL_PHONE)
        _hasBackgroundLocationPermission.value = checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        // Check Bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12+, check BLUETOOTH_CONNECT and BLUETOOTH_SCAN
            _hasBluetoothPermission.value = checkPermission(Manifest.permission.BLUETOOTH_CONNECT) && 
                                           checkPermission(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            // For older versions, check BLUETOOTH and BLUETOOTH_ADMIN
            _hasBluetoothPermission.value = checkPermission(Manifest.permission.BLUETOOTH) && 
                                           checkPermission(Manifest.permission.BLUETOOTH_ADMIN)
        }

        _isDefaultDialer.value = isDefaultDialerApp()
        _isDefaultSmsHandler.value = isDefaultSmsApp()
    }

    /**
     * Checks if the app is the default dialer
     */
    private fun isDefaultDialerApp(): Boolean {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        return telecomManager?.defaultDialerPackage == context.packageName
    }

    /**
     * Checks if the app is the default SMS app
     */
    private fun isDefaultSmsApp(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        } else {
            false
        }
    }

    /**
     * Checks if a specific permission is granted
     */
    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests location permissions
     * On Android 10+, background location permission must be requested separately
     */
    fun requestLocationPermissions(activity: Activity) {
        // First request foreground location permissions
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Requests background location permission
     * This should be called only after foreground location permissions are granted
     */
    fun requestBackgroundLocationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Check if foreground location permissions are granted
            if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {

                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                Toast.makeText(
                    context,
                    "Please grant location permissions first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Requests camera permission
     */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Requests SMS permissions
     */
    fun requestSmsPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            ),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Requests phone call permissions
     */
    fun requestCallPhonePermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG
            ),
            CALL_PHONE_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Requests Bluetooth permissions
     * Different permissions are requested based on Android version
     */
    fun requestBluetoothPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12+, request BLUETOOTH_CONNECT and BLUETOOTH_SCAN
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        } else {
            // For older versions, request BLUETOOTH and BLUETOOTH_ADMIN
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                ),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Requests all permissions at once
     * Background location is requested separately if needed
     */
    fun requestAllPermissions(activity: Activity) {
        // Create a list of permissions to request
        val permissionsList = mutableListOf(
            // Location permissions (foreground only)
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

            // Camera permission
            Manifest.permission.CAMERA,

            // SMS permissions
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,

            // Phone call permissions
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG
        )

        // Add Bluetooth permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12+, add BLUETOOTH_CONNECT and BLUETOOTH_SCAN
            permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionsList.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            // For older versions, add BLUETOOTH and BLUETOOTH_ADMIN
            permissionsList.add(Manifest.permission.BLUETOOTH)
            permissionsList.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        // Request all permissions except background location
        ActivityCompat.requestPermissions(
            activity,
            permissionsList.toTypedArray(),
            ALL_PERMISSIONS_REQUEST_CODE
        )

        // Background location will be requested separately after foreground location is granted
        Log.d("PermissionManager", "Requested all permissions except background location")
    }

    /**
     * Opens app settings to allow user to grant permissions manually
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    /**
     * Makes a phone call if permission is granted
     */
    fun makePhoneCall(phoneNumber: String) {
        if (_hasCallPhonePermission.value) {
            try {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$phoneNumber")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to make call: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Call permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sends an SMS if permission is granted
     */
    fun sendSms(phoneNumber: String, message: String) {
        if (_hasSmsPermission.value) {
            try {
                // Use the new SmsManager API for Android 11+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                } else {
                    // Use the deprecated API for older versions
                    @Suppress("DEPRECATION")
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                }
                Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show()
                Log.d("PermissionManager", "SMS sent to $phoneNumber")
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PermissionManager", "Failed to send SMS: ${e.message}")
            }
        } else {
            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show()
            Log.w("PermissionManager", "Cannot send SMS: Permission not granted")
        }
    }

    /**
     * Handles permission result from ActivityCompat.requestPermissions
     */
    fun handlePermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // Check each permission in the result
                for (i in permissions.indices) {
                    if (i < grantResults.size && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        when (permissions[i]) {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                _hasLocationPermission.value = true
                                Log.d("PermissionManager", "Location permission granted")
                            }
                        }
                    }
                }
            }
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _hasBackgroundLocationPermission.value = true
                    Log.d("PermissionManager", "Background location permission granted")
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _hasCameraPermission.value = true
                    Log.d("PermissionManager", "Camera permission granted")
                }
            }
            SMS_PERMISSION_REQUEST_CODE -> {
                // Check if all SMS permissions are granted
                var allSmsPermissionsGranted = true
                val smsPermissions = arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                )

                for (permission in smsPermissions) {
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        allSmsPermissionsGranted = false
                        Log.d("PermissionManager", "SMS permission not granted: $permission")
                        break
                    }
                }

                _hasSmsPermission.value = allSmsPermissionsGranted
                if (allSmsPermissionsGranted) {
                    Log.d("PermissionManager", "All SMS permissions granted")
                }
            }
            CALL_PHONE_PERMISSION_REQUEST_CODE -> {
                // Check if all call permissions are granted
                var allCallPermissionsGranted = true
                val callPermissions = arrayOf(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_CALL_LOG
                )

                for (permission in callPermissions) {
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        allCallPermissionsGranted = false
                        Log.d("PermissionManager", "Call permission not granted: $permission")
                        break
                    }
                }

                _hasCallPhonePermission.value = allCallPermissionsGranted
                if (allCallPermissionsGranted) {
                    Log.d("PermissionManager", "All call permissions granted")
                }
            }
            ALL_PERMISSIONS_REQUEST_CODE -> {
                updatePermissionStates()
                Log.d("PermissionManager", "All permissions updated")
            }
            DEFAULT_DIALER_REQUEST_CODE -> {
                updatePermissionStates()
                Log.d("PermissionManager", "Default dialer status updated: ${_isDefaultDialer.value}")
            }
            DEFAULT_SMS_HANDLER_REQUEST_CODE -> {
                updatePermissionStates()
                Log.d("PermissionManager", "Default SMS handler status updated: ${_isDefaultSmsHandler.value}")
            }
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                // Check if all Bluetooth permissions are granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // For Android 12+, check BLUETOOTH_CONNECT and BLUETOOTH_SCAN
                    val bluetoothPermissions = arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )

                    var allBluetoothPermissionsGranted = true
                    for (permission in bluetoothPermissions) {
                        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                            allBluetoothPermissionsGranted = false
                            Log.d("PermissionManager", "Bluetooth permission not granted: $permission")
                            break
                        }
                    }

                    _hasBluetoothPermission.value = allBluetoothPermissionsGranted
                    if (allBluetoothPermissionsGranted) {
                        Log.d("PermissionManager", "All Bluetooth permissions granted (Android 12+)")
                    }
                } else {
                    // For older versions, check BLUETOOTH and BLUETOOTH_ADMIN
                    val bluetoothPermissions = arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                    )

                    var allBluetoothPermissionsGranted = true
                    for (permission in bluetoothPermissions) {
                        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                            allBluetoothPermissionsGranted = false
                            Log.d("PermissionManager", "Bluetooth permission not granted: $permission")
                            break
                        }
                    }

                    _hasBluetoothPermission.value = allBluetoothPermissionsGranted
                    if (allBluetoothPermissionsGranted) {
                        Log.d("PermissionManager", "All Bluetooth permissions granted")
                    }
                }
            }
        }
    }

    /**
     * Checks if a permission should show rationale
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Requests to be the default dialer app
     */
    fun requestDefaultDialer(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            activity.startActivityForResult(intent, DEFAULT_DIALER_REQUEST_CODE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            activity.startActivityForResult(intent, DEFAULT_DIALER_REQUEST_CODE)
        } else {
            Toast.makeText(
                context,
                "Setting default dialer is not supported on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Requests to be the default SMS app
     */
    fun requestDefaultSmsHandler(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            activity.startActivityForResult(intent, DEFAULT_SMS_HANDLER_REQUEST_CODE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            activity.startActivityForResult(intent, DEFAULT_SMS_HANDLER_REQUEST_CODE)
        } else {
            Toast.makeText(
                context,
                "Setting default SMS app is not supported on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1006
        const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        const val SMS_PERMISSION_REQUEST_CODE = 1003
        const val CALL_PHONE_PERMISSION_REQUEST_CODE = 1004
        const val ALL_PERMISSIONS_REQUEST_CODE = 1005
        const val DEFAULT_DIALER_REQUEST_CODE = 1007
        const val DEFAULT_SMS_HANDLER_REQUEST_CODE = 1008
        const val BLUETOOTH_PERMISSION_REQUEST_CODE = 1009
    }
}

/**
 * Composable function to use permission manager in Compose UI
 */
@Composable
fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    DisposableEffect(permissionManager) {
        onDispose {
            // Clean up if needed
        }
    }

    return permissionManager
}
