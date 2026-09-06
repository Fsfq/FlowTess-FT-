package com.example.ads

import kotlinx.coroutines.delay

/**
 * Менеджер рекламы Yandex Mobile Ads SDK (Заглушка / Mock).
 * Разработан для легкой интеграции реального Yandex Mobile Ads SDK
 * (com.yandex.android:mobileads) без доната, чисто за показ рекламы.
 */
object YandexAdsManager {

    /**
     * Флаг тестового режима / заглушки.
     */
    const val IS_PREVIEW_MODE = true

    // Идентификаторы рекламных блоков Yandex (заполняются из партнерского кабинета РСЯ)
    const val REWARDED_AD_UNIT_ID = "R-M-DEMO-rewarded-client"
    const val INTERSTITIAL_AD_UNIT_ID = "R-M-DEMO-interstitial-client"

    enum class RewardOfferType(
        val id: String,
        val creditReward: Int,
        val keyRewardType: String?,
        val keyRewardCount: Int,
        val durationEstimateSec: Int
    ) {
        SHORT_VIDEO("short_video", creditReward = 250, keyRewardType = null, keyRewardCount = 0, durationEstimateSec = 15),
        SUPER_AD("super_ad", creditReward = 750, keyRewardType = null, keyRewardCount = 0, durationEstimateSec = 30),
        CRATE_KEY("crate_key", creditReward = 0, keyRewardType = "iron", keyRewardCount = 1, durationEstimateSec = 30)
    }

    fun isAdReady(type: RewardOfferType): Boolean {
        return true
    }

    suspend fun simulateAdPlayback(
        type: RewardOfferType,
        onProgress: (stepText: String, progress: Float) -> Unit,
        onComplete: (RewardOfferType) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            onProgress("Загрузка...", 0.25f)
            delay(400)
            onProgress("Воспроизведение...", 0.65f)
            delay(600)
            onProgress("Завершено", 1.0f)
            delay(250)
            onComplete(type)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Ошибка показа рекламы")
        }
    }
}
