#include <jni.h>
#include <string>
#include <vector>
#include <mutex>
#include <sstream>
#include <android/log.h>

#include "eos_init.h"
#include "eos_sdk.h"
#include "eos_auth.h"
#include "eos_connect.h"
#include "eos_lobby.h"
#include "eos_p2p.h"
#include "eos_logging.h"
#include "Android/eos_android.h"

#define LOG_TAG "EOS_Bridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static JavaVM* g_JavaVM = nullptr;
static EOS_HPlatform g_PlatformHandle = nullptr;
static bool g_IsInitialized = false;
static EOS_ProductUserId g_LocalProductUserId = nullptr;
static std::string g_LocalProductUserIdStr = "";
static EOS_NotificationId g_P2pNotificationId = EOS_INVALID_NOTIFICATIONID;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_JavaVM = vm;
    return JNI_VERSION_1_6;
}

static JNIEnv* GetEnv() {
    JNIEnv* env = nullptr;
    if (g_JavaVM->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        g_JavaVM->AttachCurrentThread(&env, nullptr);
    }
    return env;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_eos_EosBridge_nativeInitSdk(JNIEnv* env, jobject thiz, jstring cacheDir) {
    if (g_IsInitialized) {
        return JNI_TRUE;
    }

    const char* pathChars = env->GetStringUTFChars(cacheDir, nullptr);
    std::string internalPath = pathChars ? pathChars : "";
    if (pathChars) env->ReleaseStringUTFChars(cacheDir, pathChars);

    EOS_Android_InitializeOptions SystemInitOptions = {};
    SystemInitOptions.ApiVersion = EOS_ANDROID_INITIALIZEOPTIONS_API_LATEST;
    SystemInitOptions.OptionalInternalDirectory = internalPath.empty() ? nullptr : internalPath.c_str();
    SystemInitOptions.OptionalExternalDirectory = nullptr;

    EOS_InitializeOptions InitOptions = {};
    InitOptions.ApiVersion = EOS_INITIALIZE_API_LATEST;
    InitOptions.ProductName = "TetrisOnline";
    InitOptions.ProductVersion = "1.0";
    InitOptions.SystemInitializeOptions = &SystemInitOptions;

    EOS_EResult Result = EOS_Initialize(&InitOptions);
    if (Result == EOS_EResult::EOS_Success || Result == EOS_EResult::EOS_AlreadyConfigured) {
        g_IsInitialized = true;
        LOGI("EOS_Initialize successful: %d", (int)Result);
        return JNI_TRUE;
    }

    LOGE("EOS_Initialize failed: %d", (int)Result);
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_eos_EosBridge_nativeCreatePlatform(
    JNIEnv* env, jobject thiz,
    jstring productId, jstring sandboxId, jstring deploymentId,
    jstring clientId, jstring clientSecret) {

    if (g_PlatformHandle != nullptr) {
        return JNI_TRUE;
    }

    const char* pId = env->GetStringUTFChars(productId, nullptr);
    const char* sId = env->GetStringUTFChars(sandboxId, nullptr);
    const char* dId = env->GetStringUTFChars(deploymentId, nullptr);
    const char* cId = clientId ? env->GetStringUTFChars(clientId, nullptr) : nullptr;
    const char* cSec = clientSecret ? env->GetStringUTFChars(clientSecret, nullptr) : nullptr;

    EOS_Platform_ClientCredentials ClientCredentials = {};
    ClientCredentials.ClientId = (cId && strlen(cId) > 0) ? cId : nullptr;
    ClientCredentials.ClientSecret = (cSec && strlen(cSec) > 0) ? cSec : nullptr;

    EOS_Platform_Options PlatformOptions = {};
    PlatformOptions.ApiVersion = EOS_PLATFORM_OPTIONS_API_LATEST;
    PlatformOptions.ProductId = pId;
    PlatformOptions.SandboxId = sId;
    PlatformOptions.DeploymentId = dId;
    PlatformOptions.ClientCredentials = ClientCredentials;
    PlatformOptions.bIsServer = EOS_FALSE;
    PlatformOptions.Flags = 0;

    g_PlatformHandle = EOS_Platform_Create(&PlatformOptions);

    env->ReleaseStringUTFChars(productId, pId);
    env->ReleaseStringUTFChars(sandboxId, sId);
    env->ReleaseStringUTFChars(deploymentId, dId);
    if (cId) env->ReleaseStringUTFChars(clientId, cId);
    if (cSec) env->ReleaseStringUTFChars(clientSecret, cSec);

    if (g_PlatformHandle == nullptr) {
        LOGE("EOS_Platform_Create failed");
        return JNI_FALSE;
    }

    LOGI("EOS_Platform_Create success");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_example_eos_EosBridge_nativeTick(JNIEnv* env, jobject thiz) {
    if (g_PlatformHandle != nullptr) {
        EOS_Platform_Tick(g_PlatformHandle);
    }
}

JNIEXPORT void JNICALL
Java_com_example_eos_EosBridge_nativeShutdown(JNIEnv* env, jobject thiz) {
    if (g_PlatformHandle != nullptr) {
        EOS_HP2P P2pHandle = EOS_Platform_GetP2PInterface(g_PlatformHandle);
        if (P2pHandle != nullptr && g_P2pNotificationId != EOS_INVALID_NOTIFICATIONID) {
            EOS_P2P_RemoveNotifyPeerConnectionRequest(P2pHandle, g_P2pNotificationId);
            g_P2pNotificationId = EOS_INVALID_NOTIFICATIONID;
        }
        EOS_Platform_Release(g_PlatformHandle);
        g_PlatformHandle = nullptr;
    }
    if (g_IsInitialized) {
        EOS_Shutdown();
        g_IsInitialized = false;
    }
    g_LocalProductUserId = nullptr;
    g_LocalProductUserIdStr = "";
    LOGI("EOS Shutdown completed");
}

struct LoginContext {
    jobject callbackRef;
    std::string displayName;
};

static void EOS_CALL ConnectLoginCallback(const EOS_Connect_LoginCallbackInfo* Data);

static void DoConnectLogin(LoginContext* ctx) {
    if (g_PlatformHandle == nullptr) {
        LOGE("g_PlatformHandle is null in DoConnectLogin");
        delete ctx;
        return;
    }
    EOS_HConnect ConnectHandle = EOS_Platform_GetConnectInterface(g_PlatformHandle);
    if (ConnectHandle == nullptr) {
        LOGE("ConnectHandle is null in DoConnectLogin");
        delete ctx;
        return;
    }

    EOS_Connect_Credentials Credentials = {};
    Credentials.ApiVersion = EOS_CONNECT_CREDENTIALS_API_LATEST;
    Credentials.Token = nullptr;
    Credentials.Type = EOS_EExternalCredentialType::EOS_ECT_DEVICEID_ACCESS_TOKEN;

    EOS_Connect_UserLoginInfo UserLoginInfo = {};
    UserLoginInfo.ApiVersion = EOS_CONNECT_USERLOGININFO_API_LATEST;
    UserLoginInfo.DisplayName = ctx->displayName.c_str();

    EOS_Connect_LoginOptions LoginOptions = {};
    LoginOptions.ApiVersion = EOS_CONNECT_LOGIN_API_LATEST;
    LoginOptions.Credentials = &Credentials;
    LoginOptions.UserLoginInfo = &UserLoginInfo;

    EOS_Connect_Login(ConnectHandle, &LoginOptions, ctx, ConnectLoginCallback);
}

static void EOS_CALL ConnectCreateDeviceIdCallback(const EOS_Connect_CreateDeviceIdCallbackInfo* Data) {
    LOGI("EOS_Connect_CreateDeviceId result: %d", (int)Data->ResultCode);
    LoginContext* ctx = static_cast<LoginContext*>(Data->ClientData);
    if (ctx != nullptr) {
        DoConnectLogin(ctx);
    }
}

static void EOS_CALL ConnectCreateUserCallback(const EOS_Connect_CreateUserCallbackInfo* Data) {
    LoginContext* ctx = static_cast<LoginContext*>(Data->ClientData);
    JNIEnv* env = GetEnv();

    bool success = (Data->ResultCode == EOS_EResult::EOS_Success);
    std::string puidStr = "";

    if (success) {
        g_LocalProductUserId = Data->LocalUserId;
        char puidBuffer[EOS_PRODUCTUSERID_MAX_LENGTH + 1];
        int32_t bufSize = sizeof(puidBuffer);
        if (EOS_ProductUserId_ToString(g_LocalProductUserId, puidBuffer, &bufSize) == EOS_EResult::EOS_Success) {
            g_LocalProductUserIdStr = puidBuffer;
            puidStr = puidBuffer;
        }
        LOGI("EOS_Connect_CreateUser success! Local PUID: %s", puidStr.c_str());
    } else {
        LOGE("EOS_Connect_CreateUser failed with code: %d", (int)Data->ResultCode);
    }

    if (ctx && ctx->callbackRef && env) {
        jclass cbClass = env->GetObjectClass(ctx->callbackRef);
        jmethodID methodId = env->GetMethodID(cbClass, "onLoginResult", "(ZLjava/lang/String;)V");
        if (methodId) {
            jstring puidJStr = puidStr.empty() ? nullptr : env->NewStringUTF(puidStr.c_str());
            env->CallVoidMethod(ctx->callbackRef, methodId, success ? JNI_TRUE : JNI_FALSE, puidJStr);
            if (puidJStr) env->DeleteLocalRef(puidJStr);
        }
        env->DeleteGlobalRef(ctx->callbackRef);
    }

    delete ctx;
}

static void EOS_CALL ConnectLoginCallback(const EOS_Connect_LoginCallbackInfo* Data) {
    LoginContext* ctx = static_cast<LoginContext*>(Data->ClientData);
    JNIEnv* env = GetEnv();

    if (Data->ResultCode == EOS_EResult::EOS_NotFound && Data->ContinuanceToken != nullptr) {
        LOGI("User not found in EOS Connect, creating user with ContinuanceToken...");
        if (g_PlatformHandle != nullptr) {
            EOS_HConnect ConnectHandle = EOS_Platform_GetConnectInterface(g_PlatformHandle);
            if (ConnectHandle != nullptr) {
                EOS_Connect_CreateUserOptions CreateUserOptions = {};
                CreateUserOptions.ApiVersion = EOS_CONNECT_CREATEUSER_API_LATEST;
                CreateUserOptions.ContinuanceToken = Data->ContinuanceToken;
                EOS_Connect_CreateUser(ConnectHandle, &CreateUserOptions, ctx, ConnectCreateUserCallback);
                return; // Wait for ConnectCreateUserCallback
            }
        }
    }

    bool success = (Data->ResultCode == EOS_EResult::EOS_Success);
    std::string puidStr = "";

    if (success) {
        g_LocalProductUserId = Data->LocalUserId;
        char puidBuffer[EOS_PRODUCTUSERID_MAX_LENGTH + 1];
        int32_t bufSize = sizeof(puidBuffer);
        if (EOS_ProductUserId_ToString(g_LocalProductUserId, puidBuffer, &bufSize) == EOS_EResult::EOS_Success) {
            g_LocalProductUserIdStr = puidBuffer;
            puidStr = puidBuffer;
        }
        LOGI("EOS_Connect_Login success! Local PUID: %s", puidStr.c_str());
    } else {
        LOGE("EOS_Connect_Login failed with code: %d", (int)Data->ResultCode);
    }

    if (ctx && ctx->callbackRef && env) {
        jclass cbClass = env->GetObjectClass(ctx->callbackRef);
        jmethodID methodId = env->GetMethodID(cbClass, "onLoginResult", "(ZLjava/lang/String;)V");
        if (methodId) {
            jstring puidJStr = puidStr.empty() ? nullptr : env->NewStringUTF(puidStr.c_str());
            env->CallVoidMethod(ctx->callbackRef, methodId, success ? JNI_TRUE : JNI_FALSE, puidJStr);
            if (puidJStr) env->DeleteLocalRef(puidJStr);
        }
        env->DeleteGlobalRef(ctx->callbackRef);
    }

    delete ctx;
}

JNIEXPORT void JNICALL
Java_com_example_eos_EosBridge_nativeLoginAnonymous(
    JNIEnv* env, jobject thiz, jstring displayName, jobject callback) {

    if (g_PlatformHandle == nullptr) {
        LOGE("PlatformHandle is null in nativeLoginAnonymous");
        return;
    }

    EOS_HConnect ConnectHandle = EOS_Platform_GetConnectInterface(g_PlatformHandle);
    if (ConnectHandle == nullptr) {
        LOGE("ConnectHandle is null");
        return;
    }

    const char* dName = displayName ? env->GetStringUTFChars(displayName, nullptr) : "TetrisPlayer";
    std::string nameStr = dName;
    if (displayName) env->ReleaseStringUTFChars(displayName, dName);

    LoginContext* ctx = new LoginContext();
    ctx->callbackRef = env->NewGlobalRef(callback);
    ctx->displayName = nameStr;

    EOS_Connect_CreateDeviceIdOptions DeviceOptions = {};
    DeviceOptions.ApiVersion = EOS_CONNECT_CREATEDEVICEID_API_LATEST;
    DeviceOptions.DeviceModel = "AndroidDevice";
    EOS_Connect_CreateDeviceId(ConnectHandle, &DeviceOptions, ctx, ConnectCreateDeviceIdCallback);
}

JNIEXPORT jstring JNICALL
Java_com_example_eos_EosBridge_nativeGetLocalProductUserId(JNIEnv* env, jobject thiz) {
    if (g_LocalProductUserIdStr.empty()) return nullptr;
    return env->NewStringUTF(g_LocalProductUserIdStr.c_str());
}

// ── P2P CONNECTION HANDLING & AUTO-ACCEPT ──

static void EOS_CALL OnIncomingPeerConnectionRequest(const EOS_P2P_OnIncomingConnectionRequestInfo* Data) {
    if (g_PlatformHandle == nullptr || Data == nullptr) return;
    EOS_HP2P P2pHandle = EOS_Platform_GetP2PInterface(g_PlatformHandle);
    if (P2pHandle == nullptr) return;

    EOS_P2P_AcceptConnectionOptions AcceptOptions = {};
    AcceptOptions.ApiVersion = EOS_P2P_ACCEPTCONNECTION_API_LATEST;
    AcceptOptions.LocalUserId = Data->LocalUserId;
    AcceptOptions.RemoteUserId = Data->RemoteUserId;
    AcceptOptions.SocketId = Data->SocketId;

    EOS_EResult Res = EOS_P2P_AcceptConnection(P2pHandle, &AcceptOptions);
    LOGI("Auto-accepted P2P connection from remote user, result: %d", (int)Res);
}

JNIEXPORT void JNICALL
Java_com_example_eos_EosBridge_nativeSetupP2pNotification(JNIEnv* env, jobject thiz) {
    if (g_PlatformHandle == nullptr || g_LocalProductUserId == nullptr) return;
    EOS_HP2P P2pHandle = EOS_Platform_GetP2PInterface(g_PlatformHandle);
    if (P2pHandle == nullptr) return;

    if (g_P2pNotificationId != EOS_INVALID_NOTIFICATIONID) {
        EOS_P2P_RemoveNotifyPeerConnectionRequest(P2pHandle, g_P2pNotificationId);
        g_P2pNotificationId = EOS_INVALID_NOTIFICATIONID;
    }

    EOS_P2P_AddNotifyPeerConnectionRequestOptions Opts = {};
    Opts.ApiVersion = EOS_P2P_ADDNOTIFYPEERCONNECTIONREQUEST_API_LATEST;
    Opts.LocalUserId = g_LocalProductUserId;
    Opts.SocketId = nullptr;

    g_P2pNotificationId = EOS_P2P_AddNotifyPeerConnectionRequest(P2pHandle, &Opts, nullptr, OnIncomingPeerConnectionRequest);
    LOGI("P2P Notification handler registered, ID: %llu", (unsigned long long)g_P2pNotificationId);
}

// ── P2P PACKET STREAMING ──

JNIEXPORT jboolean JNICALL
Java_com_example_eos_EosBridge_nativeSendPacket(
    JNIEnv* env, jobject thiz,
    jstring targetPuid, jstring socketName, jbyteArray data) {

    if (g_PlatformHandle == nullptr || g_LocalProductUserId == nullptr) {
        return JNI_FALSE;
    }

    EOS_HP2P P2pHandle = EOS_Platform_GetP2PInterface(g_PlatformHandle);
    if (P2pHandle == nullptr) return JNI_FALSE;

    const char* targetStr = env->GetStringUTFChars(targetPuid, nullptr);
    EOS_ProductUserId targetUserId = EOS_ProductUserId_FromString(targetStr);
    env->ReleaseStringUTFChars(targetPuid, targetStr);

    if (!EOS_ProductUserId_IsValid(targetUserId)) {
        return JNI_FALSE;
    }

    const char* socketStr = env->GetStringUTFChars(socketName, nullptr);
    EOS_P2P_SocketId SocketId = {};
    SocketId.ApiVersion = EOS_P2P_SOCKETID_API_LATEST;
    strncpy(SocketId.SocketName, socketStr, sizeof(SocketId.SocketName) - 1);
    env->ReleaseStringUTFChars(socketName, socketStr);

    jsize dataLen = env->GetArrayLength(data);
    std::vector<uint8_t> buffer(dataLen);
    env->GetByteArrayRegion(data, 0, dataLen, reinterpret_cast<jbyte*>(buffer.data()));

    EOS_P2P_SendPacketOptions SendOptions = {};
    SendOptions.ApiVersion = EOS_P2P_SENDPACKET_API_LATEST;
    SendOptions.LocalUserId = g_LocalProductUserId;
    SendOptions.RemoteUserId = targetUserId;
    SendOptions.SocketId = &SocketId;
    SendOptions.Channel = 0;
    SendOptions.DataLengthBytes = (uint32_t)dataLen;
    SendOptions.Data = buffer.data();
    SendOptions.bAllowDelayedDelivery = EOS_TRUE;
    SendOptions.Reliability = EOS_EPacketReliability::EOS_PR_ReliableOrdered;
    SendOptions.bDisableAutoAcceptConnection = EOS_FALSE;

    EOS_EResult Result = EOS_P2P_SendPacket(P2pHandle, &SendOptions);
    return (Result == EOS_EResult::EOS_Success) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_eos_EosBridge_nativeReceivePacket(
    JNIEnv* env, jobject thiz, jstring socketName) {

    if (g_PlatformHandle == nullptr || g_LocalProductUserId == nullptr) {
        return nullptr;
    }

    EOS_HP2P P2pHandle = EOS_Platform_GetP2PInterface(g_PlatformHandle);
    if (P2pHandle == nullptr) return nullptr;

    const char* socketStr = env->GetStringUTFChars(socketName, nullptr);
    EOS_P2P_SocketId SocketId = {};
    SocketId.ApiVersion = EOS_P2P_SOCKETID_API_LATEST;
    strncpy(SocketId.SocketName, socketStr, sizeof(SocketId.SocketName) - 1);
    env->ReleaseStringUTFChars(socketName, socketStr);

    EOS_P2P_GetNextReceivedPacketSizeOptions SizeOptions = {};
    SizeOptions.ApiVersion = EOS_P2P_GETNEXTRECEIVEDPACKETSIZE_API_LATEST;
    SizeOptions.LocalUserId = g_LocalProductUserId;
    uint8_t channel = 0;
    SizeOptions.RequestedChannel = &channel;

    uint32_t packetSize = 0;
    EOS_EResult Result = EOS_P2P_GetNextReceivedPacketSize(P2pHandle, &SizeOptions, &packetSize);
    if (Result != EOS_EResult::EOS_Success || packetSize == 0) {
        return nullptr;
    }

    std::vector<uint8_t> buffer(packetSize);
    EOS_ProductUserId remotePeerId = nullptr;
    uint32_t bytesWritten = 0;

    EOS_P2P_ReceivePacketOptions ReceiveOptions = {};
    ReceiveOptions.ApiVersion = EOS_P2P_RECEIVEPACKET_API_LATEST;
    ReceiveOptions.LocalUserId = g_LocalProductUserId;
    ReceiveOptions.MaxDataSizeBytes = packetSize;
    ReceiveOptions.RequestedChannel = &channel;

    Result = EOS_P2P_ReceivePacket(P2pHandle, &ReceiveOptions, &remotePeerId, &SocketId, &channel, buffer.data(), &bytesWritten);
    if (Result == EOS_EResult::EOS_Success && bytesWritten > 0) {
        jbyteArray arr = env->NewByteArray(bytesWritten);
        env->SetByteArrayRegion(arr, 0, bytesWritten, reinterpret_cast<jbyte*>(buffer.data()));
        return arr;
    }

    return nullptr;
}

JNIEXPORT jboolean JNICALL
Java_com_example_eos_EosBridge_nativeAcceptConnection(
    JNIEnv* env, jobject thiz, jstring remotePuid, jstring socketName) {

    if (g_PlatformHandle == nullptr || g_LocalProductUserId == nullptr) {
        return JNI_FALSE;
    }

    EOS_HP2P P2pHandle = EOS_Platform_GetP2PInterface(g_PlatformHandle);
    if (P2pHandle == nullptr) return JNI_FALSE;

    const char* targetStr = env->GetStringUTFChars(remotePuid, nullptr);
    EOS_ProductUserId targetUserId = EOS_ProductUserId_FromString(targetStr);
    env->ReleaseStringUTFChars(remotePuid, targetStr);

    const char* socketStr = env->GetStringUTFChars(socketName, nullptr);
    EOS_P2P_SocketId SocketId = {};
    SocketId.ApiVersion = EOS_P2P_SOCKETID_API_LATEST;
    strncpy(SocketId.SocketName, socketStr, sizeof(SocketId.SocketName) - 1);
    env->ReleaseStringUTFChars(socketName, socketStr);

    EOS_P2P_AcceptConnectionOptions AcceptOptions = {};
    AcceptOptions.ApiVersion = EOS_P2P_ACCEPTCONNECTION_API_LATEST;
    AcceptOptions.LocalUserId = g_LocalProductUserId;
    AcceptOptions.RemoteUserId = targetUserId;
    AcceptOptions.SocketId = &SocketId;

    EOS_EResult Result = EOS_P2P_AcceptConnection(P2pHandle, &AcceptOptions);
    return (Result == EOS_EResult::EOS_Success) ? JNI_TRUE : JNI_FALSE;
}

// ── REAL EOS LOBBY CLOUD API ──

struct LobbyContext {
    jobject callbackRef;
    std::string roomName;
    int bet;
};

static void EOS_CALL OnCreateLobbyCallback(const EOS_Lobby_CreateLobbyCallbackInfo* Data) {
    LobbyContext* ctx = static_cast<LobbyContext*>(Data->ClientData);
    JNIEnv* env = GetEnv();

    bool success = (Data->ResultCode == EOS_EResult::EOS_Success);
    std::string lobbyId = Data->LobbyId ? Data->LobbyId : "";
    LOGI("EOS_Lobby_CreateLobby finished: result=%d, lobbyId=%s", (int)Data->ResultCode, lobbyId.c_str());

    if (ctx && ctx->callbackRef && env) {
        jclass cbClass = env->GetObjectClass(ctx->callbackRef);
        jmethodID methodId = env->GetMethodID(cbClass, "onLobbyResult", "(ZLjava/lang/String;)V");
        if (methodId) {
            jstring idJStr = lobbyId.empty() ? nullptr : env->NewStringUTF(lobbyId.c_str());
            env->CallVoidMethod(ctx->callbackRef, methodId, success ? JNI_TRUE : JNI_FALSE, idJStr);
            if (idJStr) env->DeleteLocalRef(idJStr);
        }
        env->DeleteGlobalRef(ctx->callbackRef);
    }
    delete ctx;
}

JNIEXPORT void JNICALL
Java_com_example_eos_EosBridge_nativeCreateLobby(
    JNIEnv* env, jobject thiz,
    jstring roomName, jint bet, jboolean isPrivate, jobject callback) {

    if (g_PlatformHandle == nullptr || g_LocalProductUserId == nullptr) {
        LOGE("Cannot create lobby: platform or local user is null");
        return;
    }

    EOS_HLobby LobbyHandle = EOS_Platform_GetLobbyInterface(g_PlatformHandle);
    if (LobbyHandle == nullptr) return;

    LobbyContext* ctx = new LobbyContext();
    ctx->callbackRef = env->NewGlobalRef(callback);
    ctx->bet = bet;

    EOS_Lobby_CreateLobbyOptions CreateOptions = {};
    CreateOptions.ApiVersion = EOS_LOBBY_CREATELOBBY_API_LATEST;
    CreateOptions.LocalUserId = g_LocalProductUserId;
    CreateOptions.MaxLobbyMembers = 2;
    CreateOptions.PermissionLevel = isPrivate ? EOS_ELobbyPermissionLevel::EOS_LPL_INVITEONLY : EOS_ELobbyPermissionLevel::EOS_LPL_PUBLICADVERTISED;
    CreateOptions.bPresenceEnabled = EOS_FALSE;
    CreateOptions.bAllowInvites = EOS_TRUE;
    CreateOptions.BucketId = "TetrisLobby";
    CreateOptions.bDisableHostMigration = EOS_TRUE;
    CreateOptions.bEnableRTCRoom = EOS_FALSE;

    EOS_Lobby_CreateLobby(LobbyHandle, &CreateOptions, ctx, OnCreateLobbyCallback);
}

static void EOS_CALL OnJoinLobbyCallback(const EOS_Lobby_JoinLobbyByIdCallbackInfo* Data) {
    LobbyContext* ctx = static_cast<LobbyContext*>(Data->ClientData);
    JNIEnv* env = GetEnv();

    bool success = (Data->ResultCode == EOS_EResult::EOS_Success);
    std::string lobbyId = Data->LobbyId ? Data->LobbyId : "";
    LOGI("EOS_Lobby_JoinLobbyById finished: result=%d, lobbyId=%s", (int)Data->ResultCode, lobbyId.c_str());

    if (ctx && ctx->callbackRef && env) {
        jclass cbClass = env->GetObjectClass(ctx->callbackRef);
        jmethodID methodId = env->GetMethodID(cbClass, "onLobbyResult", "(ZLjava/lang/String;)V");
        if (methodId) {
            jstring idJStr = lobbyId.empty() ? nullptr : env->NewStringUTF(lobbyId.c_str());
            env->CallVoidMethod(ctx->callbackRef, methodId, success ? JNI_TRUE : JNI_FALSE, idJStr);
            if (idJStr) env->DeleteLocalRef(idJStr);
        }
        env->DeleteGlobalRef(ctx->callbackRef);
    }
    delete ctx;
}

JNIEXPORT void JNICALL
Java_com_example_eos_EosBridge_nativeJoinLobby(
    JNIEnv* env, jobject thiz, jstring lobbyId, jobject callback) {

    if (g_PlatformHandle == nullptr || g_LocalProductUserId == nullptr) return;
    EOS_HLobby LobbyHandle = EOS_Platform_GetLobbyInterface(g_PlatformHandle);
    if (LobbyHandle == nullptr) return;

    const char* idChars = env->GetStringUTFChars(lobbyId, nullptr);
    std::string idStr = idChars;
    env->ReleaseStringUTFChars(lobbyId, idChars);

    LobbyContext* ctx = new LobbyContext();
    ctx->callbackRef = env->NewGlobalRef(callback);

    EOS_Lobby_JoinLobbyByIdOptions JoinOptions = {};
    JoinOptions.ApiVersion = EOS_LOBBY_JOINLOBBYBYID_API_LATEST;
    JoinOptions.LobbyId = idStr.c_str();
    JoinOptions.LocalUserId = g_LocalProductUserId;
    JoinOptions.bPresenceEnabled = EOS_FALSE;

    EOS_Lobby_JoinLobbyById(LobbyHandle, &JoinOptions, ctx, OnJoinLobbyCallback);
}

static void EOS_CALL OnLeaveLobbyCallback(const EOS_Lobby_LeaveLobbyCallbackInfo* Data) {
    LOGI("EOS_Lobby_LeaveLobby finished: result=%d", (int)Data->ResultCode);
}

JNIEXPORT void JNICALL
Java_com_example_eos_EosBridge_nativeLeaveLobby(JNIEnv* env, jobject thiz, jstring lobbyId) {
    if (g_PlatformHandle == nullptr || g_LocalProductUserId == nullptr) return;
    EOS_HLobby LobbyHandle = EOS_Platform_GetLobbyInterface(g_PlatformHandle);
    if (LobbyHandle == nullptr) return;

    const char* idChars = env->GetStringUTFChars(lobbyId, nullptr);
    std::string idStr = idChars;
    env->ReleaseStringUTFChars(lobbyId, idChars);

    EOS_Lobby_LeaveLobbyOptions LeaveOptions = {};
    LeaveOptions.ApiVersion = EOS_LOBBY_LEAVELOBBY_API_LATEST;
    LeaveOptions.LobbyId = idStr.c_str();
    LeaveOptions.LocalUserId = g_LocalProductUserId;

    EOS_Lobby_LeaveLobby(LobbyHandle, &LeaveOptions, nullptr, OnLeaveLobbyCallback);
}

} // extern "C"
