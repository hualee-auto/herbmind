package hua.lee.herbmind.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hua.lee.herbmind.data.model.HerbCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
    categories: List<HerbCategory>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            AssistChip(
                onClick = { onCategoryClick(category.name) },
                label = {
                    Text("${category.icon} ${category.name} (${category.herbCount})")
                }
            )
        }
    }
}
