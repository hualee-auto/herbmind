package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.android.ui.theme.HerbColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "本草记",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = HerbColors.InkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HerbColors.RicePaper
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(HerbColors.RicePaper)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 搜索框
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SearchBar()
            }

            // 热门功效
            item {
                HotEffectsSection()
            }

            // 今日推荐
            item {
                SectionTitle("今日推荐")
            }

            items(3) { index ->
                when (index) {
                    0 -> DailyRecommendCard(
                        name = "当归",
                        effects = "补血活血 · 调经止痛",
                        tag = "妇科圣药"
                    )
                    1 -> DailyRecommendCard(
                        name = "黄芪",
                        effects = "补气升阳 · 益卫固表",
                        tag = "补气诸药之最"
                    )
                    2 -> DailyRecommendCard(
                        name = "金银花",
                        effects = "清热解毒 · 疏散风热",
                        tag = "疮家圣药"
                    )
                }
            }

            // 分类浏览
            item {
                SectionTitle("浏览中药库")
            }

            item {
                CategoryGrid()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SearchBar() {
    Card(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = HerbColors.InkLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "输入功效，查找中药...",
                fontSize = 16.sp,
                color = HerbColors.InkLight
            )
        }
    }
}

@Composable
private fun HotEffectsSection() {
    Column {
        Text(
            text = "热门功效",
            fontSize = 14.sp,
            color = HerbColors.InkGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        val effects = listOf("补气", "活血", "清热", "安神", "解毒", "止痛")
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            effects.forEach { effect ->
                EffectTag(text = effect)
            }
        }
    }
}

@Composable
private fun EffectTag(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = HerbColors.BambooGreenPale
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = HerbColors.BambooGreenDark
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            modifier = Modifier.weight(1f),
            color = HerbColors.BorderPale,
            thickness = 1.dp
        )
    }
}

@Composable
private fun DailyRecommendCard(
    name: String,
    effects: String,
    tag: String
) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HerbColors.BambooGreenPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌿",
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                    Text(
                        text = effects,
                        fontSize = 14.sp,
                        color = HerbColors.InkGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = HerbColors.BorderPale, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = tag,
                fontSize = 14.sp,
                color = HerbColors.Ochre
            )
        }
    }
}

@Composable
private fun CategoryGrid() {
    val categories = listOf(
        "解表药" to "🌡️",
        "清热药" to "🔥",
        "补虚药" to "💊",
        "理气药" to "🌿",
        "活血化瘀" to "💉",
        "安神药" to "😴"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (name, icon) ->
                    CategoryCard(
                        name = name,
                        icon = icon,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    name: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, HerbColors.BorderPale)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )
        }
    }
}
