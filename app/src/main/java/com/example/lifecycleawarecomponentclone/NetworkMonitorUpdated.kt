package com.example.lifecycleawarecomponents

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NetworkMonitorUpdated constructor(context: Context) : DefaultLifecycleObserver {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var connectivityManager: ConnectivityManager? = null
    private var validNetworks: HashSet<Network> = HashSet<Network>()
    private lateinit var job: Job
    private lateinit var coroutineScope: CoroutineScope
    private var _networkAvailableStateFlow: MutableStateFlow<NetworkMonitorUpdated.NetworkState> =
        MutableStateFlow(NetworkState.Available)

    private val networkAvailableStateFlow get() = _networkAvailableStateFlow

    init {
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    public fun registerNetworkCallbacks() {
        initCoroutine()
        initNetworkMonitoring()
        checkCurrentNetworkState()
    }

    public fun unRegisterNetworkCallbacks() {
        validNetworks.clear()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
        job.cancel()
    }

    private fun initNetworkMonitoring() {
        networkCallback = createNetworkCallback()

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun initCoroutine() {
        job = Job()
        coroutineScope = CoroutineScope(Dispatchers.Default + job)
    }

    private fun checkCurrentNetworkState() {
        connectivityManager?.allNetworks?.let {
            validNetworks.addAll(it)
        }
        checkValidNetworks()

//        val network = connectivityManager?.activeNetwork
//        network ?: return false
//        return true
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            connectivityManager?.getNetworkCapabilities(network).also {
                if (it?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                    validNetworks.add(network)
                }
            }
            checkValidNetworks()
        }

        override fun onLost(network: Network) {
            validNetworks.remove(network)
            checkValidNetworks()
        }
    }

    private fun checkValidNetworks() {
        coroutineScope.launch {
            _networkAvailableStateFlow.emit(
                if (validNetworks.size > 0)
                    NetworkState.Available
                else
                    NetworkState.Unavailable
            )
        }
    }

    sealed class NetworkState {
        object Available : NetworkState()
        object Unavailable : NetworkState()
    }
}