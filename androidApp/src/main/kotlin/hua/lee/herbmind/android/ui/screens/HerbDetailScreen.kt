@file:OptIn(ExperimentalFoundationApi::class)

package hua.lee.herbmind.android.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import hua.lee.herbmind.android.ui.theme.HerbColors
import hua.lee.herbmind.android.ui.viewmodel.HerbDetailUiState
import hua.lee.herbmind.android.ui.viewmodel.HerbDetailViewModel
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.remote.ResourceConfig
import hua.lee.herbmind.domain.ad.model.BannerAdData
import coil.compose.rememberAsyncImagePainter
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * 将图片半路径拼接为完整 URL
 */
@Composable
private fun rememberFullImageUrl(halfPath: String): String {
    return remember(halfPath) {
        if (halfPath.startsWith("http://") || halfPath.startsWith("https://")) {
            halfPath
        } else {
            ResourceConfig.getImageBaseUrl() + halfPath
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbDetailScreen(
    herbId: String,
    onBackClick: () -> Unit,
    onFormulaClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: HerbDetailViewModel = koinViewModel { parametersOf(herbId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = HerbColors.BambooGreen)
        }
        return
    }

    if (uiState.error != null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(uiState.error ?: "加载失败")
        }
        return
    }

    val herb = uiState.herb
    if (herb == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("药材未找到")
        }
        return
    }

    HerbDetailContent(
        herb = herb,
        relatedFormulas = uiState.relatedFormulas,
        uiState = uiState,
        onBackClick = onBackClick,
        onFormulaClick = onFormulaClick,
        modifier = modifier
    )
}

/**
 * 横幅广告视图，使用AdMob官方AdView渲染
 * 直接使用测试广告单元ID，确保每次都能加载成功
 */
@Composable
private fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adView = remember {
        com.google.android.gms.ads.AdView(context).apply {
            // 直接使用AdMob官方测试横幅广告ID，确保加载成功
            setAdUnitId("ca-app-pub-3940256099942544/9214589741")
            setAdSize(com.google.android.gms.ads.AdSize.BANNER)
            loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HerbDetailContent(
    herb: Herb,
    relatedFormulas: List<hua.lee.herbmind.data.model.Formula>,
    uiState: HerbDetailUiState,
    onBackClick: () -> Unit,
    onFormulaClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 图片查看器状态
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }
    // 横幅广告高度，标准AdMob横幅高度为50dp
    val bannerAdHeight = 50.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(herb.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // 横幅广告悬浮在底部
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerAdHeight)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                // 底部预留广告高度 + 间距，避免内容被广告遮挡
                .padding(bottom = bannerAdHeight + 16.dp)
        ) {
            // 药材图片（分区域展示，支持滑动切换）
            HerbImagesSection(
                herb = herb,
                onImageClick = { imageUrl ->
                    selectedImageUrl = imageUrl
                    showImageViewer = true
                }
            )

            // 基本信息卡片
            BasicInfoCard(herb = herb)

            // 功效主治
            EffectsCard(herb = herb)

            // 详细信息
            DetailsCard(herb = herb)

            // 相关方剂
            if (relatedFormulas.isNotEmpty()) {
                RelatedFormulasCard(
                    formulas = relatedFormulas,
                    onFormulaClick = onFormulaClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 图片查看对话框
    if (showImageViewer) {
        ImageViewerDialog(
            imageUrl = selectedImageUrl,
            onDismiss = { showImageViewer = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HerbImagesSection(
    herb: Herb,
    onImageClick: (String) -> Unit
) {
    val medicinalImages = herb.images.slice.takeIf { it.isNotEmpty() } ?: herb.images.medicinal
    val plantImages = herb.images.plant

    Column {
        // 饮片图区域（滑动切换）
        if (medicinalImages.isNotEmpty()) {
            ImageCarousel(
                images = medicinalImages,
                label = "饮片图",
                contentDescription = "${herb.name} 饮片图",
                onImageClick = onImageClick
            )
        }

        // 植物图区域（滑动切换）
        if (plantImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ImageCarousel(
                images = plantImages,
                label = "植物图",
                contentDescription = "${herb.name} 植物图",
                onImageClick = onImageClick
            )
        }

        // 没有图片时显示占位符
        if (medicinalImages.isEmpty() && plantImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(HerbColors.RicePaper),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌿",
                    fontSize = 64.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageCarousel(
    images: List<String>,
    label: String,
    contentDescription: String,
    onImageClick: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Column {
        // 标签
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // 图片轮播
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // 拼接完整图片 URL
                val imageUrl = rememberFullImageUrl(images[page])
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "$contentDescription ${page + 1}/${images.size}",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .clickable { onImageClick(imageUrl) },
                    contentScale = ContentScale.Fit
                )
            }

            // 指示器（多张图片时显示）
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == pagerState.currentPage)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicInfoCard(herb: Herb) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 名称和拼音
            Text(
                text = herb.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = herb.pinyin,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (herb.latinName.isNotEmpty()) {
                Text(
                    text = herb.latinName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // 类别
            InfoRow(label = "类别", value = herb.category)

            // 性味
            if (herb.nature.isNotEmpty()) {
                InfoRow(label = "性味", value = herb.nature)
            }

            // 归经
            if (herb.meridians.isNotEmpty()) {
                InfoRow(label = "归经", value = herb.meridians.joinToString("、"))
            }

            // 产地
            if (herb.origin.isNotEmpty()) {
                InfoRow(label = "产地", value = herb.origin)
            }

            // 别名
            if (herb.aliases.isNotEmpty()) {
                InfoRow(label = "别名", value = herb.aliases.joinToString("、"))
            }
        }
    }
}

@Composable
private fun EffectsCard(herb: Herb) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreen.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "功效",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HerbColors.BambooGreen
            )

            if (herb.effects.isNotEmpty()) {
                Text(
                    text = herb.effects.joinToString("、"),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (herb.indications.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "主治",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HerbColors.BambooGreen
                )

                herb.indications.forEach { indication ->
                    Text(
                        text = "• $indication",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsCard(herb: Herb) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "详细信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 性状
            if (herb.traits.isNotEmpty()) {
                DetailSection(title = "性状", content = herb.traits)
            }

            // 品质
            if (herb.quality.isNotEmpty()) {
                DetailSection(title = "品质", content = herb.quality)
            }

            // 来源链接
            if (herb.sourceUrl.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = "数据来源: 香港浸会大学中医药学院",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: String) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun RelatedFormulasCard(
    formulas: List<hua.lee.herbmind.data.model.Formula>,
    onFormulaClick: ((String) -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "相关方剂",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            formulas.forEach { formula ->
                ListItem(
                    headlineContent = { Text(formula.name) },
                    supportingContent = {
                        if (formula.function.isNotEmpty()) {
                            Text(
                                formula.function,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        onFormulaClick?.invoke(formula.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 图片查看对话框 - 支持手势缩放和拖动
 * 单张图片展示，点击哪张展示哪张
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 顶部工具栏
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )

            // 图片查看器（支持缩放）
            ZoomableImage(
                imageUrl = imageUrl,
                contentDescription = "查看图片"
            )
        }
    }
}

/**
 * 可缩放的图片组件
 * 支持：双指缩放、双击缩放、拖动查看
 */
@Composable
private fun ZoomableImage(
    imageUrl: String,
    contentDescription: String
) {
    // 缩放状态
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 双击缩放倍数
    val doubleTapScale = 3f
    // 最大缩放倍数
    val maxScale = 5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 处理缩放手势（双指缩放 + 拖动）
            .pointerInput(Unit) {
                detectTransformGestures(
                    // 启用平移（拖动）
                    panZoomLock = true
                ) { centroid, pan, zoom, _ ->
                    // 计算新的缩放值
                    val newScale = (scale * zoom).coerceIn(1f, maxScale)

                    // 计算新的偏移量
                    val maxX = (size.width * (newScale - 1)) / 2
                    val maxY = (size.height * (newScale - 1)) / 2

                    // 平移时需要考虑当前缩放比例
                    val newOffsetX = (offset.x + pan.x * scale).coerceIn(-maxX, maxX)
                    val newOffsetY = (offset.y + pan.y * scale).coerceIn(-maxY, maxY)

                    offset = Offset(newOffsetX, newOffsetY)
                    scale = newScale
                }
            }
            // 双击检测（单独处理）
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > 1f) {
                            // 重置
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            // 放大到指定倍数，以点击位置为中心
                            scale = doubleTapScale

                            // 计算偏移让点击位置成为中心
                            val maxX = (size.width * (scale - 1)) / 2
                            val maxY = (size.height * (scale - 1)) / 2

                            val targetX = (size.width / 2 - tapOffset.x) * (scale - 1)
                            val targetY = (size.height / 2 - tapOffset.y) * (scale - 1)

                            offset = Offset(
                                targetX.coerceIn(-maxX, maxX),
                                targetY.coerceIn(-maxY, maxY)
                            )
                        }
                    }
                )
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit
        )
    }
}
