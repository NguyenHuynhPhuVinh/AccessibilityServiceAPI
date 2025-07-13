package com.tomisakae.accessibilityserviceapi

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tomisakae.accessibilityserviceapi.service.AccessibilityApiService
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var statusTextView: TextView
    private lateinit var serverStatusTextView: TextView
    private lateinit var serverUrlTextView: TextView
    private lateinit var enableServiceButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupClickListeners()
        updateServiceStatus()
    }
    
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
    
    private fun initViews() {
        statusTextView = findViewById(R.id.statusTextView)
        serverStatusTextView = findViewById(R.id.serverStatusTextView)
        serverUrlTextView = findViewById(R.id.serverUrlTextView)
        enableServiceButton = findViewById(R.id.enableServiceButton)

        updateServerUrl()
    }
    
    private fun setupClickListeners() {
        enableServiceButton.setOnClickListener {
            openAccessibilitySettings()
        }

        // Refresh IP when clicking on server URL
        serverUrlTextView.setOnClickListener {
            updateServerUrl()
        }
    }
    
    private fun updateServiceStatus() {
        val isServiceEnabled = isAccessibilityServiceEnabled()

        if (isServiceEnabled) {
            statusTextView.text = "✅ ${getString(R.string.service_running)}"
            statusTextView.setTextColor(getColor(R.color.dark_success))
            enableServiceButton.text = "⚙️ Mở Cài đặt Accessibility"

            // Check if server is running
            val isServerRunning = AccessibilityApiService.isServerRunning()
            val serverStatusText = if (isServerRunning) {
                "🟢 API Server: Đang chạy"
            } else {
                "🔴 API Server: Đã dừng"
            }
            serverStatusTextView.text = serverStatusText
            serverStatusTextView.setTextColor(
                if (isServerRunning) getColor(R.color.dark_success)
                else getColor(R.color.dark_error)
            )
        } else {
            statusTextView.text = "❌ ${getString(R.string.service_stopped)}"
            statusTextView.setTextColor(getColor(R.color.dark_error))
            enableServiceButton.text = "🔧 ${getString(R.string.enable_accessibility)}"
            serverStatusTextView.text = "⚠️ API Server: Không khả dụng"
            serverStatusTextView.setTextColor(getColor(R.color.dark_warning))
        }

        // Update server URL display
        updateServerUrl()
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        for (service in enabledServices) {
            if (service.resolveInfo.serviceInfo.packageName == packageName) {
                return true
            }
        }
        return false
    }
    
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun updateServerUrl() {
        val ipAddress = getDeviceIpAddress()
        Log.d("MainActivity", "Device IP Address: '$ipAddress'")

        val urlText = if (ipAddress.isNotEmpty() && ipAddress != "0.0.0.0") {
            "🏠 Local:   http://localhost:8080\n🌐 Network: http://$ipAddress:8080\n\n💻 Desktop access:\ncurl http://$ipAddress:8080/health\n\n📱 Device IP: $ipAddress"
        } else {
            "🏠 Local: http://localhost:8080\n🌐 Network: http://0.0.0.0:8080\n\n⚠️ Không tìm thấy IP WiFi\n💡 Kiểm tra kết nối WiFi"
        }
        serverUrlTextView.text = urlText
    }

    private fun getDeviceIpAddress(): String {
        try {
            // Try WiFi first
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null && wifiInfo.ipAddress != 0) {
                val ip = Formatter.formatIpAddress(wifiInfo.ipAddress)
                Log.d("MainActivity", "WiFi IP: $ip")
                if (ip != "0.0.0.0") {
                    return ip
                }
            }

            // Fallback to network interfaces
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                Log.d("MainActivity", "Checking interface: ${networkInterface.name}")
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    val addresses = Collections.list(networkInterface.inetAddresses)
                    for (address in addresses) {
                        if (!address.isLoopbackAddress && !address.isLinkLocalAddress) {
                            val hostAddress = address.hostAddress
                            Log.d("MainActivity", "Found address: $hostAddress")
                            if (hostAddress != null && hostAddress.indexOf(':') < 0) { // IPv4
                                return hostAddress
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error getting IP address", e)
        }
        return ""
    }
}
