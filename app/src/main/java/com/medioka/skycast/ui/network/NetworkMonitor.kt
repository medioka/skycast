package com.medioka.skycast.ui.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.telephony.TelephonyManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    data class NetworkStatus(
        val isConnected: Boolean,
        val type: NetworkType,
        val carrierName: String?,
        val bandwidthKbps: Int,
        val isMetered: Boolean
    )

    enum class NetworkType {
        WIFI, CELLULAR, OTHER, NONE
    }

    val networkStatusFlow: Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentNetworkStatus())
            }

            override fun onLost(network: Network) {
                trySend(getCurrentNetworkStatus())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(getCurrentNetworkStatus())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            // Fallback if permission/manager registry fails
        }

        // Send initial state
        trySend(getCurrentNetworkStatus())

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun getCurrentNetworkStatus(): NetworkStatus {
        try {
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork == null) {
                return NetworkStatus(
                    isConnected = false,
                    type = NetworkType.NONE,
                    carrierName = null,
                    bandwidthKbps = 0,
                    isMetered = false
                )
            }

            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            val type = when {
                capabilities == null -> NetworkType.NONE
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                else -> NetworkType.OTHER
            }

            val carrierName = if (type == NetworkType.CELLULAR) {
                telephonyManager.networkOperatorName.takeIf { it.isNotEmpty() } ?: "Cellular Carrier"
            } else {
                null
            }

            val bandwidth = capabilities?.linkDownstreamBandwidthKbps ?: 0
            val isMetered = capabilities?.let {
                !it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            } ?: true

            return NetworkStatus(
                isConnected = isConnected,
                type = type,
                carrierName = carrierName,
                bandwidthKbps = bandwidth,
                isMetered = isMetered
            )
        } catch (e: Exception) {
            return NetworkStatus(
                isConnected = false,
                type = NetworkType.NONE,
                carrierName = null,
                bandwidthKbps = 0,
                isMetered = false
            )
        }
    }
}
