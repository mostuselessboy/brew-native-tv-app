package com.google.jetstream.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.tv.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.auth.QrCodeGenerator
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.theme.BrewDisplay
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.jetstream.presentation.utils.handleDPadKeyEvents
import com.google.jetstream.tvmaterial.Dialog

enum class BrewQrPopupIcon {
    Brew,
    Youtube,
}

enum class BrewQrPopupDoneAction {
    Dismiss,
    RefreshPurchase,
}

data class BrewQrPopupState(
    val qrUrl: String,
    val title: String,
    val message: String,
    val posterUri: String? = null,
    val icon: BrewQrPopupIcon = BrewQrPopupIcon.Brew,
    val doneAction: BrewQrPopupDoneAction = BrewQrPopupDoneAction.Dismiss,
)

private val PopupShape = RoundedCornerShape(20.dp)
private val PopupBorder = Color.White.copy(alpha = 0.14f)
private val PopupSurface = Color(0xFF121212)
private val ArtShape = RoundedCornerShape(14.dp)

/** Compact brew.tv QR popup — dark card; share uses wide art + overlapping QR badge. */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun BrewQrPopup(
    state: BrewQrPopupState?,
    onDismissRequest: () -> Unit,
    onDone: () -> Unit,
) {
    if (state == null) return

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(state.qrUrl) {
        runCatching { focusRequester.requestFocus() }
    }

    val footerText = when (state.doneAction) {
        BrewQrPopupDoneAction.RefreshPurchase -> "Press OK when you're finished"
        BrewQrPopupDoneAction.Dismiss -> "Press OK to close"
    }
    val isShareLayout = state.posterUri?.isNotBlank() == true

    Dialog(
        showDialog = true,
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .focusRequester(focusRequester)
                .focusable()
                .handleDPadKeyEvents(onEnter = onDone),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(340.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E),
                                PopupSurface,
                                Color(0xFF0A0A0A),
                            ),
                        ),
                        PopupShape,
                    )
                    .border(1.dp, PopupBorder, PopupShape)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = state.title,
                        color = Color.White,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isShareLayout) 22.sp else 20.sp,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = state.message,
                        color = Color.White.copy(alpha = 0.62f),
                        fontFamily = BrewDisplay,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                    )

                    if (isShareLayout) {
                        SharePosterWithCenteredQr(
                            posterUri = state.posterUri!!,
                            qrUrl = state.qrUrl,
                            icon = state.icon,
                        )
                    } else {
                        BrewQrCodeWithCenterIcon(
                            url = state.qrUrl,
                            icon = state.icon,
                            size = 148.dp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    Text(
                        text = footerText,
                        color = Color.White.copy(alpha = 0.45f),
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = (-0.2).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/** Vertical poster art with centered QR badge. */
@Composable
private fun SharePosterWithCenteredQr(
    posterUri: String,
    qrUrl: String,
    icon: BrewQrPopupIcon,
) {
    val context = LocalContext.current
    val artWidth = 150.dp
    val artHeight = 225.dp
    val qrSize = 110.dp

    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .width(artWidth)
            .height(artHeight)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(BrewImageUrl.forPortraitCard(posterUri))
                .size(BrewImageUrl.PORTRAIT_CARD_WIDTH, BrewImageUrl.PORTRAIT_CARD_HEIGHT)
                .crossfade(200)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Semi-transparent dark overlay to ensure QR is highly readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f)),
        )

        // QR Code configured in the center
        BrewQrCodeWithCenterIcon(
            url = qrUrl,
            icon = icon,
            size = qrSize,
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp)),
        )
    }
}

@Composable
private fun BrewQrCodeWithCenterIcon(
    url: String,
    icon: BrewQrPopupIcon,
    size: Dp = 148.dp,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(url) { QrCodeGenerator.createBitmap(url, 512) }
    val logoSize = size * 0.30f
    val iconWidth = logoSize * 1.1f
    val iconHeight = logoSize * 0.65f

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(size * 0.07f),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR code",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )

        Box(
            modifier = Modifier
                .size(logoSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when (icon) {
                BrewQrPopupIcon.Brew -> Image(
                    painter = painterResource(R.drawable.brew_logo),
                    contentDescription = null,
                    modifier = Modifier.size(width = iconWidth, height = iconHeight),
                    contentScale = ContentScale.Fit,
                )
                BrewQrPopupIcon.Youtube -> Image(
                    painter = painterResource(R.drawable.ic_youtube),
                    contentDescription = null,
                    modifier = Modifier.size(logoSize * 0.58f),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
