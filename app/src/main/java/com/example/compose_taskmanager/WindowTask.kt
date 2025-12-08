package com.example.compose_taskmanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class WindowTask {
    @Composable
    fun ExpandableBlock(expanded: Boolean, onDismiss: () -> Unit) {
        var exp = expanded
//        Text("Заголовок", fontSize = 20.sp)
//        if(expanded){
//            Spacer(Modifier.height(22.dp))
//            Text("Контент, который появляется с анимацией.")
//        }

//        val scale by animateFloatAsState(
//            targetValue = if(expanded) 1f else 0.8f,
//            label = ""
//        )
//        val alpha by animateFloatAsState(
//            targetValue = if(expanded) 1f else 0f,
//            label = ""
//        )
//
//        if(expanded){
//            Box(modifier = Modifier.fillMaxWidth()
//                .graphicsLayer{
//                    scaleX = scale
//                    scaleY = scale
//                    this.alpha = alpha
//                }
//                .clip(RoundedCornerShape(15.dp))
//                .background(Color.White)
//                .padding(16.dp)
//            ){
//                Text("Содержание окна")
//            }
//        }

//        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
//        Box(
//            modifier = Modifier.fillMaxSize()
//                .background(Color.Transparent)
//        ) {
//            if (expanded) {
//                Box(
//                    Modifier.fillMaxSize()
//                        .background(Color.Black.copy(alpha = 0.4f))
//                        .clickable { onDismiss() }
//                )
//            }
//
//            AnimatedVisibility(visible = expanded,
//                enter = slideInVertically(
//                    initialOffsetY = {-it}
//                ) + fadeIn(),
//                exit = slideOutVertically(
//                    targetOffsetY = {-it}
//                ) + fadeOut()) {
//                    Box(
//                        modifier = Modifier
//                            .height(screenHeight / 2)
//                            .fillMaxSize()
//                            .background(Color.White, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
//                            .padding(20.dp)
//                    ){
//                        content()
//                    }
//            }
//        }
        if(exp){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onDismiss)
            ){
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                        .animateContentSize(animationSpec = spring())
                ){
                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                        Text("Это кастомный диалог!")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { exp = false; onDismiss()}) { Text("Закрыть")}
                    }
                }
            }
        }
    }
}