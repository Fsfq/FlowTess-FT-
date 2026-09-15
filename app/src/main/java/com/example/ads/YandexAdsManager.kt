package com.example.ads

import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private var isInitialized = false
    private var rewardedAdLoader: RewardedAdLoader? = null
    private var currentRewardedAd: RewardedAd? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Инициализация Yandex Mobile Ads SDK.
     * Вызывается один раз при старте приложения (в MainActivity.onCreate или Application).
     */
    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext

        try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            cm?.registerDefaultNetworkCallback(object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    Log.i(TAG, "Network became available, auto-retrying ad load")
                    if (!_isAdLoaded.value && !_isLoading.value && isInitialized) {
                        loadRewardedAd()
                    }
                }
            })
        } catch (e: Throwable) {
            Log.w(TAG, "Could not register network callback: ${e.message}")
        }

        YandexAds.initialize(appContext) {
            Log.i(TAG, "Yandex Mobile Ads SDK successfully initialized")
            isInitialized = true
            rewardedAdLoader = RewardedAdLoader(appContext)
            loadRewardedAd()
        }
    }

    /**
     * Загрузка рекламного видео с вознаграждением.
     */
    fun loadRewardedAd() {
        if (_isLoading.value || _isAdLoaded.value) return
        val loader = rewardedAdLoader ?: return

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
        scope.launch {
            kotlinx.coroutines.delay(15000L)
            if (!_isAdLoaded.value && !_isLoading.value && isInitialized) {
                Log.i(TAG, "Auto-retrying ad load after delay...")
                loadRewardedAd()
            }
        }
    }

    /**
     * Показ видео за вознаграждение пользователю.
     *
     * @param activity Текущая Activity для отображения полноэкранного видео.
     * @param onRewarded Колбэк успешного завершения просмотра пользователем (выдача монет / ключей).
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

            var rewardGranted = false

            ad.setAdEventListener(object : RewardedAdEventListener {
                override fun onAdShown() {
                    Log.i(TAG, "Rewarded ad presented on screen")
                }

                override fun onAdFailedToShow(adError: AdError) {
                    Log.e(TAG, "Failed to show rewarded ad: ${adError.description}")
                    ad.setAdEventListener(null)
                    currentRewardedAd = null
                    _isAdLoaded.value = false
                    onError("Не удалось показать рекламу: ${adError.description}")
                    loadRewardedAd()
                }

                override fun onAdDismissed() {
                    Log.i(TAG, "Rewarded ad dismissed by user")
                    ad.setAdEventListener(null)
                    currentRewardedAd = null
                    _isAdLoaded.value = false
                    onDismissed()
                    // Автоматически подгружаем следующее видео в фоне
                    loadRewardedAd()
                }

                override fun onAdClicked() {
                    Log.i(TAG, "Rewarded ad clicked")
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    Log.i(TAG, "Rewarded ad impression logged")
                }

                override fun onRewarded(reward: Reward) {
                    Log.i(TAG, "Reward earned: amount=${reward.amount}, type=${reward.type}")
                    rewardGranted = true
                    val coinAmount = if (reward.amount > 1) reward.amount else REWARD_COIN_AMOUNT
                    onRewarded(coinAmount, "coins")
                }
            })

            ad.show(activity)
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


