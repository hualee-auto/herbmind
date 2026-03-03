package com.herbmind.android.util

import android.content.Context

/**
 * 图片资源配置
 * 管理本地和远程图片路径
 * 
 * 半路径格式: resources/images/concocted/{name}_hkbu.jpg
 * 通过设置 baseUrl 可切换不同 CDN 源
 */
object ImageResourceConfig {
    
    // 远程基础URL（CDN）
    // 默认使用 GitHub Raw，可替换为国内 CDN
    var remoteBaseUrl: String = "https://raw.githubusercontent.com/hualee-auto/herbmind/main/"
    
    /**
     * 获取完整图片 URL
     * @param halfPath 半路径（如：resources/images/concocted/人参_hkbu.jpg）
     * @return 完整 URL，如果 halfPath 为空则返回空字符串
     */
    fun getImageUrl(halfPath: String?): String {
        if (halfPath.isNullOrEmpty()) return ""
        return remoteBaseUrl + halfPath
    }
    
    /**
     * 设置远程基础 URL
     * @param baseUrl CDN 基础 URL，必须以 / 结尾
     */
    fun setRemoteBaseUrl(baseUrl: String) {
        remoteBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    }
}
