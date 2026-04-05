package hua.lee.herbmind.domain.ad.model

/**
 * 广告位置枚举
 * 定义应用中所有可能展示广告的位置
 */
enum class AdPosition {
    /**
     * 首页顶部横幅
     */
    HOME_TOP_BANNER,

    /**
     * 搜索结果列表中插屏
     */
    SEARCH_RESULT_NATIVE,

    /**
     * 药材详情页底部
     */
    HERB_DETAIL_BOTTOM_BANNER,

    /**
     * 分类列表插屏
     */
    CATEGORY_LIST_NATIVE,

    /**
     * 对比页面顶部
     */
    COMPARE_TOP_BANNER,

    /**
     * 学习页面插屏
     */
    STUDY_SECTION_NATIVE,

    /**
     * 启动页开屏广告
     */
    SPLASH_SCREEN
}
