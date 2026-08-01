package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.util.WatchPlatformLogos
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

/** Non-interactive “More ways to watch” platform row — sits inside page bottom gradient. */
@Composable
fun MovieDetailPageFooter(modifier: Modifier = Modifier) {
    val childPadding = rememberChildPadding()
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .focusProperties { canFocus = false }
            .padding(
                start = childPadding.start,
                end = childPadding.end,
                top = 20.dp,
                bottom = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "More ways to watch",
            color = Color.White.copy(alpha = 0.42f),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = (-0.25).sp,
            maxLines = 1,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            WatchPlatformLogos.footerRow.forEach { logo ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(logo.url)
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(logo.widthDp.dp)
                        .height(logo.heightDp.dp),
                )
            }
        }
    }
}
