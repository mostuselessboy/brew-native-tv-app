package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
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

private val CastShape = RoundedCornerShape(12.dp)
private val CastCardWidth = 132.dp

/**
 * Cast row aligned with vod-frontend `CastAndCrew.tsx` —
 * 3:4 headshots, name + role under the image.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CastAndCrewList(castAndCrew: List<MovieCast>) {
    if (castAndCrew.isEmpty()) return
    val childPadding = rememberChildPadding()
    val members = castAndCrew.take(12)

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = stringResource(R.string.cast_and_crew),
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = (-0.4).sp,
            modifier = Modifier.padding(start = childPadding.start),
        )
        LazyRow(
            modifier = Modifier
                .padding(top = 14.dp)
                .focusRestorer(),
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(members, key = { it.id }) { member ->
                CastAndCrewItem(member)
            }
        }
    }
}

@Composable
private fun CastAndCrewItem(castMember: MovieCast) {
    val context = LocalContext.current
    Surface(
        onClick = {},
        shape = ClickableSurfaceDefaults.shape(CastShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                shape = CastShape,
            ),
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)),
                shape = CastShape,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF111111),
            focusedContainerColor = Color(0xFF111111),
        ),
        modifier = Modifier.width(CastCardWidth),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center,
            ) {
                if (castMember.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(BrewImageUrl.forCast(castMember.avatarUrl))
                            .size(BrewImageUrl.CAST_WIDTH, BrewImageUrl.CAST_HEIGHT)
                            .crossfade(true)
                            .build(),
                        contentDescription = castMember.realName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = castMember.realName.take(1).uppercase(),
                        color = Color.White.copy(alpha = 0.35f),
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    )
                }
            }
            Text(
                text = castMember.realName,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp),
            )
            if (castMember.characterName.isNotBlank()) {
                Text(
                    text = castMember.characterName,
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 12.dp),
                )
            } else {
                SpacerBottom()
            }
        }
    }
}

@Composable
private fun SpacerBottom() {
    Box(modifier = Modifier.padding(bottom = 12.dp))
}
