package hua.lee.herbmind.domain.ad.model

/**
 * 原生广告数据类
 * 包含原生广告展示所需的所有字段
 */
data class NativeAdData(
    /**
     * 广告唯一标识符
     */
    val adId: String,

    /**
     * 广告标题
     */
    val title: String,

    /**
     * 广告正文内容
     */
    val body: String,

    /**
     * 广告主名称
     */
    val advertiser: String,

    /**
     * 广告图标 URL
     */
    val iconUrl: String?,

    /**
     * 广告主图 URL
     */
    val imageUrl: String?,

    /**
     * 价格信息（如"¥0.99"）
     */
    val price: String?,

    /**
     * 星级评分（0-5）
     */
    val starRating: Double?,

    /**
     * 商店名称（如"Google Play"）
     */
    val store: String?,

    /**
     * 操作按钮文本（如"安装"、"了解更多"）
     */
    val callToAction: String,

    /**
     * 广告平台来源
     */
    val adPlatform: String,

    /**
     * 广告展示位置
     */
    val position: AdPosition
)
