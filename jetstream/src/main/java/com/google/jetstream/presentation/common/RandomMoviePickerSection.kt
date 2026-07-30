package com.google.jetstream.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle
import kotlin.random.Random

private val PickerTitleStyle = TextStyle(
    fontFamily = BrewTitle,
    fontWeight = FontWeight.Bold,
    fontSize = 54.sp,
    lineHeight = 50.sp,
    letterSpacing = (-3.8).sp,
)

@Composable
fun RandomMoviePickerSection(
    movies: MovieList,
    onSurpriseMe: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (movies.isEmpty()) return

    val childPadding = rememberChildPadding()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = childPadding.start,
                end = childPadding.end,
                top = 8.dp,
                bottom = 8.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.stuck_in_scroll_loop),
            color = Color.White,
            style = PickerTitleStyle,
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 620.dp),
        )

        Button(
            onClick = {
                onSurpriseMe(movies[Random.nextInt(movies.size)])
            },
            modifier = Modifier
                .height(44.dp)
                .padding(start = 16.dp),
            shape = ButtonDefaults.shape(shape = RoundedCornerShape(50)),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
            colors = ButtonDefaults.colors(
                containerColor = Color.White,
                contentColor = Color.Black,
                focusedContainerColor = Color.White,
                focusedContentColor = Color.Black,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.surprise_me),
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.35).sp,
                )
            }
        }
    }
}
