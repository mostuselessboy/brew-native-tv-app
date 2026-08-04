package com.google.jetstream.presentation.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.data.util.RibbonLabel
import com.google.jetstream.data.util.RibbonLabelImages
import com.google.jetstream.presentation.theme.BrewTitle

private val StampGold = androidx.compose.ui.graphics.Color(0xFFFFC15E)
private val StampInset = androidx.compose.ui.graphics.Color(0x47FFC15E)

enum class RibbonLabelBadgeSize {
    Sm,
    Md,
}

/** Hero eyebrow ribbon — week poster art or gold selection stamp. */
@Composable
fun RibbonLabelBadge(
    label: String,
    modifier: Modifier = Modifier,
    size: RibbonLabelBadgeSize = RibbonLabelBadgeSize.Md,
) {
    val text = label.trim()
    if (text.isEmpty()) return

    when {
        RibbonLabel.isFilmOfTheWeek(text) -> {
            WeekRibbonImage(
                url = RibbonLabelImages.FILM_OF_THE_WEEK,
                height = weekImageHeight(size),
                modifier = modifier,
            )
        }
        RibbonLabel.isShortOfTheWeek(text) -> {
            WeekRibbonImage(
                url = RibbonLabelImages.SHORT_OF_THE_WEEK,
                height = weekImageHeight(size),
                modifier = modifier,
            )
        }
        RibbonLabel.isSelectionStamp(text) -> {
            SelectionStampBadge(
                label = RibbonLabel.normalize(text),
                size = size,
                modifier = modifier,
            )
        }
    }
}

private fun weekImageHeight(size: RibbonLabelBadgeSize): Dp =
    when (size) {
        RibbonLabelBadgeSize.Sm -> 40.dp
        RibbonLabelBadgeSize.Md -> 48.dp
    }

@Composable
private fun WeekRibbonImage(
    url: String,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val aspect = remember(url) {
        when (url) {
            RibbonLabelImages.FILM_OF_THE_WEEK -> 1590f / 1195f
            RibbonLabelImages.SHORT_OF_THE_WEEK -> 1964f / 1196f
            else -> 1.33f
        }
    }
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(180)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .padding(start = 8.dp)
            .height(height)
            .width(height * aspect),
    )
}

@Composable
private fun SelectionStampBadge(
    label: String,
    size: RibbonLabelBadgeSize,
    modifier: Modifier = Modifier,
) {
    val isSm = size == RibbonLabelBadgeSize.Sm
    Box(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = StampGold,
                shape = RoundedCornerShape(3.dp),
            )
            .padding(3.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = StampGold,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = if (isSm) 9.sp else 11.sp,
            letterSpacing = if (isSm) 1.8.sp else 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .border(
                    width = 3.dp,
                    color = StampInset,
                    shape = RoundedCornerShape(2.dp),
                )
                .padding(
                    horizontal = if (isSm) 8.dp else 10.dp,
                    vertical = if (isSm) 3.dp else 4.dp,
                ),
        )
    }
}
