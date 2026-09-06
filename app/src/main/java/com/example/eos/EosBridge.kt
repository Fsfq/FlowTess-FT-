package com.example.eos

import android.util.Log

interface EosLoginCallback {
    fun onLoginResult(success: Boolean, puid: String?)
}

interface EosLobbyCallback {
    fun onLobbyResult(success: Boolean, lobbyId: String?)
}

object EosBridge {
    private const val TAG = "EosBridge"
    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("EOSSDK")
            System.loadLibrary("eos_bridge")
            isNativeLoaded = true
            Log.i(TAG, "Native libraries EOSSDK and eos_bridge loaded successfully")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load native EOS libraries: ${e.message}", e)
        }
    }

    fun isLoaded(): Boolean = isNativeLoaded

    fun initSdk(cacheDir: String): Boolean {
        if (!isNativeLoaded) return false
        return nativeInitSdk(cacheDir)
    }

    fun createPlatform(
        productId: String,
        sandboxId: String,
        deploymentId: String,
        clientId: String = "",
        clientSecret: String = ""
    ): Boolean {
        if (!isNativeLoaded) return false
        return nativeCreatePlatform(productId, sandboxId, deploymentId, clientId, clientSecret)
    }

    fun tick() {
        if (!isNativeLoaded) return
        nativeTick()
    }

    fun shutdown() {
        if (!isNativeLoaded) return
        nativeShutdown()
    }

    fun loginAnonymous(displayName: String, callback: EosLoginCallback) {
        if (!isNativeLoaded) {
            callback.onLoginResult(false, null)
            return
        }
        nativeLoginAnonymous(displayName, callback)
    }

    fun getLocalProductUserId(): String? {
        if (!isNativeLoaded) return null
        return nativeGetLocalProductUserId()
    }

    fun setupP2pNotification() {
        if (!isNativeLoaded) return
        nativeSetupP2pNotification()
    }

    fun createLobby(roomName: String, bet: Int, isPrivate: Boolean, callback: EosLobbyCallback) {
        if (!isNativeLoaded) {
            callback.onLobbyResult(false, null)
            return
        }
        nativeCreateLobby(roomName, bet, isPrivate, callback)
    }

    fun joinLobby(lobbyId: String, callback: EosLobbyCallback) {
        if (!isNativeLoaded) {
            callback.onLobbyResult(false, null)
            return
        }
        nativeJoinLobby(lobbyId, callback)
    }

    fun leaveLobby(lobbyId: String) {
        if (!isNativeLoaded) return
        nativeLeaveLobby(lobbyId)
    }

    fun sendPacket(targetPuid: String, socketName: String, data: ByteArray): Boolean {
        if (!isNativeLoaded) return false
        return nativeSendPacket(targetPuid, socketName, data)
    }

    fun receivePacket(socketName: String): ByteArray? {
        if (!isNativeLoaded) return null
        return nativeReceivePacket(socketName)
    }

    fun acceptConnection(remotePuid: String, socketName: String): Boolean {
        if (!isNativeLoaded) return false
        return nativeAcceptConnection(remotePuid, socketName)
    }

    // ── NATIVE JNI DECLARATIONS ──
    private external fun nativeInitSdk(cacheDir: String): Boolean
    private external fun nativeCreatePlatform(
        productId: String,
        sandboxId: String,
        deploymentId: String,
        clientId: String,
        clientSecret: String
    ): Boolean
    private external fun nativeTick()
    private external fun nativeShutdown()
    private external fun nativeLoginAnonymous(displayName: String, callback: EosLoginCallback)
    private external fun nativeGetLocalProductUserId(): String?
    private external fun nativeSetupP2pNotification()
    private external fun nativeCreateLobby(roomName: String, bet: Int, isPrivate: Boolean, callback: EosLobbyCallback)
    private external fun nativeJoinLobby(lobbyId: String, callback: EosLobbyCallback)
    private external fun nativeLeaveLobby(lobbyId: String)
    private external fun nativeSendPacket(targetPuid: String, socketName: String, data: ByteArray): Boolean
    private external fun nativeReceivePacket(socketName: String): ByteArray?
    private external fun nativeAcceptConnection(remotePuid: String, socketName: String): Boolean
}
