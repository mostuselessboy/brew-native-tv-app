package com.google.jetstream.presentation.screens.profile

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
import androidx.compose.runtime.Immutable
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
import com.google.jetstream.data.auth.AuthCountries
import com.google.jetstream.data.auth.AuthSignInMethod
import com.google.jetstream.data.auth.AuthSignInStep
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.BrewTitle
import kotlinx.coroutines.delay

private val AccentYellow = Color(0xFFFFC15E)
private const val WatchHiddenGemsUrl =
    "https://createstir.b-cdn.net/stir-static/watch-hidden-gems.png"

@Immutable
data class AccountsSectionData(
    val title: String,
    val value: String? = null,
    val onClick: () -> Unit = {},
    /** Read-only tiles (signed-in profile summary) are not focusable. */
    val focusable: Boolean = true,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AccountsSection(
    onSignInPhone: () -> Unit,
    onSignInEmail: () -> Unit,
    panelFocusRequester: FocusRequester? = null,
    viewModel: AccountsViewModel = hiltViewModel(),
    authViewModel: com.google.jetstream.presentation.screens.auth.AuthViewModel = hiltViewModel(),
) {
    val childPadding = rememberChildPadding()
    val authState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val defaultPanelFocus = remember { FocusRequester() }
    val panelFocus = panelFocusRequester ?: defaultPanelFocus

    val inputFormFocusRequester = remember { FocusRequester() }
    val otpFormFocusRequester = remember { FocusRequester() }
    val otpFormContainerFocusRequester = remember { FocusRequester() }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    LaunchedEffect(authUiState.method, authUiState.step) {
        if (authUiState.method != AuthSignInMethod.Qr) {
            delay(200)
            if (authUiState.step == AuthSignInStep.Input) {
                runCatching { inputFormFocusRequester.requestFocus() }
            } else {
                keyboardController?.hide()
                runCatching { otpFormContainerFocusRequester.requestFocus() }
            }
        } else {
            delay(150)
            runCatching { panelFocus.requestFocus() }
        }
    }

    if (authState.isSignedIn) {
        val accountsSectionListItems = remember(authState) {
            val user = authState.user
            listOf(
                AccountsSectionData(
                    title = user?.displayName ?: "Signed in",
                    value = user?.email ?: user?.phone ?: "Brew account",
                    focusable = false,
                ),
                AccountsSectionData(
                    title = StringConstants.Composable.Placeholders.AccountsSelectionLogOut,
                    value = "Sign out of Brew",
                    onClick = { viewModel.signOut() },
                ),
                AccountsSectionData(
                    title = StringConstants.Composable.Placeholders
                        .AccountsSelectionChangePasswordTitle,
                    value = StringConstants.Composable.Placeholders.AccountsSelectionChangePasswordValue,
                ),
                AccountsSectionData(
                    title = StringConstants.Composable.Placeholders
                        .AccountsSelectionViewSubscriptionsTitle,
                ),
                AccountsSectionData(
                    title = StringConstants.Composable.Placeholders.AccountsSelectionDeleteAccountTitle,
                    onClick = { showDeleteDialog = true },
                ),
            )
        }
        val firstFocusIndex = accountsSectionListItems.indexOfFirst { it.focusable }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(520.dp)
                    .wrapContentHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E),
                                Color(0xFF121212),
                                Color(0xFF0A0A0A),
                            ),
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Account Settings",
                    color = Color.White,
                    fontFamily = BrewTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                accountsSectionListItems.forEachIndexed { index, item ->
                    AccountsSelectionItem(
                        modifier = Modifier.then(
                            if (index == firstFocusIndex && firstFocusIndex >= 0) {
                                Modifier.focusRequester(panelFocus)
                            } else {
                                Modifier
                            },
                        ),
                        key = index,
                        accountsSectionData = item,
                    )
                }
            }
        }

        AccountsSectionDeleteDialog(
            showDialog = showDeleteDialog,
            onDismissRequest = { showDeleteDialog = false },
            modifier = Modifier.width(428.dp),
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(680.dp)
                    .wrapContentHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E),
                                Color(0xFF121212),
                                Color(0xFF0A0A0A),
                            ),
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (authUiState.method) {
                    AuthSignInMethod.Qr -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            // Left Column: QR Code steps
                            Column(
                                modifier = Modifier.weight(1.3f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                QrOnboardingContent(
                                    qrUrl = authUiState.qrUrl,
                                    qrCode = authUiState.qrCode,
                                    qrExpired = authUiState.qrExpired,
                                    isLoading = authUiState.isLoading,
                                    onRefreshQr = authViewModel::generateQrCode,
                                )
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .height(240.dp)
                                    .width(1.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )

                            // Right Column: Sign in header & buttons
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BrewAuthBrandRow()

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Sign in to Brew",
                                    color = Color.White,
                                    fontFamily = BrewTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    letterSpacing = (-0.8).sp,
                                )

                                Text(
                                    text = "Cinema for intelligent folks.",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontFamily = BrewTitle,
                                    fontSize = 12.sp,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Or sign in using:",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = BrewTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )

                                AuthPrimaryButton(
                                    label = "Sign in with Email",
                                    onClick = { authViewModel.selectMethod(AuthSignInMethod.Email) },
                                    modifier = Modifier.fillMaxWidth().focusRequester(panelFocus)
                                )

                                AuthSecondaryButton(
                                    label = "Sign in with Phone",
                                    onClick = { authViewModel.selectMethod(AuthSignInMethod.Phone) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                authUiState.errorMessage?.let { error ->
                                    Text(
                                        text = error,
                                        color = Color(0xFFFF6B6B),
                                        fontFamily = BrewTitle,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                    AuthSignInMethod.Email, AuthSignInMethod.Phone -> {
                        val isEmail = authUiState.method == AuthSignInMethod.Email
                        if (authUiState.step == AuthSignInStep.Input) {
                            // Input Step (Email or Phone Number)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BrewAuthBrandRow()

                                Text(
                                    text = if (isEmail) "Enter Email Address" else "Enter Phone Number",
                                    color = Color.White,
                                    fontFamily = BrewTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                )

                                Text(
                                    text = "We will send a one-time verification code to your device.",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontFamily = BrewTitle,
                                    fontSize = 13.sp,
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                if (isEmail) {
                                    TVTextInput(
                                        value = authUiState.email,
                                        onValueChange = { authViewModel.updateEmail(it) },
                                        placeholder = "yourname@domain.com",
                                        keyboardType = KeyboardType.Email,
                                        containerFocusRequester = inputFormFocusRequester,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Country Selector Button
                                        Surface(
                                            onClick = { authViewModel.cycleCountry() },
                                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                                            colors = ClickableSurfaceDefaults.colors(
                                                containerColor = Color.White.copy(alpha = 0.1f),
                                                focusedContainerColor = Color.White.copy(alpha = 0.22f),
                                                contentColor = Color.White,
                                                focusedContentColor = Color.White,
                                            ),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(horizontal = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "+${authUiState.selectedCountry.dialCode} (${authUiState.selectedCountry.code})",
                                                    fontFamily = BrewTitle,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        // Phone Input Field
                                        TVTextInput(
                                            value = authUiState.phoneDigits,
                                            onValueChange = { authViewModel.updatePhoneDigits(it) },
                                            placeholder = "0000000000",
                                            keyboardType = KeyboardType.Number,
                                            containerFocusRequester = inputFormFocusRequester,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                authUiState.errorMessage?.let { error ->
                                    Text(
                                        text = error,
                                        color = Color(0xFFFF6B6B),
                                        fontFamily = BrewTitle,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AuthPrimaryButton(
                                        label = "Send Code",
                                        onClick = { authViewModel.sendOtp() },
                                        enabled = if (isEmail) authUiState.email.isNotBlank() else authUiState.phoneDigits.isNotBlank()
                                    )

                                    AuthSecondaryButton(
                                        label = "Back to QR",
                                        onClick = { authViewModel.selectMethod(AuthSignInMethod.Qr) }
                                    )
                                }
                            }
                        } else {
                            // OTP Step
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BrewAuthBrandRow()

                                Text(
                                    text = "Enter Verification Code",
                                    color = Color.White,
                                    fontFamily = BrewTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                )

                                Text(
                                    text = "Sent to ${authUiState.otpSentDisplay ?: "your device"}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontFamily = BrewTitle,
                                    fontSize = 13.sp,
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Hidden input field to capture keys
                                val otpLength = if (authUiState.otpIssueChannel == AuthSignInMethod.Email) 6 else 4
                                var isOtpKeyboardActive by remember { mutableStateOf(false) }

                                Surface(
                                    onClick = {
                                        isOtpKeyboardActive = true
                                        runCatching { otpFormFocusRequester.requestFocus() }
                                        keyboardController?.show()
                                    },
                                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                                    colors = ClickableSurfaceDefaults.colors(
                                        containerColor = Color.Transparent,
                                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                        contentColor = Color.White,
                                        focusedContentColor = Color.White,
                                    ),
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .focusRequester(otpFormContainerFocusRequester)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(6.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        // Styled code digits row
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (i in 0 until otpLength) {
                                                val char = authUiState.otp.getOrNull(i)?.toString() ?: ""
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 40.dp, height = 50.dp)
                                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = char,
                                                        color = Color.White,
                                                        fontFamily = BrewTitle,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 22.sp,
                                                    )
                                                }
                                            }
                                        }

                                        // Real TextField overlaid transparently
                                        BasicTextField(
                                            value = authUiState.otp,
                                            onValueChange = { authViewModel.updateOtp(it) },
                                            readOnly = !isOtpKeyboardActive,
                                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent),
                                            cursorBrush = SolidColor(Color.Transparent),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .size(width = (48 * otpLength).dp, height = 50.dp)
                                                .focusRequester(otpFormFocusRequester)
                                                .onKeyEvent { event ->
                                                    if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                                                        if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP) {
                                                            isOtpKeyboardActive = false
                                                            keyboardController?.hide()
                                                            runCatching { otpFormContainerFocusRequester.requestFocus() }
                                                        }
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                }
                                        )
                                    }
                                }

                                authUiState.errorMessage?.let { error ->
                                    Text(
                                        text = error,
                                        color = Color(0xFFFF6B6B),
                                        fontFamily = BrewTitle,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (authUiState.resendCountdown > 0) {
                                        Text(
                                            text = "Resend in ${authUiState.resendCountdown}s",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontFamily = BrewTitle,
                                            fontSize = 13.sp,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    } else {
                                        AuthSecondaryButton(
                                            label = "Resend Code",
                                            onClick = { authViewModel.resendOtp() }
                                        )
                                    }

                                    AuthSecondaryButton(
                                        label = "Back to Input",
                                        onClick = { authViewModel.resetOtpStep() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
                .height(28.dp)
                .width(42.dp),
            contentScale = ContentScale.Fit,
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color.White.copy(alpha = 0.3f)),
        )
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(WatchHiddenGemsUrl)
                .crossfade(false)
                .build(),
            contentDescription = "Watch hidden gems",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(32.dp)
                .width(72.dp),
        )
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Step 1 Column
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            OnboardingStepHeader(
                step = 1,
                text = "Point your camera to this code, or go to brew.tv/tv2",
            )
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isLoading && qrUrl == null -> Loading(modifier = Modifier.size(110.dp))
                        qrExpired -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Expired",
                                    color = Color.Black.copy(alpha = 0.7f),
                                    fontFamily = BrewTitle,
                                    fontSize = 12.sp,
                                )
                                AuthPrimaryButton(
                                    label = "Refresh",
                                    onClick = onRefreshQr,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                        qrUrl != null -> {
                            val bitmap = remember(qrUrl) { QrCodeGenerator.createBitmap(qrUrl, 512) }
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR sign in",
                                modifier = Modifier.size(110.dp),
                            )
                        }
                    }
                }
                if (!qrExpired && qrUrl != null) {
                    AuthSecondaryButton(
                        label = "Refresh QR",
                        onClick = onRefreshQr,
                    )
                }
            }
        }

        // Step 2 Column
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            OnboardingStepHeader(
                step = 2,
                text = "Confirm this code on your device",
            )
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isLoading || qrCode.isNullOrBlank()) {
                    Loading(modifier = Modifier.size(36.dp))
                } else {
                    qrCode.forEach { char ->
                        Box(
                            modifier = Modifier
                                .size(width = 30.dp, height = 40.dp)
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = char.toString(),
                                color = Color.White,
                                fontFamily = BrewTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
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
