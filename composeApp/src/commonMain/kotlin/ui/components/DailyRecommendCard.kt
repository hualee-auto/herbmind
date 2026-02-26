package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.data.model.DailyRecommend
import com.herbmind.data.model.RecommendType
import ui.theme.HerbColors

@Composable
fun DailyRecommendCard(
    recommend: DailyRecommend,
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
            // 类型标签
            val (icon, label, color) = when (recommend.type) {
                RecommendType.SEASONAL ->
                    Triple(Icons.Default.WbSunny, "节气", HerbColors.RattanYellow)
                RecommendType.EXAM ->
                    Triple(Icons.Default.Star, "考点", HerbColors.Cinnabar)
                RecommendType.CONTRAST ->
                    Triple(Icons.Default.CompareArrows, "对比", HerbColors.Ochre)
                RecommendType.DISCOVERY ->
                    Triple(Icons.Default.Explore, "发现", HerbColors.InfoBlue)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 药名和功效
            Text(
                text = recommend.herb.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HerbColors.InkBlack
            )

            Text(
                text = recommend.herb.effects.take(2).joinToString(" · "),
                fontSize = 14.sp,
                color = HerbColors.InkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = HerbColors.BorderPale)

            Spacer(modifier = Modifier.height(8.dp))

            // 推荐理由
            Text(
                text = recommend.reason,
                fontSize = 13.sp,
                color = HerbColors.Ochre
            )
        }
    }
}
