package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.presentation.utils.Padding

/** Home + movie-detail showcase height — first tray peeks below. */
val ShowcaseHeight = 380.dp

/** Poster height vs hero frame — keeps 16:9, sits upper-right. */
internal const val ShowcaseBackdropHeightFraction = 0.90f

/** Shared typography — home showcase and movie detail hero. */
object ShowcaseHeroStyles {
    val Title = TextStyle(
        fontFamily = BrewTitle,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 40.sp,
        letterSpacing = (-2.4).sp,
    )
    val Meta = TextStyle(
        fontFamily = BrewTitle,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
    )
    val Description = TextStyle(
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Medium,
    )
}

private const val BrewPlusWordmark =
    "https://createstir.b-cdn.net/stir-static/brew%2B.webp"

/**
 * Exact home showcase shell — fixed 380dp, poster clipped to right half, copy column bottom-left.
 * [backdrop] slot allows carousel cross-fade on home; movie detail passes a static backdrop.
 */
@Composable
fun ShowcaseHeroFrame(
    padding: Padding,
    modifier: Modifier = Modifier,
    showBrewPlus: Boolean = false,
    alsoInStore: Boolean = false,
    backdrop: @Composable BoxScope.() -> Unit,
    overlay: @Composable BoxScope.() -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ShowcaseHeight)
            .clipToBounds()
            .background(Color.Black),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            backdrop()
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = padding.start,
                    end = padding.end,
                    bottom = 4.dp,
                )
                .widthIn(max = 460.dp)
                .fillMaxWidth(0.56f),
            verticalArrangement = Arrangement.Bottom,
            content = bottomContent,
        )

        if (showBrewPlus) {
            ShowcaseHeroBrewPlusBadge(
                alsoInStore = alsoInStore,
                padding = padding,
            )
        }

        overlay()
    }
}

/** Right-half poster + left scrim gradients. Optional teaser video replaces poster after delay. */
@OptIn(UnstableApi::class)
@Composable
fun ShowcaseHeroBackdrop(
    posterUri: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    teaserPlayer: androidx.media3.exoplayer.ExoPlayer? = null,
    showTeaserVideo: Boolean = false,
    posterAlpha: Float = 1f,
    backdropHeightFraction: Float = ShowcaseBackdropHeightFraction,
) {
    val contentBg = Color.Black
    val context = LocalContext.current
    val imageRequest = remember(posterUri) {
        ImageRequest.Builder(context)
            .data(BrewImageUrl.forShowcase(posterUri))
            .size(BrewImageUrl.SHOWCASE_WIDTH, BrewImageUrl.SHOWCASE_HEIGHT)
            .crossfade(false)
            .build()
    }
    val verticalBrush = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color.Black.copy(alpha = 0.35f),
                0.42f to Color.Transparent,
                0.52f to Color.Black.copy(alpha = 0.25f),
                0.65f to Color.Black.copy(alpha = 0.55f),
                0.78f to Color.Black.copy(alpha = 0.82f),
                0.9f to Color.Black.copy(alpha = 0.95f),
                1f to Color.Black,
            ),
        )
    }
    val horizontalBrush1 = remember(contentBg) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to contentBg,
                0.08f to contentBg,
                0.15f to contentBg.copy(alpha = 0.98f),
                0.26f to contentBg.copy(alpha = 0.80f),
                0.36f to contentBg.copy(alpha = 0.50f),
                0.46f to contentBg.copy(alpha = 0.20f),
                0.54f to contentBg.copy(alpha = 0.05f),
                0.62f to Color.Transparent,
                1f to Color.Transparent,
            ),
        )
    }
    val horizontalBrush2 = remember(contentBg) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to contentBg,
                0.15f to contentBg.copy(alpha = 0.95f),
                0.30f to contentBg.copy(alpha = 0.65f),
                0.45f to contentBg.copy(alpha = 0.30f),
                0.58f to contentBg.copy(alpha = 0.08f),
                0.68f to Color.Transparent,
                1f to Color.Transparent,
            ),
        )
    }
    val imageLeftFadeBrush = remember(contentBg) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to contentBg,
                0.05f to contentBg.copy(alpha = 0.98f),
                0.11f to contentBg.copy(alpha = 0.85f),
                0.19f to contentBg.copy(alpha = 0.65f),
                0.28f to contentBg.copy(alpha = 0.40f),
                0.38f to contentBg.copy(alpha = 0.15f),
                0.46f to contentBg.copy(alpha = 0.05f),
                0.54f to Color.Transparent,
                1f to Color.Transparent,
            ),
        )
    }

    Box(modifier = modifier.background(contentBg)) {
        val mediaModifier = Modifier
            .align(Alignment.TopEnd)
            .fillMaxHeight(backdropHeightFraction)
            .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)

        if (showTeaserVideo && teaserPlayer != null) {
            PlayerSurface(
                player = teaserPlayer,
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                modifier = mediaModifier
                    .graphicsLayer { clip = true }
                    .drawWithContent {
                        drawContent()
                        drawRect(verticalBrush)
                        drawRect(imageLeftFadeBrush)
                    },
            )
        }

        if (posterUri.isNotBlank() && posterAlpha > 0.01f) {
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = mediaModifier
                    .alpha(posterAlpha)
                    .drawWithContent {
                        drawContent()
                        drawRect(verticalBrush)
                        drawRect(imageLeftFadeBrush)
                    },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(horizontalBrush1)
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.30f)
                .background(horizontalBrush2),
        )
    }
}

/** Skeleton hero backdrop — same layout/sizing as [ShowcaseHeroBackdrop] on movie detail. */
@Composable
fun ShowcaseHeroShimmerBackdrop(
    modifier: Modifier = Modifier,
    backdropHeightFraction: Float = 0.92f,
    posterUri: String? = null,
    posterFill: Brush = Brush.linearGradient(
        listOf(Color(0xFF141414), Color(0xFF1E1E1E), Color(0xFF141414)),
    ),
) {
    val contentBg = Color.Black
    val context = LocalContext.current
    val imageRequest = remember(posterUri) {
        posterUri?.takeIf { it.isNotBlank() }?.let { uri ->
            ImageRequest.Builder(context)
                .data(BrewImageUrl.forShowcase(uri))
                .size(BrewImageUrl.SHOWCASE_WIDTH, BrewImageUrl.SHOWCASE_HEIGHT)
                .crossfade(false)
                .build()
        }
    }
    val verticalBrush = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color.Black.copy(alpha = 0.35f),
                0.42f to Color.Transparent,
                0.52f to Color.Black.copy(alpha = 0.25f),
                0.65f to Color.Black.copy(alpha = 0.55f),
                0.78f to Color.Black.copy(alpha = 0.82f),
                0.9f to Color.Black.copy(alpha = 0.95f),
                1f to Color.Black,
            ),
        )
    }
    val horizontalBrush1 = remember(contentBg) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to contentBg,
                0.10f to contentBg,
                0.20f to contentBg.copy(alpha = 0.98f),
                0.34f to contentBg.copy(alpha = 0.82f),
                0.48f to contentBg.copy(alpha = 0.52f),
                0.60f to contentBg.copy(alpha = 0.22f),
                0.72f to contentBg.copy(alpha = 0.06f),
                0.82f to Color.Transparent,
                1f to Color.Transparent,
            ),
        )
    }
    val horizontalBrush2 = remember(contentBg) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to contentBg,
                0.22f to contentBg.copy(alpha = 0.96f),
                0.40f to contentBg.copy(alpha = 0.68f),
                0.56f to contentBg.copy(alpha = 0.32f),
                0.70f to contentBg.copy(alpha = 0.10f),
                0.82f to Color.Transparent,
                1f to Color.Transparent,
            ),
        )
    }
    val imageLeftFadeBrush = remember(contentBg) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to contentBg,
                0.06f to contentBg.copy(alpha = 0.98f),
                0.14f to contentBg.copy(alpha = 0.88f),
                0.24f to contentBg.copy(alpha = 0.68f),
                0.36f to contentBg.copy(alpha = 0.42f),
                0.48f to contentBg.copy(alpha = 0.18f),
                0.58f to contentBg.copy(alpha = 0.06f),
                0.68f to Color.Transparent,
                1f to Color.Transparent,
            ),
        )
    }

    Box(modifier = modifier.background(contentBg)) {
        val mediaModifier = Modifier
            .align(Alignment.TopEnd)
            .fillMaxHeight(backdropHeightFraction)
            .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)

        Box(
            modifier = mediaModifier
                .then(
                    if (imageRequest != null) {
                        Modifier
                    } else {
                        Modifier.background(posterFill)
                    },
                )
                .drawWithContent {
                    drawContent()
                    drawRect(verticalBrush)
                    drawRect(imageLeftFadeBrush)
                },
        ) {
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(horizontalBrush1)
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.38f)
                .background(horizontalBrush2),
        )
    }
}

@Composable
fun ShowcaseHeroMetaRow(
    infoLine: String,
    showStore: Boolean,
    modifier: Modifier = Modifier,
) {
    if (infoLine.isBlank() && !showStore) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 4.dp),
    ) {
        if (showStore) {
            Box(
                modifier = Modifier
                    .offset(y = (-3).dp)
                    .size(13.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.38f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fa_shopping_bag),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(7.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        if (infoLine.isNotBlank()) {
            Text(
                text = infoLine.uppercase(),
                color = Color.White.copy(alpha = 0.78f),
                style = ShowcaseHeroStyles.Meta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

@Composable
fun BoxScope.ShowcaseHeroBrewPlusBadge(
    alsoInStore: Boolean,
    padding: Padding,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(end = padding.end, bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = stringResource(
                if (alsoInStore) {
                    R.string.also_included_in_brew_plus
                } else {
                    R.string.included_in_brew_plus
                },
            ),
            color = Color.White.copy(alpha = 0.55f),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = (-0.45).sp,
            lineHeight = 10.sp,
            modifier = Modifier.alignByBaseline(),
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(BrewPlusWordmark)
                .size(220, 44)
                .build(),
            contentDescription = "Brew Plus",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .alignByBaseline()
                .offset(y = (-1).dp)
                .height(10.dp)
                .width(38.dp),
        )
    }
}
