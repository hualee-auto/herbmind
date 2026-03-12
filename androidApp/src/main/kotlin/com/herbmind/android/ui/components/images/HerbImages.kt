package com.herbmind.android.ui.components.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.android.util.ImageResourceConfig
import com.herbmind.data.model.Images

// 预定义一组柔和的背景色用于无图片时显示
private val placeholderColors = listOf(
    Color(0xFFE8F5E9), // 浅绿
    Color(0xFFFFF3E0), // 浅橙
    Color(0xFFE3F2FD), // 浅蓝
    Color(0xFFF3E5F5), // 浅紫
    Color(0xFFFFFDE7), // 浅黄
    Color(0xFFE0F2F1), // 浅青
    Color(0xFFFCE4EC), // 浅粉
    Color(0xFFF5F5F5), // 浅灰
)

/**
 * 根据药材名称生成固定的颜色索引
 * 确保同一药材每次都显示相同颜色
 */
private fun getColorIndexForHerb(herbName: String): Int {
    return herbName.hashCode().absoluteValue % placeholderColors.size
}

private val Int.absoluteValue: Int get() = if (this < 0) -this else this

/**
 * 获取药材名称的缩写（取前2个字或首字）
 */
private fun getHerbAbbreviation(herbName: String): String {
    return when {
        herbName.length >= 2 -> herbName.substring(0, 2)
        herbName.isNotEmpty() -> herbName
        else -> "?"
    }
}

/**
 * 药材图片组件 - 标准尺寸
 *
 * @param images 图片信息对象
 * @param herbName 药材名称（用于占位符显示）
 * @param modifier 修饰符
 * @param contentScale 内容缩放模式
 */
@Composable
fun HerbImage(
    images: Images,
    herbName: String,
    modifier: Modifier = Modifier.size(120.dp),
    contentScale: ContentScale = ContentScale.Crop
) {
    // 获取第一张图片，如果没有则使用空字符串
    val imagePath = images.slice.firstOrNull()
        ?: images.medicinal.firstOrNull()
        ?: ""
    HerbImageFromPath(
        imagePath = imagePath,
        herbName = herbName,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * 药材图片组件（直接使用路径）
 *
 * @param imagePath 图片半路径（如：resources/images/concocted/人参_hkbu.jpg）
 * @param herbName 药材名称（用于占位符显示）
 * @param modifier 修饰符
 * @param contentScale 内容缩放模式
 */
@Composable
fun HerbImageFromPath(
    imagePath: String?,
    herbName: String,
    modifier: Modifier = Modifier.size(120.dp),
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    // 获取完整图片 URL
    val imageUrl = ImageResourceConfig.getImageUrl(imagePath)

    // 是否有图片
    val hasImage = !imageUrl.isNullOrEmpty()

    // 获取固定的背景色
    val backgroundColor = placeholderColors[getColorIndexForHerb(herbName)]

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (hasImage) {
                // 加载网络图片
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = herbName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            } else {
                // 显示随机背景色 + 药材名称缩写
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getHerbAbbreviation(herbName),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 小型药材图片 - 用于列表项
 *
 * @param images 图片信息对象
 * @param herbName 药材名称
 * @param modifier 修饰符
 */
@Composable
fun HerbSmallImage(
    images: Images,
    herbName: String,
    modifier: Modifier = Modifier.size(48.dp)
) {
    val imagePath = images.slice.firstOrNull()
        ?: images.medicinal.firstOrNull()
        ?: ""
    HerbSmallImageFromPath(
        imagePath = imagePath,
        herbName = herbName,
        modifier = modifier
    )
}

/**
 * 小型药材图片（直接使用路径）
 *
 * @param imagePath 图片半路径
 * @param herbName 药材名称
 * @param modifier 修饰符
 */
@Composable
fun HerbSmallImageFromPath(
    imagePath: String?,
    herbName: String,
    modifier: Modifier = Modifier.size(48.dp)
) {
    val context = LocalContext.current
    val imageUrl = ImageResourceConfig.getImageUrl(imagePath)
    val hasImage = !imageUrl.isNullOrEmpty()

    // 获取固定的背景色
    val backgroundColor = placeholderColors[getColorIndexForHerb(herbName)]

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (hasImage) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = herbName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = getHerbAbbreviation(herbName).take(1),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 大型药材图片 - 用于详情页
 *
 * @param images 图片信息对象
 * @param herbName 药材名称
 * @param modifier 修饰符
 */
@Composable
fun HerbLargeImage(
    images: Images,
    herbName: String,
    modifier: Modifier = Modifier.size(200.dp)
) {
    val imagePath = images.slice.firstOrNull()
        ?: images.medicinal.firstOrNull()
        ?: ""
    HerbLargeImageFromPath(
        imagePath = imagePath,
        herbName = herbName,
        modifier = modifier
    )
}

/**
 * 大型药材图片（直接使用路径）
 *
 * @param imagePath 图片半路径
 * @param herbName 药材名称
 * @param modifier 修饰符
 */
@Composable
fun HerbLargeImageFromPath(
    imagePath: String?,
    herbName: String,
    modifier: Modifier = Modifier.size(200.dp)
) {
    val context = LocalContext.current
    val imageUrl = ImageResourceConfig.getImageUrl(imagePath)
    val hasImage = !imageUrl.isNullOrEmpty()

    // 获取固定的背景色
    val backgroundColor = placeholderColors[getColorIndexForHerb(herbName)]

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (hasImage) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = herbName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // 显示占位符
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = getHerbAbbreviation(herbName),
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 药材图片行（用于列表显示）
 *
 * @param herbId 药材ID
 * @param herbName 药材名称
 * @param images 图片信息对象
 * @param onClick 点击回调
 */
@Composable
fun HerbImageRow(
    herbId: String,
    herbName: String,
    images: Images,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HerbImage(
            images = images,
            herbName = herbName,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = herbName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "点击查看详情",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
