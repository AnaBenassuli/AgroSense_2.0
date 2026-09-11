package com.agrosense.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.agrosense.app.R

@Composable
fun AgroSenseLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val preferredHeight = if (compact) 28.dp else 132.dp

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val responsiveHeight = minOf(
            preferredHeight,
            maxWidth / 2.72f
        )

        Image(
            painter = painterResource(R.drawable.agrosense_logo),
            contentDescription = "Logo de AgroSense",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(responsiveHeight)
                .aspectRatio(2.72f)
        )
    }
}