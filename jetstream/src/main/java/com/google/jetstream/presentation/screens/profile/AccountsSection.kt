package com.google.jetstream.presentation.screens.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding

@Immutable
data class AccountsSectionData(
    val title: String,
    val value: String? = null,
    val onClick: () -> Unit = {},
    /** Read-only tiles (signed-in profile summary) are not focusable. */
    val focusable: Boolean = true,
)

@Composable
fun AccountsSection(
    onSignInPhone: () -> Unit = {},
    onSignInEmail: () -> Unit = {},
    panelFocusRequester: FocusRequester? = null,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val childPadding = rememberChildPadding()
    val authState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val defaultPanelFocus = remember { FocusRequester() }
    val panelFocus = panelFocusRequester ?: defaultPanelFocus

    val accountsSectionListItems = remember(authState, onSignInPhone, onSignInEmail) {
        if (authState.isSignedIn) {
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
        } else {
            listOf(
                AccountsSectionData(
                    title = "Sign in with Phone",
                    value = "We'll text you a 4-digit code",
                    onClick = onSignInPhone,
                ),
                AccountsSectionData(
                    title = "Sign in with Email",
                    value = "Code sent to your inbox",
                    onClick = onSignInEmail,
                ),
                AccountsSectionData(
                    title = StringConstants.Composable.Placeholders
                        .AccountsSelectionSwitchAccountsTitle,
                    value = StringConstants.Composable.Placeholders.AccountsSelectionSwitchAccountsEmail,
                ),
                AccountsSectionData(
                    title = StringConstants.Composable.Placeholders.AccountsSelectionAddNewAccountTitle,
                ),
            )
        }
    }

    val firstFocusIndex = accountsSectionListItems.indexOfFirst { it.focusable }

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = childPadding.start),
        columns = GridCells.Fixed(2),
        content = {
            items(accountsSectionListItems.size) { index ->
                val item = accountsSectionListItems[index]
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
        },
    )

    AccountsSectionDeleteDialog(
        showDialog = showDeleteDialog,
        onDismissRequest = { showDeleteDialog = false },
        modifier = Modifier.width(428.dp),
    )
}
