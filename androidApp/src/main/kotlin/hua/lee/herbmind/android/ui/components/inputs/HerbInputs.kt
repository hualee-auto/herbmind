package hua.lee.herbmind.android.ui.components.inputs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hua.lee.herbmind.android.ui.theme.HerbColors

/**
 * 搜索栏 - 首页使用
 *
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param placeholder 占位文字
 */
@Composable
fun HerbSearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "输入功效，查找中药..."
) {
    Card(
        onClick = onClick,
        modifier = modifier
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
                text = placeholder,
                fontSize = 16.sp,
                color = HerbColors.InkLight
            )
        }
    }
}

/**
 * 搜索输入框 - 搜索页使用
 *
 * @param query 当前查询文字
 * @param onQueryChange 查询文字变化回调
 * @param onSearch 搜索回调
 * @param onClear 清除回调
 * @param modifier 修饰符
 * @param placeholder 占位文字
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbSearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "输入功效，查找中药..."
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        border = BorderStroke(
            width = 2.dp,
            color = HerbColors.BambooGreen
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = HerbColors.BambooGreen,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = HerbColors.InkLight,
                        fontSize = 16.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = HerbColors.PureWhite,
                    unfocusedContainerColor = HerbColors.PureWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "清除",
                        tint = HerbColors.InkLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 文本输入框 - 通用
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param label 标签
 * @param modifier 修饰符
 * @param placeholder 占位文字
 * @param singleLine 是否单行
 * @param maxLines 最大行数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = HerbColors.InkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        color = HerbColors.InkLight
                    )
                }
            },
            singleLine = singleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = HerbColors.PureWhite,
                unfocusedContainerColor = HerbColors.PureWhite,
                focusedIndicatorColor = HerbColors.BambooGreen,
                unfocusedIndicatorColor = HerbColors.BorderLight
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 多行文本输入框
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param label 标签
 * @param modifier 修饰符
 * @param placeholder 占位文字
 * @param minLines 最小行数
 * @param maxLines 最大行数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    minLines: Int = 3,
    maxLines: Int = 6
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = HerbColors.InkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        color = HerbColors.InkLight
                    )
                }
            },
            minLines = minLines,
            maxLines = maxLines,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = HerbColors.PureWhite,
                unfocusedContainerColor = HerbColors.PureWhite,
                focusedIndicatorColor = HerbColors.BambooGreen,
                unfocusedIndicatorColor = HerbColors.BorderLight
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
