package hua.lee.herbmind.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hua.lee.herbmind.android.ui.theme.HerbColors
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.domain.ad.model.NativeAdData

/**
 * 列表项密封类，支持药材项和广告项
 */
sealed class ListItem {
    data class HerbItem(val herb: Herb) : ListItem()
    data class SearchResultItem(val result: hua.lee.herbmind.data.model.SearchResult) : ListItem()
    data class AdItem(val ad: NativeAdData, val position: Int) : ListItem()
}

/**
 * 药材列表适配器，支持广告和药材的混合展示
 */
object HerbListAdapter {

    /**
     * 插入广告到药材列表中，每6条药材插入1条广告
     * @param herbs 原始药材列表
     * @param ads 可用的广告列表
     * @return 混合后的列表项
     */
    fun insertAds(herbs: List<Herb>, ads: List<NativeAdData>): List<ListItem> {
        val items = mutableListOf<ListItem>()
        var adIndex = 0

        herbs.forEachIndexed { index, herb ->
            items.add(ListItem.HerbItem(herb))
            
            // 每6条药材后插入一条广告，如果还有可用广告
            if ((index + 1) % 6 == 0 && adIndex < ads.size) {
                items.add(ListItem.AdItem(ads[adIndex], index + 1))
                adIndex++
            }
        }

        return items
    }

    /**
     * 插入广告到搜索结果列表中，每6条结果插入1条广告
     * @param searchResults 原始搜索结果列表
     * @param ads 可用的广告列表
     * @return 混合后的列表项
     */
    fun insertAdsToSearchResults(
        searchResults: List<hua.lee.herbmind.data.model.SearchResult>, 
        ads: List<NativeAdData>
    ): List<ListItem> {
        val items = mutableListOf<ListItem>()
        var adIndex = 0

        searchResults.forEachIndexed { index, result ->
            items.add(ListItem.SearchResultItem(result))
            
            // 每6条结果后插入一条广告，如果还有可用广告
            if ((index + 1) % 6 == 0 && adIndex < ads.size) {
                items.add(ListItem.AdItem(ads[adIndex], index + 1))
                adIndex++
            }
        }

        return items
    }

    /**
     * 渲染混合列表到LazyColumn
     * @param items 混合列表项
     * @param onHerbClick 药材点击回调
     * @param onAdClick 广告点击回调
     * @param onAdClose 广告关闭回调
     * @param onAdImpression 广告曝光回调
     */
    fun LazyListScope.renderListItems(
        items: List<ListItem>,
        onHerbClick: (String) -> Unit,
        onAdClick: (NativeAdData) -> Unit,
        onAdClose: (NativeAdData) -> Unit,
        onAdImpression: (NativeAdData) -> Unit
    ) {
        items(items) { item ->
            when (item) {
                is ListItem.HerbItem -> {
                    HerbListItem(
                        herb = item.herb,
                        onClick = { onHerbClick(item.herb.id) }
                    )
                }
                is ListItem.SearchResultItem -> {
                    SearchResultItem(
                        result = item.result,
                        onClick = { onHerbClick(item.result.herb.id) }
                    )
                }
                is ListItem.AdItem -> {
                    AdNativeCard(
                        ad = item.ad,
                        onClick = { onAdClick(item.ad) },
                        onClose = { onAdClose(item.ad) },
                        onImpression = { onAdImpression(item.ad) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 搜索结果项组件（从SearchScreen.kt迁移过来）
 */
@Composable
private fun SearchResultItem(
    result: hua.lee.herbmind.data.model.SearchResult,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(
                    text = result.herb.name,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                if (result.score > 0) {
                    androidx.compose.material3.Text(
                        text = "${result.score}分",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        modifier = androidx.compose.ui.Modifier.padding(start = 8.dp)
                    )
                }
            }

            androidx.compose.material3.Text(
                text = result.herb.pinyin,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 类别标签
            if (result.herb.category.isNotEmpty()) {
                androidx.compose.material3.Text(
                    text = result.herb.category,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = androidx.compose.ui.Modifier.padding(top = 4.dp)
                )
            }

            // 功效
            if (result.herb.effects.isNotEmpty()) {
                androidx.compose.material3.Text(
                    text = result.herb.effects.take(3).joinToString("、"),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = androidx.compose.ui.Modifier.padding(top = 4.dp),
                    maxLines = 1
                )
            }

            // 匹配的功效
            if (result.matchedEffects.isNotEmpty()) {
                androidx.compose.material3.Text(
                    text = "匹配: ${result.matchedEffects.joinToString("、")}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = hua.lee.herbmind.android.ui.theme.HerbColors.BambooGreen,
                    modifier = androidx.compose.ui.Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
