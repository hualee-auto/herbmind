package com.herbmind.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.herbmind.android.util.ImageResourceConfig

/**
 * 药材图片组件
 * 用于在UI中显示药材图片
 * 
 * @param imagePath 图片半路径（如：resources/images/concocted/人参_hkbu.jpg）
 * @param herbName 药材名称（用于占位符显示）
 * @param modifier 修饰符
 * @param contentScale 内容缩放模式
 */
@Composable
fun HerbImage(
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
                // 显示占位符
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌿",
                        fontSize = 32.sp
                    )
                }
            }
        }
    }
}

/**
 * 带占位符的药材图片组件
 * 
 * @param imagePath 图片半路径
 * @param herbName 药材名称
 * @param modifier 修饰符
 * @param placeholderColor 占位符背景色
 */
@Composable
fun HerbImageWithPlaceholder(
    imagePath: String?,
    herbName: String,
    modifier: Modifier = Modifier.size(120.dp),
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val context = LocalContext.current
    val imageUrl = ImageResourceConfig.getImageUrl(imagePath)
    val hasImage = !imageUrl.isNullOrEmpty()
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(placeholderColor),
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
                text = "🌿",
                fontSize = 32.sp
            )
        }
    }
}

/**
 * 药材图片行（用于列表显示）
 * 
 * @param herbId 药材ID
 * @param herbName 药材名称
 * @param imagePath 图片半路径
 * @param onClick 点击回调
 */
@Composable
fun HerbImageRow(
    herbId: String,
    herbName: String,
    imagePath: String?,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HerbImage(
            imagePath = imagePath,
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

/**
 * 小型药材图片（用于列表项）
 * 
 * @param imagePath 图片半路径
 * @param herbName 药材名称
 * @param modifier 修饰符
 */
@Composable
fun HerbSmallImage(
    imagePath: String?,
    herbName: String,
    modifier: Modifier = Modifier.size(48.dp)
) {
    val context = LocalContext.current
    val imageUrl = ImageResourceConfig.getImageUrl(imagePath)
    val hasImage = !imageUrl.isNullOrEmpty()
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
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
            Text(
                text = "🌿",
                fontSize = 24.sp
            )
        }
    }
}
