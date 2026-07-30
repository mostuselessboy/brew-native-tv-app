package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.google.jetstream.data.entities.PurchaseCta
import com.google.jetstream.data.entities.PurchaseCtaSlot
import com.google.jetstream.presentation.common.BrewDetailIconButton
import com.google.jetstream.presentation.theme.BrewTitle

private val BrewYellow = Color(0xFFFFC107)
private val MetaBadgeBg = Color.White.copy(alpha = 0.16f)

@Composable
fun DetailMetaRow(
    releaseLabel: String,
    genres: String,
    duration: String,
    ratingBadge: String,
    ratingSummary: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ratingSummary?.takeIf { it.isNotBlank() }?.let { stars ->
            Text(
                text = stars,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (releaseLabel.isNotBlank()) {
            MetaChip(releaseLabel)
        }
        if (genres.isNotBlank()) {
            Text(
                text = genres,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (duration.isNotBlank() && duration != "—") {
            Text(
                text = duration,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
            )
        }
        if (ratingBadge.isNotBlank() && ratingBadge != "NR") {
            MetaChip(ratingBadge.uppercase())
        }
    }
}

@Composable
private fun MetaChip(label: String) {
    Text(
        text = label,
        color = Color.White.copy(alpha = 0.92f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        modifier = Modifier
            .background(MetaBadgeBg, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

@Composable
fun DetailSynopsisBlock(
    text: String,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (text.isBlank()) return
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (text.length > 120 || text.contains('\n')) {
            Spacer(modifier = Modifier.width(8.dp))
            MetaChip(if (expanded) "LESS" else "MORE")
        }
    }
}

@Composable
fun DetailActionBar(
    purchaseCta: PurchaseCta?,
    hasTrailer: Boolean,
    isLoggedIn: Boolean,
    onWatch: () -> Unit,
    onRent: () -> Unit,
    onBuy: () -> Unit,
    onSubscribe: () -> Unit,
    onLoginRequired: () -> Unit,
    onTrailer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primarySlot = purchaseCta?.slots?.firstOrNull()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (primarySlot != null) {
            DetailPrimaryButton(
                slot = primarySlot,
                isLoggedIn = isLoggedIn,
                onWatch = onWatch,
                onRent = onRent,
                onBuy = onBuy,
                onSubscribe = onSubscribe,
                onLoginRequired = onLoginRequired,
            )
        } else if (hasTrailer) {
            DetailPrimaryWatchButton(label = "Watch trailer", sublabel = null, onClick = onTrailer)
        }

        purchaseCta?.slots?.drop(1)?.take(2)?.forEach { slot ->
            DetailSecondaryPill(
                slot = slot,
                isLoggedIn = isLoggedIn,
                onRent = onRent,
                onBuy = onBuy,
                onSubscribe = onSubscribe,
                onLoginRequired = onLoginRequired,
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        DetailIconAction(Icons.Outlined.BookmarkBorder, "Save") { onLoginRequired() }
        DetailIconAction(Icons.Outlined.Add, "My List") { onLoginRequired() }
        DetailIconAction(Icons.Outlined.ThumbUp, "Like") { }
        if (hasTrailer) {
            DetailIconAction(Icons.Outlined.PlayArrow, "Trailer", onTrailer)
        }
        DetailIconAction(Icons.Outlined.Info, "Info") { }
    }
}

@Composable
private fun DetailPrimaryButton(
    slot: PurchaseCtaSlot,
    isLoggedIn: Boolean,
    onWatch: () -> Unit,
    onRent: () -> Unit,
    onBuy: () -> Unit,
    onSubscribe: () -> Unit,
    onLoginRequired: () -> Unit,
) {
    val label = slotLabel(slot)
    val sublabel = slotSublabel(slot)
    val onClick = {
        when (slot.kind) {
            "watch" -> if (slot.isFree || isLoggedIn) onWatch() else onLoginRequired()
            "rent" -> if (isLoggedIn) onRent() else onLoginRequired()
            "buy" -> if (isLoggedIn) onBuy() else onLoginRequired()
            "subscribe-yearly", "subscribe-quarterly" ->
                if (isLoggedIn) onSubscribe() else onLoginRequired()
            else -> onLoginRequired()
        }
    }
    DetailPrimaryWatchButton(label = label, sublabel = sublabel.takeIf { it.isNotBlank() }, onClick = onClick)
}

@Composable
private fun DetailPrimaryWatchButton(
    label: String,
    sublabel: String?,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.widthIn(min = 180.dp, max = 240.dp),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(10.dp)),
        colors = ButtonDefaults.colors(
            containerColor = Color.White,
            contentColor = Color.Black,
            focusedContainerColor = Color.White,
            focusedContentColor = Color.Black,
        ),
    ) {
        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            sublabel?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = Color.Black.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = label,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DetailSecondaryPill(
    slot: PurchaseCtaSlot,
    isLoggedIn: Boolean,
    onRent: () -> Unit,
    onBuy: () -> Unit,
    onSubscribe: () -> Unit,
    onLoginRequired: () -> Unit,
) {
    val isYellow = slot.color.equals("yellow", ignoreCase = true)
    val bg = if (isYellow) BrewYellow else Color.White.copy(alpha = 0.16f)
    val fg = if (isYellow) Color.Black else Color.White
    Button(
        onClick = {
            when (slot.kind) {
                "rent" -> if (isLoggedIn) onRent() else onLoginRequired()
                "buy" -> if (isLoggedIn) onBuy() else onLoginRequired()
                "subscribe-yearly", "subscribe-quarterly" ->
                    if (isLoggedIn) onSubscribe() else onLoginRequired()
                else -> onLoginRequired()
            }
        },
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(10.dp)),
        colors = ButtonDefaults.colors(
            containerColor = bg,
            contentColor = fg,
            focusedContainerColor = bg,
            focusedContentColor = fg,
        ),
        modifier = Modifier.widthIn(min = 120.dp, max = 160.dp),
    ) {
        Text(
            text = slotLabel(slot),
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailIconAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit = {},
) {
    BrewDetailIconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun slotLabel(slot: PurchaseCtaSlot): String = when (slot.kind) {
    "watch" -> when {
        slot.isFree -> "Watch for free"
        slot.isContinueWatching -> "Continue watching"
        else -> "Watch now"
    }
    "rent" -> "Rent"
    "buy" -> "Buy"
    "subscribe-yearly" -> "Brew+"
    "subscribe-quarterly" -> "Quarterly"
    else -> slot.kind.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

private fun slotSublabel(slot: PurchaseCtaSlot): String = when (slot.kind) {
    "watch" -> when {
        slot.isContinueWatching -> "${slot.percentageWatched.toInt()}% watched"
        else -> "Included with Brew+"
    }
    else -> ""
}
