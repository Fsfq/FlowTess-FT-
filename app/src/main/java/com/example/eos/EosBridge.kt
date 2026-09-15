package com.example.eos

import android.util.Log

interface EosLoginCallback {
    fun onLoginResult(success: Boolean, puid: String?)
}

interface EosLobbyCallback {
    fun onLobbyResult(
        success: Boolean,
        lobbyId: String?,
        hostPuid: String?,
        roomName: String?,
        hostName: String?,
        hostTier: String?,
        bet: Int
    )
}

interface EosMemberStatusCallback {
    fun onMemberStatusChanged(lobbyId: String, memberPuid: String, status: String)
}

interface EosSearchCallback {
    fun onSearchResult(success: Boolean, roomsJson: String?)
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

    fun createLobby(
        roomName: String,
        bet: Int,
        isPrivate: Boolean,
        shortCode: String,
        hostName: String,
        hostTier: String,
        callback: EosLobbyCallback
    ) {
        if (!isNativeLoaded) {
            callback.onLobbyResult(false, null, null, null, null, null, 0)
            return
        }
        nativeCreateLobby(roomName, bet, isPrivate, shortCode, hostName, hostTier, callback)
    }

    fun joinLobby(lobbyId: String, callback: EosLobbyCallback) {
        if (!isNativeLoaded) {
            callback.onLobbyResult(false, null, null, null, null, null, 0)
            return
        }
        nativeJoinLobby(lobbyId, callback)
    }

    fun leaveLobby(lobbyId: String) {
        if (!isNativeLoaded) return
        nativeLeaveLobby(lobbyId)
    }

    fun searchLobbies(callback: EosSearchCallback) {
        if (!isNativeLoaded) {
            callback.onSearchResult(false, null)
            return
        }
        nativeSearchLobbies(callback)
    }

    fun searchLobbyByCode(code: String, callback: EosSearchCallback) {
        if (!isNativeLoaded) {
            callback.onSearchResult(false, null)
            return
        }
        nativeSearchLobbyByCode(code, callback)
    }

    fun setRelayControl(relayMode: Int): Boolean {
        if (!isNativeLoaded) return false
        return nativeSetRelayControl(relayMode)
    }

    fun setupMemberStatusNotification(callback: EosMemberStatusCallback?) {
        if (!isNativeLoaded) return
        nativeSetupMemberStatusNotification(callback)
    }

    fun sendPacket(
        targetPuid: String,
        socketName: String,
        data: ByteArray,
        channel: Int = 0,
        isReliable: Boolean = true
    ): Boolean {
        if (!isNativeLoaded) return false
        return nativeSendPacketFull(targetPuid, socketName, data, channel, isReliable)
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
    private external fun nativeSetRelayControl(relayControl: Int): Boolean
    private external fun nativeCreateLobby(
        roomName: String,
        bet: Int,
        isPrivate: Boolean,
        shortCode: String,
        hostName: String,
        hostTier: String,
        callback: EosLobbyCallback
    )
    private external fun nativeJoinLobby(lobbyId: String, callback: EosLobbyCallback)
    private external fun nativeLeaveLobby(lobbyId: String)
    private external fun nativeSearchLobbies(callback: EosSearchCallback)
    private external fun nativeSearchLobbyByCode(code: String, callback: EosSearchCallback)
    private external fun nativeSetupMemberStatusNotification(callback: EosMemberStatusCallback?)
    private external fun nativeSendPacketFull(targetPuid: String, socketName: String, data: ByteArray, channel: Int, isReliable: Boolean): Boolean
    private external fun nativeReceivePacket(socketName: String): ByteArray?
    private external fun nativeAcceptConnection(remotePuid: String, socketName: String): Boolean
}
