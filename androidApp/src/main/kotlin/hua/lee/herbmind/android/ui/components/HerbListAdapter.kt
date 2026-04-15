package hua.lee.herbmind.android.ui.components

import android.util.Log
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
import hua.lee.herbmind.android.ui.components.AdNativeCard2

/**
 * 每页药材数量（不含广告）
 */
private const val HERBS_PER_PAGE = 7

/**
 * 列表项密封类，支持药材项和广告项
 */
sealed class ListItem {
    data class HerbItem(val herb: Herb) : ListItem()
    data class SearchResultItem(val result: hua.lee.herbmind.data.model.SearchResult) : ListItem()
    data class AdItem(val ad: NativeAdData, val pageIndex: Int, val indexInPage: Int) : ListItem()
}

/**
 * 药材列表适配器，支持广告和药材的混合展示
 * 按页组织：每页 7 条药材 + 1 条广告（插入在页末）
 */
object HerbListAdapter {

    /**
     * 插入广告到药材列表中，每7条药材的第4位插入1条广告
     * @param herbs 原始药材列表
     * @param ads 可用的广告列表
     * @param hiddenAdIds 被用户关闭的广告 ID 集合（这些广告会被过滤掉）
     * @return 混合后的列表项
     */
    fun insertAds(
        herbs: List<Herb>,
        ads: List<NativeAdData>,
        hiddenAdIds: Set<String> = emptySet()
    ): List<ListItem> {
        val items = mutableListOf<ListItem>()
        var pageIndex = 0
        var adIndex = 0
        var indexInPage = 0

        // 获取可用的广告（未隐藏）
        val availableAds = ads.filter { it.adId !in hiddenAdIds }

        herbs.forEach { herb ->
            items.add(ListItem.HerbItem(herb))
            indexInPage++

            // 每 7 条药材后插入一条广告，插入到第 4 位（索引3）
            if (indexInPage == HERBS_PER_PAGE && adIndex < availableAds.size) {
                val ad = availableAds[adIndex % availableAds.size]
                // 插入位置：当前页的第 4 位 = 已添加的项数 - 3
                val insertPosition = items.size - 3
                items.add(insertPosition, ListItem.AdItem(ad, pageIndex, 3))
                adIndex++
                pageIndex++
                indexInPage = 0
            }
        }

        // 剩余药材不足一页但广告数 >= 3 时，在最后插入广告
        if (indexInPage > 0 && indexInPage < HERBS_PER_PAGE && availableAds.size >= 3) {
            val ad = availableAds[adIndex % availableAds.size]
            items.add(ListItem.AdItem(ad, pageIndex, indexInPage))
        }

        return items
    }

    /**
     * 插入广告到搜索结果列表中
     * 逻辑：每 7 条搜索结果，在第 4 位（索引3）插入 1 条广告
     * 如果结果不足 7 条但广告数 >= 3，也插入广告（放在最后）
     * @param searchResults 原始搜索结果列表
     * @param ads 可用的广告列表
     * @param hiddenAdIds 被用户关闭的广告 ID 集合
     * @return 混合后的列表项
     */
    fun insertAdsToSearchResults(
        searchResults: List<hua.lee.herbmind.data.model.SearchResult>,
        ads: List<NativeAdData>,
        hiddenAdIds: Set<String> = emptySet()
    ): List<ListItem> {
        if (searchResults.isEmpty()) {
            // 搜索结果为空时，如果有待显示的广告，插入一条
            val availableAd = ads.firstOrNull { it.adId !in hiddenAdIds }
            return if (availableAd != null) {
                listOf(ListItem.AdItem(availableAd, 0, 3))
            } else {
                emptyList()
            }
        }

        val items = mutableListOf<ListItem>()
        var pageIndex = 0
        var adIndex = 0
        var indexInPage = 0

        // 获取可用的广告（未隐藏）
        val availableAds = ads.filter { it.adId !in hiddenAdIds }

        searchResults.forEachIndexed { idx, result ->
            items.add(ListItem.SearchResultItem(result))
            indexInPage++

            // 每 7 条搜索结果后插入一条广告，插入到第 4 位（索引3）
            if (indexInPage == HERBS_PER_PAGE && adIndex < availableAds.size) {
                val ad = availableAds[adIndex % availableAds.size]
                // 插入位置：当前页的第 4 位 = 已添加的项数 - 3
                val insertPosition = items.size - 3
                items.add(insertPosition, ListItem.AdItem(ad, pageIndex, 3))
                adIndex++
                pageIndex++
                indexInPage = 0
            }
        }

        // 剩余搜索结果不足一页但广告数 >= 3 时，在最后插入广告
        if (indexInPage > 0 && indexInPage < HERBS_PER_PAGE && availableAds.size >= 3) {
            val ad = availableAds[adIndex % availableAds.size]
            items.add(ListItem.AdItem(ad, pageIndex, indexInPage))
        }

        return items
    }

    /**
     * 渲染混合列表到LazyColumn
     * @param items 混合列表项
     * @param onHerbClick 药材点击回调
     * @param onAdClick 广告点击回调
     * @param onAdClose 广告关闭回调，参数为完整的AdItem
     */
    fun LazyListScope.renderListItems(
        items: List<ListItem>,
        onHerbClick: (String) -> Unit,
        onAdClick: (NativeAdData) -> Unit,
        onAdClose: (ListItem.AdItem) -> Unit
    ) {
        items(
            items = items,
            key = { item ->
                when (item) {
                    is ListItem.HerbItem -> "herb_${item.herb.id}"
                    is ListItem.SearchResultItem -> "result_${item.result.herb.id}"
                    is ListItem.AdItem -> "ad_${item.pageIndex}_${item.indexInPage}_${item.ad.adId}"
                }
            }
        ) { item ->
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
                    AdNativeCard2(
                        ad = item.ad,
                        onClose = { onAdClose(item) },
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
