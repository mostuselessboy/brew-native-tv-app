package com.google.jetstream.presentation.screens.auth

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.presentation.theme.BrewTitle
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun AuthScreen(
    onLoggedIn: () -> Unit,
    onBackPressed: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            AuthUiState.Success, AuthUiState.AlreadyLoggedIn -> onLoggedIn()
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            AuthUiState.Loading, AuthUiState.LoggingIn -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (state is AuthUiState.LoggingIn) "Signing you in…" else "Loading…",
                        color = Color.White,
                        fontFamily = BrewTitle,
                        fontSize = 28.sp,
                    )
                }
            }

            is AuthUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = state.message,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 18.sp,
                    )
                    AuthRetryButtons(
                        canRetryLogin = state.canRetryLogin,
                        onRetryLogin = viewModel::retryLogin,
                        onNewQr = viewModel::generateQr,
                    )
                }
            }

            is AuthUiState.Ready -> {
                AuthContent(
                    code = state.code,
                    qrUrl = state.qrUrl,
                )
            }

            AuthUiState.Success, AuthUiState.AlreadyLoggedIn -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Welcome${user?.name?.let { ", $it" }.orEmpty()}",
                        color = Color.White,
                        fontFamily = BrewTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthRetryButtons(
    canRetryLogin: Boolean,
    onRetryLogin: () -> Unit,
    onNewQr: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (canRetryLogin) {
            Button(onClick = onRetryLogin) {
                Text(
                    text = "Retry sign in",
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Button(onClick = onNewQr) {
            Text(
                text = if (canRetryLogin) "New QR code" else "Try again",
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun AuthContent(code: String, qrUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 72.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Get Started",
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 44.sp,
            letterSpacing = (-1.2).sp,
        )
        Spacer(modifier = Modifier.height(36.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                StepHeader(step = 1, text = "Scan this code with your phone, or go to brew.tv/tv2")
                Spacer(modifier = Modifier.height(20.dp))
                QrImage(url = qrUrl)
            }
            Column(modifier = Modifier.weight(1f)) {
                StepHeader(step = 2, text = "Confirm this code on your phone or tablet")
                Spacer(modifier = Modifier.height(20.dp))
                PairingCodeDisplay(code = code)
            }
        }
    }
}

@Composable
private fun StepHeader(step: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = step.toString(),
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.92f),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QrImage(url: String) {
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(url) {
        bitmap = rememberQrBitmap(url, 280)
    }
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Login QR code",
                modifier = Modifier.size(248.dp),
            )
        }
    }
}

@Composable
private fun PairingCodeDisplay(code: String) {
    val chars = code.filter { it.isLetterOrDigit() }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        chars.forEach { char ->
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 56.dp)
                    .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = char.uppercaseChar().toString(),
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                )
            }
        }
    }
}

private fun rememberQrBitmap(content: String, size: Int): Bitmap? {
    return runCatching {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    }.getOrNull()
}
