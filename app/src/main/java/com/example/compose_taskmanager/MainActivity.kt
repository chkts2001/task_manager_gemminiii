package com.example.compose_taskmanager

import android.graphics.pdf.models.ListItem
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.compose_taskmanager.ui.theme.Compose_taskmanagerTheme
import kotlinx.serialization.builtins.ArraySerializer
import kotlin.div
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            setContent {
                val configuration = LocalConfiguration.current
                val items = rememberListState()
                val staggeredGridState = rememberLazyStaggeredGridState()
                var scale by remember { mutableStateOf(1f) }
                val minScale = 0.5f
                val maxScale = 2f
                val minItemWidthDp = 200.dp
                val minColumn = 1
                val maxColumn = 5

                val screenWidthDp = configuration.screenWidthDp.dp
                val columnCount = remember(key1 = screenWidthDp, key2 = scale) {
                    derivedStateOf {
                        val scaledMinWidth = minItemWidthDp * scale
                        floor(screenWidthDp / scaledMinWidth).toInt().coerceIn(minColumn, maxColumn)
                    }
                }

                var isScaling by remember {mutableStateOf(false)}

                LaunchedEffect(Unit){
                    val tempList = mutableListOf<ListItem>()
                    repeat(50) { index ->
                        tempList.add(
                            ListItem(
                                height = Random.nextInt(150, 300),
                                id = index,
                                onComplete = false,
                                color = Color(
                                    Random.nextInt(255),
                                    Random.nextInt(255),
                                    Random.nextInt(255)
                                )
                            )
                        )
                    }
                    items.addAll(tempList)
                }


                LazyVerticalStaggeredGrid(
                    //rows = StaggeredGridCells.Fixed(3),
                    StaggeredGridCells.Fixed(columnCount.value),
                    Modifier.pointerInput(Unit) {
                       awaitEachGesture {
                           var transformStarted = false
                           while(true){
                               val event = awaitPointerEvent()
                               val zoomChange = event.calculateZoom()
                               if (!transformStarted && event.changes.size >= 2){
                                   transformStarted = true
                                   isScaling = true
                               }
                               if(transformStarted){
                                   scale = (scale * zoomChange).coerceIn(minScale, maxScale)
                               }
                               if(event.changes.all {it.changedToUp()}){
                                   transformStarted = false
                                   isScaling = false
                               }
                           }
                       }
                    },
                    state = staggeredGridState,
                    PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    itemsIndexed(items = items, key = { index, item -> item.id }) { index, item ->
                        val width = calculateSize(configuration.screenWidthDp / columnCount.value,scale).coerceAtLeast(minItemWidthDp.value.toInt())
                        val widthScale = width / (screenWidthDp.value / columnCount.value)
                        val aspectRatio = item.height.toFloat() / width.toFloat()
                        val height = if (columnCount.value == 1) 80 else (if (columnCount.value != maxColumn && columnCount.value != minColumn) calculateSize(
                                item.height,
                                widthScale
                            ) else item.height)
                        SwipeableListItem(
                            item = item,
                            width = width,
                            height = height,
                            onDelete = {deleteItem ->
                                items.remove(deleteItem)
                            },
                            onComplete = {completedItem ->
                                completedItem.onComplete = true
                            },
                            isScaling = isScaling,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun rememberListState(): MutableList<ListItem>{
        return remember {mutableStateListOf<ListItem>()}
    }

    enum class SwipeAction{
        DELETE, COMPLETE
    }

    @OptIn(ExperimentalWearMaterialApi::class)
    @Composable
    fun SwipeableListItem(
        item: ListItem,
        width: Int,
        height: Int,
        onDelete: (ListItem) -> Unit,
        onComplete: (ListItem) -> Unit,
        isScaling: Boolean,
        modifier: Modifier = Modifier
    ){
        val swipeableState = rememberSwipeableState(initialValue = false)
        val density = LocalDensity.current
        val actionWidth = 70.dp
        val anchors = mapOf(
            0f to false,
            with(density){ actionWidth.toPx() } to true
        )

        Box(
            modifier = modifier
                .fillMaxWidth()
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = {_, _ -> FractionalThreshold(0.3f)},
                    orientation = Orientation.Horizontal,
                    enabled = !isScaling
                )
        ){
            Row(
                modifier = modifier.fillMaxSize().height(height.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){}
//            ){
//                IconButton(onClick = {onDelete(item)},
//                    modifier = Modifier.background(Color.Red)
//                        .width(actionWidth)
//                        .height(height.dp)
//                ){
//                    Icon(
//                        imageVector = Icons.Filled.Delete,
//                        contentDescription = "Delete",
//                        tint = Color.White
//                    )
//                }
        }
        Column(
            modifier = Modifier
                .width(width.dp)
                .height(height.dp)
               // .offset{IntoOffset(swipeableState.offset.value.roundToInt(), 0)}

                .clip(RoundedCornerShape(15.dp))
                .border(
                    width = 5.dp,
                    color = Color(101, 67, 33),
                    shape = RoundedCornerShape(15.dp)
                )
                .background(Color(245, 245, 220))
                .padding(10.dp)
        ) {
            Text(
                "Id: ${item.id}",
                fontSize = 20.sp,
                color = Color(34, 139, 34),
                fontWeight = FontWeight.Bold
            )

        }

        LaunchedEffect(swipeableState.currentValue) {
            if(swipeableState.currentValue){
                Log.d("debug", "${swipeableState.currentValue} offset: ${swipeableState.offset.value}")
                if(swipeableState.offset.value > 0){
                    onDelete(item)
                    //onComplete(item)
                }else{
                    //onDelete(item)
                }
                swipeableState.animateTo(false)
            }
        }
    }

    @Composable
    fun IntoOffset(x0: Int, x1: Int): IntOffset {
        TODO("Not yet implemented")
    }

    data class ListItem(
        val height: Int,
        val id: Int,
        var onComplete: Boolean,
        val color: Color
    )

    fun calculateSize(size: Int, scale: Float): Int {
        return (size * scale).toInt()
    }
}