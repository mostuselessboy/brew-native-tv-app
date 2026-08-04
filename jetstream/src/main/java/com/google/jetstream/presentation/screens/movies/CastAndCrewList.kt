package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.entities.MovieCast
import com.google.jetstream.data.util.BrewImageUrl
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle

private val CastAvatarSize = 100.dp
private val CastCardWidth = 104.dp

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun CastAndCrewList(
    castAndCrew: List<MovieCast>,
    onCastMemberClick: (MovieCast) -> Unit,
    firstItemFocusRequester: FocusRequester? = null,
) {
    if (castAndCrew.isEmpty()) return
    val childPadding = rememberChildPadding()
    val members = castAndCrew.take(12)
    val defaultFirstItemFocusRequester = remember { FocusRequester() }
    val firstItem = firstItemFocusRequester ?: defaultFirstItemFocusRequester

    Column(modifier = Modifier.padding(top = 8.dp)) {
        MovieDetailSectionTitle(text = stringResource(R.string.cast_and_crew))
        LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .focusRestorer(),
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(members, key = { it.id }) { member ->
                CastAndCrewItem(
                    castMember = member,
                    onCastMemberClick = onCastMemberClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CastAndCrewItem(
    castMember: MovieCast,
    onCastMemberClick: (MovieCast) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }

    val scaleAnimationSpec = if (isFocused) {
        spring<Float>(
            dampingRatio = 0.85f,
            stiffness = 180f
        )
    } else {
        tween<Float>(durationMillis = 650, easing = LinearOutSlowInEasing)
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1.0f,
        animationSpec = scaleAnimationSpec,
        label = "CastCardScale"
    )

    Column(
        modifier = modifier.width(CastCardWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            onClick = { onCastMemberClick(castMember) },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 1f),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(1.5.dp, Color.White),
                    shape = CircleShape,
                ),
            ),
            glow = ClickableSurfaceDefaults.glow(
                focusedGlow = Glow(
                    elevationColor = Color.White.copy(alpha = 0.30f),
                    elevation = 20.dp,
                ),
            ),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color(0xFF1A1A1A),
                focusedContainerColor = Color(0xFF2A2A2A),
            ),
            modifier = Modifier
                .size(CastAvatarSize)
                .onFocusChanged { isFocused = it.isFocused }
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .zIndex(if (isFocused) 10f else 1f),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(CastAvatarSize)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A)),
            ) {
                if (castMember.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(BrewImageUrl.forCastAvatar(castMember.avatarUrl))
                            .size(BrewImageUrl.CAST_AVATAR_PX, BrewImageUrl.CAST_AVATAR_PX)
                            .crossfade(true)
                            .build(),
                        contentDescription = castMember.realName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(CastAvatarSize)
                            .clip(CircleShape),
                    )
                } else {
                    Text(
                        text = castMember.realName.take(1).uppercase(),
                        color = Color.White.copy(alpha = 0.35f),
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                }
            }
        }
        Text(
            text = castMember.realName,
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = (-0.3).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(CastCardWidth)
                .padding(top = 8.dp),
        )
        if (castMember.characterName.isNotBlank()) {
            Text(
                text = castMember.characterName,
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(CastCardWidth)
                    .padding(top = 2.dp),
            )
        }
    }
}
