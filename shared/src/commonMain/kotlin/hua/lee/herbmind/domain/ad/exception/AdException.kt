package hua.lee.herbmind.domain.ad.exception

import hua.lee.herbmind.domain.ad.model.AdPosition

/**
 * 广告异常密封类
 * 定义所有广告相关的异常类型
 */
sealed class AdException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /**
     * 广告平台初始化失败
     */
    class InitializationFailed(
        platform: String,
        cause: Throwable? = null
    ) : AdException("Ad platform $platform initialization failed", cause)

    /**
     * 广告加载失败
     */
    class LoadFailed(
        position: AdPosition,
        errorCode: Int,
        message: String,
        cause: Throwable? = null
    ) : AdException("Ad load failed for position $position: $message (code: $errorCode)", cause)

    /**
     * 广告展示失败
     */
    class ShowFailed(
        position: AdPosition,
        message: String,
        cause: Throwable? = null
    ) : AdException("Ad show failed for position $position: $message", cause)

    /**
     * 广告点击失败
     */
    class ClickFailed(
        position: AdPosition,
        message: String,
        cause: Throwable? = null
    ) : AdException("Ad click failed for position $position: $message", cause)

    /**
     * 广告配置错误
     */
    class ConfigurationError(
        message: String,
        cause: Throwable? = null
    ) : AdException("Ad configuration error: $message", cause)

    /**
     * 无可用广告
     */
    class NoFill(
        position: AdPosition,
        cause: Throwable? = null
    ) : AdException("No ad fill available for position $position", cause)

    /**
     * 网络错误
     */
    class NetworkError(
        position: AdPosition,
        cause: Throwable? = null
    ) : AdException("Network error while loading ad for position $position", cause)

    /**
     * 超时错误
     */
    class TimeoutError(
        position: AdPosition,
        timeoutMs: Long,
        cause: Throwable? = null
    ) : AdException("Ad load timeout for position $position after ${timeoutMs}ms", cause)

    /**
     * 未知错误
     */
    class UnknownError(
        message: String = "Unknown ad error",
        cause: Throwable? = null
    ) : AdException(message, cause)

    /**
     * 频率限制达到
     */
    class FrequencyLimitReached(
        message: String = "Ad frequency limit reached",
        cause: Throwable? = null
    ) : AdException(message, cause)

    /**
     * 所有广告平台都加载失败
     */
    class AllPlatformsFailed(
        message: String = "All ad platforms failed to load ad",
        override val cause: Throwable? = null,
        val suppressedExceptions: List<Throwable> = emptyList()
    ) : AdException(message, cause)
}
