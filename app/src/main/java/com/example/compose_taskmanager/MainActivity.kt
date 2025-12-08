package com.example.compose_taskmanager

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    lateinit var winTask: WindowTask
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                var stateExp = false

                val screenWidthDp = configuration.screenWidthDp.dp
                val columnCount = remember(key1 = screenWidthDp, key2 = scale) {
                    derivedStateOf {
                        val scaledMinWidth = minItemWidthDp * scale
                        floor(screenWidthDp / scaledMinWidth).toInt().coerceIn(minColumn, maxColumn)
                    }
                }

                var isScaling by remember {mutableStateOf(false)}
                winTask = WindowTask()

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
                        stateExp = SwipeableListItem(
                            item = item,
                            width = width,
                            height = height,
                            onDelete = {deleteItem ->
                                items.remove(deleteItem)
                            },
                            onComplete = {completedItem ->
                                completedItem.onComplete = !completedItem.onComplete
                            },
                            isScaling = isScaling,
                            //modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                winTask.ExpandableBlock(stateExp, onDismiss = {stateExp = false})
            }
    }

    @Composable
    fun rememberListState(): MutableList<ListItem>{
        return remember {mutableStateListOf<ListItem>()}
    }

    enum class SwipeAction{
        DELETE, COMPLETE, NONE
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
    ): Boolean{
        var expanded by remember {mutableStateOf(false)}
        val swipeableState = rememberSwipeableState(initialValue = SwipeAction.NONE)
        val density = LocalDensity.current
        val actionWidth = 40.dp
        val actionWidthPx = with(density) {actionWidth.toPx()}
        val anchors: Map<Float, SwipeAction> = mapOf(
            -actionWidthPx to SwipeAction.COMPLETE,
            0f to SwipeAction.NONE,
            actionWidthPx to SwipeAction.DELETE


//            0f to false,
//            with(density){ actionWidth.toPx() } to true,
        )
        Box(
            modifier = modifier
                .width(width.dp)
                .height(height.dp)
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal,
                    enabled = !isScaling
                )
        ){
            Row(
                modifier = modifier
                    .width((width/2).dp)
                    .height(height.dp)
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        shape = RoundedCornerShape(15.dp)
                        clip = true // Включаем clip в graphicsLayer
                    }
                    .background(Color.Red),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            var delayedState by remember {mutableStateOf(item.onComplete)}
            LaunchedEffect(item.onComplete) {
                delay(350)                    // ← задержка 1 сек
                delayedState = item.onComplete // ← применяем изменение
            }
            Row(
                modifier = modifier
                    .width((width/2).dp)
                    .height(height.dp)
                    .align(Alignment.CenterEnd)
                    .graphicsLayer {
                        shape = RoundedCornerShape(15.dp)
                        clip = true // Включаем clip в graphicsLayer
                    }
                    .background( if(delayedState) Color.Red else Color(34, 139, 34)),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = if(delayedState) Icons.Filled.Close else Icons.Filled.Check,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 10.dp),
                )
            }
            Row(
                modifier = modifier
                    .width(width.dp)
                    .height(height.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                //winTask.ExpandableBlock(expanded, onDismiss = {expanded = false})
                val offsetPX = swipeableState.offset.value

                Column(
                    modifier = Modifier
                        .width(width.dp)
                        .height(height.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(15.dp)
                            clip = true
                            translationX = offsetPX
                        }
                        .border(
                            width = 5.dp,
                            color = Color(101, 67, 33),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .clickable {expanded = true}
                        .background(Color(245, 245, 220))

                ) {
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                        Text(
                            "Id: ${item.id}",
                            fontSize = 20.sp,
                            color = Color(34, 139, 34),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier.width(45.dp).height(45.dp),
                                //modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                                imageVector = if (item.onComplete) Icons.Filled.Check else Icons.Filled.Close,
                                tint = if (item.onComplete) Color(34, 139, 34) else Color.Red,
                                contentDescription = if (item.onComplete) "complete" else "uncomplete",
                            )
                        }
                    }
                }
            }
        }


        LaunchedEffect(swipeableState.currentValue) {
            Log.d("debug", "outside ${swipeableState.currentValue} offset: ${swipeableState.offset.value}")
            if(swipeableState.offset.value > 0){
                onDelete(item)
                //onComplete(item)
            }else if(swipeableState.offset.value < 0){
                onComplete(item)
                //onDelete(item)
            }
            swipeableState.animateTo(SwipeAction.NONE)
        }
        Log.d("debug", "$expanded")
        return expanded
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