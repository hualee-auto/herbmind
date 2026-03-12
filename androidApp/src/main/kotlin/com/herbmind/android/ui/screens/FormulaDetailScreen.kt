package com.herbmind.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.android.ui.viewmodel.FormulaDetailUiState
import com.herbmind.android.ui.viewmodel.FormulaDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaDetailScreen(
    formulaId: String,
    onBackClick: () -> Unit,
    onHerbClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: FormulaDetailViewModel = koinViewModel { parametersOf(formulaId) }
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

    val formula = uiState.formula
    if (formula == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("方剂未找到")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(formula.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 方剂名称
            Text(
                text = formula.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (formula.pinyin.isNotEmpty()) {
                Text(
                    text = formula.pinyin,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 出处
            if (formula.source.isNotEmpty()) {
                Text(
                    text = "出处: ${formula.source}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // 组成
            if (formula.ingredients.isNotEmpty()) {
                SectionTitle("组成")
                formula.ingredients.forEach { ingredient ->
                    val herbId = ingredient.herbId
                    val clickable = onHerbClick != null && !herbId.isNullOrEmpty()
                    Text(
                        text = "• ${ingredient.herbName}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable(enabled = clickable) {
                                herbId?.let { onHerbClick?.invoke(it) }
                            }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 功用
            if (formula.function.isNotEmpty()) {
                SectionTitle("功用")
                Text(
                    text = formula.function,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 主治
            if (formula.indication.isNotEmpty()) {
                SectionTitle("主治")
                Text(
                    text = formula.indication,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 方歌
            if (formula.song.isNotEmpty()) {
                SectionTitle("方歌")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = HerbColors.BambooGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = formula.song,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 用法
            if (formula.usage.isNotEmpty()) {
                SectionTitle("用法")
                Text(
                    text = formula.usage,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 注意事项
            if (formula.precautions.isNotEmpty()) {
                SectionTitle("注意事项")
                Text(
                    text = formula.precautions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = HerbColors.BambooGreen,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
