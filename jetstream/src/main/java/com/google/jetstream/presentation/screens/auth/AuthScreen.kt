package com.google.jetstream.presentation.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.auth.QrCodeGenerator
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.auth.AuthValidation
import com.google.jetstream.data.auth.AuthSignInMethod
import com.google.jetstream.data.auth.AuthSignInStep
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.delay

private val AccentYellow = Color(0xFFFFC15E)
private const val WatchHiddenGemsUrl =
    "https://createstir.b-cdn.net/stir-static/watch-hidden-gems.png"

private const val AuthBgImageUrl =
    "https://createstir.b-cdn.net/assetlibrary/1842/Image_1784184667208_s20a.png?width=1920&height=1440&quality=100&sharpen=true&format=webp"

private val SeparatorGray = Color(0xFF5C5C5C)

/** Landscape onboarding-style sign in — Netflix-style tabs with QR primary & remote alternate. */
@Composable
fun AuthScreen(
    onSignedIn: () -> Unit,
    onBackPressed: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler(onBack = onBackPressed)

    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) onSignedIn()
    }



    if (uiState.isLoggingIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Loading(modifier = Modifier.size(50.dp))
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFB800).copy(alpha = 0.3f),
                            Color(0xFF141414)
                        ),
                        radius = 1200f
                    )
                )
        )

        Box(
            modifier = Modifier
                .width(680.dp)
                .wrapContentHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.95f),
                        ),
                    ),
                ),
        )

        BrewAuthBrandRow(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 36.dp, top = 20.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Choose how to sign in",
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                letterSpacing = (-0.5).sp,
            )

            AuthHeaderPillTabs(
                selectedMethod = uiState.method,
                onSelectQr = { viewModel.selectMethod(AuthSignInMethod.Qr) },
                onSelectRemote = {
                    if (uiState.method == AuthSignInMethod.Qr) {
                        viewModel.selectMethod(AuthSignInMethod.Phone)
                    }
                },
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )

            when {
                uiState.step == AuthSignInStep.Otp -> PhoneEmailOnboardingContent(
                    method = uiState.otpIssueChannel ?: uiState.method,
                    step = uiState.step,
                    phoneDigits = uiState.phoneDigits,
                    email = uiState.email,
                    countryLabel = "+${uiState.selectedCountry.dialCode} ${uiState.selectedCountry.name}",
                    otp = uiState.otp,
                    otpDisplay = uiState.otpSentDisplay,
                    otpIssueChannel = uiState.otpIssueChannel,
                    isLoading = uiState.isLoading,
                    resendCountdown = uiState.resendCountdown,
                    resendAttempts = uiState.resendAttempts,
                    onPhoneChange = viewModel::updatePhoneDigits,
                    onEmailChange = viewModel::updateEmail,
                    onOtpChange = viewModel::updateOtp,
                    onContinue = viewModel::sendOtp,
                    onResend = viewModel::resendOtp,
                    onBackToInput = { viewModel.resetOtpStep() },
                    onCycleCountry = viewModel::cycleCountry,
                    onSwitchMode = viewModel::switchInputMode,
                )
                uiState.method == AuthSignInMethod.Email -> PhoneEmailOnboardingContent(
                    method = uiState.method,
                    step = uiState.step,
                    phoneDigits = uiState.phoneDigits,
                    email = uiState.email,
                    countryLabel = "+${uiState.selectedCountry.dialCode} ${uiState.selectedCountry.name}",
                    otp = uiState.otp,
                    otpDisplay = uiState.otpSentDisplay,
                    otpIssueChannel = uiState.otpIssueChannel,
                    isLoading = uiState.isLoading,
                    resendCountdown = uiState.resendCountdown,
                    resendAttempts = uiState.resendAttempts,
                    onPhoneChange = viewModel::updatePhoneDigits,
                    onEmailChange = viewModel::updateEmail,
                    onOtpChange = viewModel::updateOtp,
                    onContinue = viewModel::sendOtp,
                    onResend = viewModel::resendOtp,
                    onBackToInput = { viewModel.resetOtpStep() },
                    onCycleCountry = viewModel::cycleCountry,
                    onSwitchMode = viewModel::switchInputMode,
                )
                uiState.method == AuthSignInMethod.Phone -> PhoneEmailOnboardingContent(
                    method = uiState.method,
                    step = uiState.step,
                    phoneDigits = uiState.phoneDigits,
                    email = uiState.email,
                    countryLabel = "+${uiState.selectedCountry.dialCode} ${uiState.selectedCountry.name}",
                    otp = uiState.otp,
                    otpDisplay = uiState.otpSentDisplay,
                    otpIssueChannel = uiState.otpIssueChannel,
                    isLoading = uiState.isLoading,
                    resendCountdown = uiState.resendCountdown,
                    resendAttempts = uiState.resendAttempts,
                    onPhoneChange = viewModel::updatePhoneDigits,
                    onEmailChange = viewModel::updateEmail,
                    onOtpChange = viewModel::updateOtp,
                    onContinue = viewModel::sendOtp,
                    onResend = viewModel::resendOtp,
                    onBackToInput = { viewModel.resetOtpStep() },
                    onCycleCountry = viewModel::cycleCountry,
                    onSwitchMode = viewModel::switchInputMode,
                )
                else -> QrOnboardingContent(
                    qrUrl = uiState.qrUrl,
                    qrCode = uiState.qrCode,
                    qrExpired = uiState.qrExpired,
                    isLoading = uiState.isLoading,
                    onRefreshQr = viewModel::generateQrCode,
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TVTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    containerFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    var isKeyboardActive by remember { mutableStateOf(false) }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    Surface(
        onClick = {
            isKeyboardActive = true
            runCatching { focusRequester.requestFocus() }
            keyboardController?.show()
        },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.08f),
            focusedContainerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White,
            focusedContentColor = Color.White,
        ),
        modifier = modifier
            .height(48.dp)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .focusRequester(containerFocusRequester)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = !isKeyboardActive,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(AccentYellow),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color.White.copy(alpha = 0.35f),
                                fontFamily = BrewTitle,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onKeyEvent { event ->
                        if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                            if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP) {
                                isKeyboardActive = false
                                keyboardController?.hide()
                                runCatching { containerFocusRequester.requestFocus() }
                            }
                            true
                        } else {
                            false
                        }
                    }
            )
        }
    }
}

@Composable
private fun BrewAuthBrandRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.brew_logo),
            contentDescription = "Brew",
            modifier = Modifier
                .height(24.dp)
                .width(38.dp),
            contentScale = ContentScale.Fit,
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(SeparatorGray),
        )
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(WatchHiddenGemsUrl)
                .crossfade(false)
                .build(),
            contentDescription = "Watch hidden gems",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(28.dp)
                .width(56.dp),
        )
    }
}

@Composable
private fun QrAndPhoneOnboardingContent(
    qrUrl: String?,
    qrCode: String?,
    qrExpired: Boolean,
    isLoading: Boolean,
    phoneDigits: String,
    countryLabel: String,
    onRefreshQr: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onCycleCountry: () -> Unit,
    onContinuePhone: () -> Unit,
) {
    val phoneFocus = remember { FocusRequester() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Top,
    ) {
        QrOnboardingContent(
            qrUrl = qrUrl,
            qrCode = qrCode,
            qrExpired = qrExpired,
            isLoading = isLoading,
            onRefreshQr = onRefreshQr,
            modifier = Modifier.width(720.dp),
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(280.dp)
                .background(SeparatorGray.copy(alpha = 0.6f)),
        )

        Column(
            modifier = Modifier
                .width(380.dp)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Sign in with phone",
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text(
                text = "We'll text you a 4-digit code",
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = BrewTitle,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuthSecondaryButton(label = countryLabel, onClick = onCycleCountry)
                AuthTextField(
                    value = phoneDigits,
                    onValueChange = onPhoneChange,
                    placeholder = "Phone number",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier
                        .width(200.dp)
                        .focusRequester(phoneFocus),
                )
            }
            AuthPrimaryButton(
                label = if (isLoading) "Sending…" else "Continue",
                onClick = onContinuePhone,
                enabled = !isLoading,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun QrOnboardingContent(
    qrUrl: String?,
    qrCode: String?,
    qrExpired: Boolean,
    isLoading: Boolean,
    onRefreshQr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(56.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(420.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            OnboardingStepHeader(
                step = 1,
                text = "Point your camera to this code, or go to brew.tv/tv2",
            )
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .padding(14.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isLoading && qrUrl == null -> Loading(modifier = Modifier.size(240.dp))
                    qrExpired -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "QR expired",
                                color = Color.Black.copy(alpha = 0.7f),
                                fontFamily = BrewTitle,
                                fontSize = 16.sp,
                            )
                            AuthPrimaryButton(
                                label = "Refresh QR",
                                onClick = onRefreshQr,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                    qrUrl != null -> {
                        val bitmap = remember(qrUrl) { QrCodeGenerator.createBitmap(qrUrl, 800) }
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR sign in",
                            modifier = Modifier.size(240.dp),
                        )
                    }
                }
            }
            if (!qrExpired && qrUrl != null) {
                AuthSecondaryButton(
                    label = "Refresh QR",
                    onClick = onRefreshQr,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .align(Alignment.CenterHorizontally),
                )
            }
        }

        Column(
            modifier = Modifier.width(400.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            OnboardingStepHeader(
                step = 2,
                text = "Confirm this code on your device",
            )
            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (isLoading || qrCode.isNullOrBlank()) {
                    Loading(modifier = Modifier.size(56.dp))
                } else {
                    qrCode.forEach { char ->
                        Box(
                            modifier = Modifier
                                .size(width = 50.dp, height = 64.dp)
                                .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = char.toString(),
                                color = Color.White,
                                fontFamily = BrewTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepHeader(step: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = step.toString(),
                color = Color.White,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontFamily = BrewTitle,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthHeaderPillTabs(
    selectedMethod: AuthSignInMethod,
    onSelectQr: () -> Unit,
    onSelectRemote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isQrSelected = selectedMethod == AuthSignInMethod.Qr
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthHeaderPillTab(
                label = "Use QR code",
                selected = isQrSelected,
                onSelect = onSelectQr,
            )
            AuthHeaderPillTab(
                label = "Use remote",
                selected = !isQrSelected,
                onSelect = onSelectRemote,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthHeaderPillTab(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Surface(
        onClick = onSelect,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Color.White else Color.Transparent,
            focusedContainerColor = if (selected) Color.White else Color.White.copy(alpha = 0.15f),
            contentColor = if (selected) Color.Black else Color.White.copy(alpha = 0.75f),
            focusedContentColor = if (selected) Color.Black else Color.White,
        ),
        modifier = Modifier
            .height(32.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused && !selected) {
                    onSelect()
                }
            },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Text(
                text = label,
                fontFamily = BrewTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PhoneEmailOnboardingContent(
    method: AuthSignInMethod,
    step: AuthSignInStep,
    phoneDigits: String,
    email: String,
    countryLabel: String,
    otp: String,
    otpDisplay: String?,
    otpIssueChannel: AuthSignInMethod?,
    isLoading: Boolean,
    resendCountdown: Int,
    resendAttempts: Int,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onContinue: () -> Unit,
    onResend: () -> Unit,
    onBackToInput: () -> Unit,
    onCycleCountry: () -> Unit,
    onSwitchMode: () -> Unit,
) {
    val inputFocus = remember { FocusRequester() }

    LaunchedEffect(method, step) {
        kotlinx.coroutines.delay(120)
        runCatching { inputFocus.requestFocus() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(horizontal = 40.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (step == AuthSignInStep.Otp) {
            val channel = otpIssueChannel ?: method
            val otpLen = AuthValidation.expectedOtpLength(channel)
            Text(
                text = "Enter the $otpLen-digit code sent to\n${otpDisplay.orEmpty()}",
                color = Color.White,
                fontFamily = BrewTitle,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
            )
            AuthTextField(
                value = otp,
                onValueChange = onOtpChange,
                placeholder = "Enter OTP",
                keyboardType = KeyboardType.NumberPassword,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .width(320.dp)
                    .focusRequester(inputFocus),
            )
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuthSecondaryButton(
                    label = if (channel == AuthSignInMethod.Email) "Change email" else "Change phone",
                    onClick = onBackToInput,
                )
                val canResend = resendCountdown <= 0 && resendAttempts < 3
                AuthSecondaryButton(
                    label = when {
                        resendAttempts >= 3 -> "Try again later"
                        resendCountdown > 0 -> "Resend (${resendCountdown}s)"
                        else -> "Resend code"
                    },
                    onClick = onResend,
                    enabled = canResend && !isLoading,
                )
            }
            return@Column
        }

        Text(
            text = if (method == AuthSignInMethod.Phone) "Sign in with phone" else "Sign in with email",
            color = Color.White,
            fontFamily = BrewTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        )

        if (method == AuthSignInMethod.Phone) {
            Row(
                modifier = Modifier.padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuthSecondaryButton(label = countryLabel, onClick = onCycleCountry)
                AuthTextField(
                    value = phoneDigits,
                    onValueChange = onPhoneChange,
                    placeholder = "Phone number",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier
                        .width(280.dp)
                        .focusRequester(inputFocus),
                )
            }
        } else {
            AuthTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Email address",
                keyboardType = KeyboardType.Email,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .width(380.dp)
                    .focusRequester(inputFocus),
            )
        }

        Row(
            modifier = Modifier.padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuthPrimaryButton(
                label = if (isLoading) "Sending…" else "Continue",
                onClick = onContinue,
                enabled = !isLoading,
            )
            AuthSecondaryButton(
                label = if (method == AuthSignInMethod.Phone) "Use email instead" else "Use phone instead",
                onClick = onSwitchMode,
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.titleMedium.copy(
            color = Color.White,
            fontFamily = BrewTitle,
            fontSize = 18.sp,
        ),
        modifier = modifier
            .height(48.dp)
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = BrewTitle,
                        fontSize = 16.sp,
                    )
                }
                inner()
            }
        },
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthPrimaryButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = AccentYellow,
            focusedContainerColor = Color.White,
            contentColor = Color.Black,
            focusedContentColor = Color.Black,
        ),
        modifier = modifier.graphicsLayer { alpha = if (enabled) 1f else 0.5f },
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = label, fontFamily = BrewTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AuthSecondaryButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.22f),
            contentColor = Color.White,
            focusedContentColor = Color.White,
        ),
        modifier = modifier.graphicsLayer { alpha = if (enabled) 1f else 0.45f },
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = label, fontFamily = BrewTitle, fontWeight = FontWeight.Medium, fontSize = 12.sp)
        }
    }
}

object AuthScreenRoute {
    const val MethodBundleKey = "method"
}
