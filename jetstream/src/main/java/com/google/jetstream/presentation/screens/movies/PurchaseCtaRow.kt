package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.google.jetstream.presentation.theme.BrewTitle

private val BrewYellow = Color(0xFFFFC107)

@Composable
fun PurchaseCtaRow(
    purchaseCta: PurchaseCta,
    isLoggedIn: Boolean,
    onWatch: () -> Unit,
    onRent: () -> Unit,
    onBuy: () -> Unit,
    onSubscribe: () -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            purchaseCta.slots.forEach { slot ->
                PurchaseCtaButton(
                    slot = slot,
                    onClick = {
                        when (slot.kind) {
                            "watch" -> if (slot.isFree || isLoggedIn) onWatch() else onLoginRequired()
                            "rent" -> if (isLoggedIn) onRent() else onLoginRequired()
                            "buy" -> if (isLoggedIn) onBuy() else onLoginRequired()
                            "subscribe-yearly", "subscribe-quarterly" ->
                                if (isLoggedIn) onSubscribe() else onLoginRequired()
                            else -> onLoginRequired()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun PurchaseCtaButton(
    slot: PurchaseCtaSlot,
    onClick: () -> Unit,
) {
    val isYellow = slot.color.equals("yellow", ignoreCase = true)
    val container = if (isYellow) BrewYellow else Color.White
    val content = Color.Black
    val label = slotLabel(slot)
    val sublabel = slotSublabel(slot)

    Button(
        onClick = onClick,
        modifier = Modifier.widthIn(min = 148.dp, max = 220.dp),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.colors(
            containerColor = container,
            contentColor = content,
            focusedContainerColor = container,
            focusedContentColor = content,
        ),
    ) {
        if (slot.kind == "watch") {
            Icon(Icons.Outlined.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.size(6.dp))
        }
        Column {
            Text(
                text = label,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (sublabel.isNotBlank()) {
                Text(
                    text = sublabel,
                    fontSize = 11.sp,
                    color = content.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

private fun slotLabel(slot: PurchaseCtaSlot): String = when (slot.kind) {
    "watch" -> if (slot.isFree) "Watch for free" else if (slot.isContinueWatching) "Continue watching" else "Watch Now"
    "rent" -> "Rent movie"
    "buy" -> "Buy movie"
    "subscribe-yearly" -> "Watch with Brew+"
    "subscribe-quarterly" -> "Quarterly Plan"
    "coin" -> "Unlock with coins"
    "coming-soon" -> "Coming Soon"
    "support_filmmaker" -> "Support filmmaker"
    "merch" -> "Buy merch"
    "not_available" -> "Not available"
    else -> slot.kind.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

private fun slotSublabel(slot: PurchaseCtaSlot): String = when (slot.kind) {
    "watch" -> when {
        slot.isFree -> "No ads, quick sign-up"
        slot.isContinueWatching -> "${slot.percentageWatched.toInt()}% watched"
        else -> "Included with Brew+"
    }
    "rent" -> "520+ rented this week"
    "buy" -> "180+ bought this week"
    "subscribe-yearly" -> "One year. Full catalog."
    "subscribe-quarterly" -> "3 months full catalog"
    else -> ""
}
