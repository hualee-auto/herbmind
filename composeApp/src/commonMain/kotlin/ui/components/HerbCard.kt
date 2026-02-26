package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult
import ui.theme.HerbColors

@Composable
fun HerbCard(
    herb: Herb,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标区域
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HerbColors.BambooGreenPale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = null,
                    tint = HerbColors.BambooGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 内容区域
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = herb.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.InkBlack
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = herb.effects.take(2).joinToString(" · "),
                    fontSize = 14.sp,
                    color = HerbColors.InkGray
                )

                herb.keyPoint?.let { keyPoint ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "[$keyPoint]",
                        fontSize = 12.sp,
                        color = HerbColors.Ochre
                    )
                }
            }

            // 收藏按钮
            onFavoriteClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = if (isFavorite) {
                            androidx.compose.material.icons.filled.Star
                        } else {
                            androidx.compose.material.icons.outlined.StarBorder
                        },
                        contentDescription = if (isFavorite) "取消收藏" else "收藏",
                        tint = if (isFavorite) HerbColors.Cinnabar else HerbColors.InkLight
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.herb.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.InkBlack
                )

                val matchColor = when {
                    result.score >= 90 -> HerbColors.PineGreen
                    result.score >= 70 -> HerbColors.BambooGreen
                    else -> HerbColors.RattanYellow
                }

                Text(
                    text = "匹配度 ${result.score}%",
                    fontSize = 12.sp,
                    color = matchColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.herb.effects.joinToString(" · "),
                fontSize = 14.sp,
                color = HerbColors.InkGray
            )

            if (result.matchedEffects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    result.matchedEffects.forEach { effect ->
                        EffectTag(effect)
                    }
                }
            }

            result.herb.keyPoint?.let { keyPoint ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "[$keyPoint]",
                    fontSize = 12.sp,
                    color = HerbColors.Ochre
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    name: String,
    icon: String,
    description: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, HerbColors.BorderPale),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "$icon $name",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = HerbColors.InkGray,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "共 $count 味",
                fontSize = 12.sp,
                color = HerbColors.BambooGreen
            )
        }
    }
}

@Composable
fun EffectTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = HerbColors.BambooGreenPale
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = HerbColors.BambooGreenDark
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
