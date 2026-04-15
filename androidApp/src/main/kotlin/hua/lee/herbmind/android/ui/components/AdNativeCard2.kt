package hua.lee.herbmind.android.ui.components

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import hua.lee.herbmind.android.R
import hua.lee.herbmind.domain.ad.AdMobAdapter
import hua.lee.herbmind.domain.ad.model.NativeAdData

/**
 * 新版原生广告卡片，使用AdMob官方NativeAdView绑定，支持点击跳转
 * 从全局缓存获取广告实例，进入页面时统一加载，退出时自动清理
 */
@Composable
fun AdNativeCard2(
    ad: NativeAdData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nativeAd = remember(ad.adId) { AdMobAdapter.globalNativeAds[ad.adId] }

    nativeAd?.let { adInstance ->
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            factory = {
                // 加载官方NativeAdView布局
                val view = View.inflate(context, R.layout.native_ad_card, null) as NativeAdView
                bindNativeAd(view, adInstance, onClose)
                view
            },
            update = { view ->
                bindNativeAd(view as NativeAdView, adInstance, onClose)
            }
        )
    }
}

/**
 * 将NativeAd数据绑定到NativeAdView
 */
private fun bindNativeAd(adView: NativeAdView, nativeAd: NativeAd, onClose: () -> Unit) {
    // 设置标题
    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    headlineView.text = nativeAd.headline
    adView.headlineView = headlineView

    // 设置正文
    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    bodyView.text = nativeAd.body
    adView.bodyView = bodyView

    // 设置广告主
    val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
    advertiserView.text = nativeAd.advertiser
    adView.advertiserView = advertiserView

    // 设置图标
    val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
    nativeAd.icon?.let { icon ->
        iconView.setImageDrawable(icon.drawable)
        adView.iconView = iconView
    }

    // 设置操作按钮
    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)
    ctaView.text = nativeAd.callToAction
    adView.callToActionView = ctaView

    // 设置关闭按钮
    val closeView = adView.findViewById<ImageView>(R.id.ad_close)
    closeView.setOnClickListener { onClose() }

    // 绑定广告到View
    adView.setNativeAd(nativeAd)
}
