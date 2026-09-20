package com.example.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.YandexAds
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadResult
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.math.pow

/**
 * Менеджер рекламы Yandex Mobile Ads SDK (РСЯ).
 * Обеспечивает инициализацию SDK, предварительную загрузку видео за вознаграждение (Rewarded Ad),
 * управление состоянием готовности и безопасный показ на главном потоке.
 */
object YandexAdsManager {

    private const val TAG = "YandexAdsManager"

    /**
     * Идентификатор рекламного блока РСЯ для видео с вознаграждением.
     */
    const val REWARDED_AD_UNIT_ID = "R-M-19694497-1"
    const val DEMO_AD_UNIT_ID = "demo-rewarded-yandex"
    const val REWARD_COIN_AMOUNT = 300

    private val isInitializing = AtomicBoolean(false)
    private var isInitialized = false
    private var appContext: Context? = null
    private var rewardedAdLoader: RewardedAdLoader? = null
    private var currentRewardedAd: RewardedAd? = null

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var retryJob: Job? = null
    private var retryAttempt = 0
    private const val MAX_RETRY_ATTEMPTS = 4

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Инициализация Yandex Mobile Ads SDK.
     * Вызывается при старте приложения (в MainActivity.onCreate или Application).
     */
    fun init(context: Context) {
        if (isInitialized) return
        if (!isInitializing.compareAndSet(false, true)) return

        val appCtx = context.applicationContext
        appContext = appCtx

        registerNetworkCallback(appCtx)

        YandexAds.initialize(appCtx) {
            Log.i(TAG, "Yandex Mobile Ads SDK successfully initialized")
            isInitialized = true
            isInitializing.set(false)
            rewardedAdLoader = RewardedAdLoader(appCtx)
            retryAttempt = 0
            loadRewardedAd()
        }
    }

    private fun registerNetworkCallback(context: Context) {
        if (networkCallback != null) return
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.i(TAG, "Network became available, auto-retrying ad load / init")
                    if (!isInitialized) {
                        appContext?.let { init(it) }
                    } else if (!_isAdLoaded.value && !_isLoading.value) {
                        retryAttempt = 0
                        loadRewardedAd()
                    }
                }
            }
            cm?.registerDefaultNetworkCallback(callback)
            networkCallback = callback
        } catch (e: Throwable) {
            Log.w(TAG, "Could not register network callback: ${e.message}")
        }
    }

    /**
     * Загрузка рекламного видео с вознаграждением.
     */
    fun loadRewardedAd() {
        if (_isLoading.value || _isAdLoaded.value) return
        val loader = rewardedAdLoader ?: return

        retryJob?.cancel()
        _isLoading.value = true
        Log.i(TAG, "Requesting rewarded ad with unit ID: $REWARDED_AD_UNIT_ID")

        scope.launch {
            try {
                val adRequest = AdRequest.Builder(REWARDED_AD_UNIT_ID).build()
                var result = loader.loadAd(adRequest)
                if (result is RewardedAdLoadResult.Failure && REWARDED_AD_UNIT_ID != DEMO_AD_UNIT_ID) {
                    Log.w(TAG, "Ad unit $REWARDED_AD_UNIT_ID failed (${result.error.description}), trying demo fallback: $DEMO_AD_UNIT_ID")
                    result = loader.loadAd(AdRequest.Builder(DEMO_AD_UNIT_ID).build())
                }
                when (result) {
                    is RewardedAdLoadResult.Success -> {
                        Log.i(TAG, "Rewarded ad successfully loaded")
                        currentRewardedAd = result.ad
                        _isAdLoaded.value = true
                        _isLoading.value = false
                        retryAttempt = 0
                    }
                    is RewardedAdLoadResult.Failure -> {
                        Log.w(TAG, "Failed to load rewarded ad: ${result.error.description}")
                        currentRewardedAd = null
                        _isAdLoaded.value = false
                        _isLoading.value = false
                        scheduleRetry()
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Exception loading rewarded ad: ${e.message}", e)
                currentRewardedAd = null
                _isAdLoaded.value = false
                _isLoading.value = false
                scheduleRetry()
            }
        }
    }

    private fun scheduleRetry() {
        if (retryAttempt >= MAX_RETRY_ATTEMPTS) {
            Log.w(TAG, "Max ad load retry attempts ($MAX_RETRY_ATTEMPTS) reached. Waiting for next user/network trigger.")
            return
        }
        val delayMillis = min(60000L, (2000.0 * 2.0.pow(retryAttempt.toDouble())).toLong())
        retryAttempt++
        Log.i(TAG, "Scheduling ad load retry #$retryAttempt in ${delayMillis}ms")

        retryJob?.cancel()
        retryJob = scope.launch {
            delay(delayMillis)
            if (!_isAdLoaded.value && !_isLoading.value && isInitialized) {
                loadRewardedAd()
            }
        }
    }

    /**
     * Показ видео за вознаграждение пользователю.
     *
     * @param activity Текущая Activity для отображения полноэкранного видео.
     * @param onRewarded Колбэк успешного завершения просмотра пользователем.
     * @param onDismissed Колбэк закрытия диалога рекламы.
     * @param onError Колбэк в случае сбоя или отсутствия готовой рекламы.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        runOnMainThread {
            val ad = currentRewardedAd
            if (ad == null) {
                Log.w(TAG, "Cannot show ad: no ad loaded")
                onError("Реклама ещё загружается. Пожалуйста, подождите...")
                loadRewardedAd()
                return@runOnMainThread
            }

            // CRIT-ADS-01: Сразу зануляем текущую ссылку в синглтоне перед показом
            currentRewardedAd = null
            _isAdLoaded.value = false

            // CRIT-ADS-02: Валидация жизненного цикла Activity
            if (activity.isFinishing || activity.isDestroyed) {
                Log.w(TAG, "Cannot show ad: Activity is finishing or destroyed")
                ad.setAdEventListener(null)
                onError("Activity is no longer valid")
                return@runOnMainThread
            }

            var rewardGranted = false

            ad.setAdEventListener(object : RewardedAdEventListener {
                override fun onAdShown() {
                    Log.i(TAG, "Rewarded ad presented on screen")
                }

                override fun onAdFailedToShow(adError: AdError) {
                    Log.e(TAG, "Failed to show rewarded ad: ${adError.description}")
                    ad.setAdEventListener(null)
                    onError("Не удалось показать рекламу: ${adError.description}")
                    loadRewardedAd()
                }

                override fun onAdDismissed() {
                    Log.i(TAG, "Rewarded ad dismissed by user")
                    ad.setAdEventListener(null)
                    onDismissed()
                    loadRewardedAd()
                }

                override fun onAdClicked() {
                    Log.i(TAG, "Rewarded ad clicked")
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    Log.i(TAG, "Rewarded ad impression logged")
                }

                override fun onRewarded(reward: Reward) {
                    // MAJ-ADS-03: Защита от повторной выдачи наград
                    if (rewardGranted) return
                    rewardGranted = true
                    Log.i(TAG, "Reward earned: amount=${reward.amount}, type=${reward.type}")
                    val coinAmount = if (reward.amount > 1) reward.amount else REWARD_COIN_AMOUNT
                    onRewarded(coinAmount, "coins")
                }
            })

            // CRIT-ADS-02: try-catch вокруг ad.show
            try {
                ad.show(activity)
            } catch (t: Throwable) {
                Log.e(TAG, "Exception during ad.show(): ${t.message}", t)
                ad.setAdEventListener(null)
                onError(t.message ?: "Failed to show ad")
                loadRewardedAd()
            }
        }
    }

    /**
     * Очистка слушателей и освобождение сильных ссылок при уничтожении Activity.
     * [CRIT-ADS-01]
     */
    fun clearListeners() {
        runOnMainThread {
            currentRewardedAd?.setAdEventListener(null)
            currentRewardedAd = null
            _isAdLoaded.value = false
            retryJob?.cancel()
            retryJob = null
        }
    }

    private fun runOnMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}


