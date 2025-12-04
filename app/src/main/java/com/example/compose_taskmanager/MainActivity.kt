package com.example.compose_taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.compose_taskmanager.ui.theme.Compose_taskmanagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            setContent {
                val configuration = LocalConfiguration.current
                val items = remember {
                    List(50) { index ->
                        ItemData(
                            height = Random.nextInt(150, 300),
                            id = index,
                            color = Color(
                                Random.nextInt(255),
                                Random.nextInt(255),
                                Random.nextInt(255)
                            )
                        )
                    }
                }
                val staggeredGridState = rememberLazyStaggeredGridState()
                var scale by remember { mutableStateOf(1f) }
                val minScale = 0.5f
                val maxScale = 2f
                val minItemWidthDp = 200.dp
                val minColumn = 1
                val maxColumn = 5

                val screenWidthDp = configuration.screenWidthDp.dp
                val columnCount = remember(screenWidthDp, scale) {
                    derivedStateOf {
                        val scaledMinWidth = minItemWidthDp * scale
                        floor(screenWidthDp / scaledMinWidth).toInt().coerceIn(minColumn, maxColumn)
                    }
                }

                LazyVerticalStaggeredGrid(
                    //rows = StaggeredGridCells.Fixed(3),
                    columns = StaggeredGridCells.Fixed(columnCount.value),
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                scale = (scale * zoom).coerceIn(minScale, maxScale)
                            }
                        },
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    //verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = staggeredGridState
                ) {
                    itemsIndexed(items = items, key = { index, item -> item.id }) { index, item ->
                        val width = calculateSize(
                            configuration.screenWidthDp / columnCount.value,
                            scale
                        ).coerceAtLeast(minItemWidthDp.value.toInt())
                        val widthScale = width / (screenWidthDp.value / columnCount.value)
                        val aspectRatio = item.height.toFloat() / width.toFloat()
                        val height =
                            if (columnCount.value == 1) 80 else (if (columnCount.value != maxColumn && columnCount.value != minColumn) calculateSize(
                                item.height,
                                widthScale
                            ) else item.height)
                        Box(
                            Modifier
                                .width(width.dp)
                                .height(height.dp)
                                .clip(RoundedCornerShape(15.dp))
//                            .border(
//                                width = 5.dp,
//                                color = Color(34, 139, 34),
//                                shape = RoundedCornerShape(15.dp)
//                            )
                                .background(Color(224, 207, 117))
                                .padding(10.dp)
                        ) {
                            Text(
                                "Id: ${item.id}",
                                fontSize = 20.sp,
                                color = Color(34, 139, 34),
                                fontWeight = FontWeight.Bold
                            )

                        }
                    }
                }
            }
        }
    }

    data class ItemData(
        val height: Int,
        val id: Int,
        val color: Color
    )

    fun calculateSize(size: Int, scale: Float): Int {
        return (size * scale).toInt()
    }
}