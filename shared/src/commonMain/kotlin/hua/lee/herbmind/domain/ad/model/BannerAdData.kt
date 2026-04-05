package hua.lee.herbmind.domain.ad.model

/**
 * 横幅广告数据类
 * 包含横幅广告展示所需的所有字段
 */
data class BannerAdData(
    /**
     * 广告唯一标识符
     */
    val adId: String,

    /**
     * 广告宽度（像素）
     */
    val width: Int,

    /**
     * 广告高度（像素）
     */
    val height: Int,

    /**
     * 广告内容 URL
     */
    val contentUrl: String,

    /**
     * 点击跳转 URL
     */
    val clickUrl: String,

    /**
     * 广告平台来源
     */
    val adPlatform: String,

    /**
     * 广告展示位置
     */
    val position: AdPosition,

    /**
     * 是否是自适应大小
     */
    val isAdaptive: Boolean = false
)
