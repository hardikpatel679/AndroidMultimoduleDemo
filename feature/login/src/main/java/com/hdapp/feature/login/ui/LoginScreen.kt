package com.hdapp.feature.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.hdapp.core.ui.components.AppButton
import com.hdapp.core.ui.components.AppOutlinedButton
import com.hdapp.core.ui.components.AppTextField
import com.hdapp.core.ui.theme.LocalDimens
import com.hdapp.core.ui.util.UiText
import com.hdapp.feature.login.R
import com.hdapp.feature.login.mvi.LoginIntent
import com.hdapp.feature.login.mvi.LoginState
import com.hdapp.feature.login.mvi.LoginViewModel
import com.hdapp.feature.login.mvi.LoginEffect
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.tooling.preview.Preview
import com.hdapp.core.ui.theme.AppTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToDashboard: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    // Stable lambda for navigating
    val navigateAction = remember(onNavigateToDashboard) {
        { username: String -> onNavigateToDashboard(username) }
    }

    // Handle Effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LoginEffect.NavigateToDashboard -> {
                    navigateAction(state.username)
                }
                is LoginEffect.ShowToast -> {
                    // Handle toast if needed
                }
            }
        }
    }

    // Stable intent handler
    val onIntent: (LoginIntent) -> Unit = remember(viewModel) {
        { intent -> viewModel.onIntent(intent) }
    }

    LoginContent(
        state = state,
        onIntent = onIntent
    )
}

@Composable
fun LoginContent(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit
) {
    val dimens = LocalDimens.current
    
    val onUsernameChange = remember(onIntent) {
        { value: String -> onIntent(LoginIntent.UsernameChanged(value)) }
    }
    val onPasswordChange = remember(onIntent) {
        { value: String -> onIntent(LoginIntent.PasswordChanged(value)) }
    }
    val onToggleVisibility = remember(onIntent) {
        { onIntent(LoginIntent.TogglePasswordVisibility) }
    }
    val onLoginClick = remember(onIntent) {
        { onIntent(LoginIntent.LoginClicked) }
    }
    val onClearError = remember(onIntent) {
        { onIntent(LoginIntent.ClearError) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.paddingLarge)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginHeader(dimens)

        LoginInputs(
            username = state.username,
            password = state.password,
            isPasswordVisible = state.isPasswordVisible,
            isError = state.error != null,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onToggleVisibility = onToggleVisibility,
            dimens = dimens
        )
        
        if (state.error != null) {
            LoginErrorDialog(state.error, onClearError)
        }

        LoginActions(
            isLoading = state.isLoading,
            onLoginClick = onLoginClick,
            dimens = dimens
        )

        LoginSocialSection(dimens)

        LoginFooter(dimens)
    }
}

@Composable
private fun LoginHeader(dimens: com.hdapp.core.ui.theme.Dimens) {
    Text(
        text = stringResource(R.string.login_welcome_back),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        text = stringResource(R.string.login_to_your_account),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = dimens.extraLarge)
    )
}

@Composable
private fun LoginInputs(
    username: String,
    password: String,
    isPasswordVisible: Boolean,
    isError: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    dimens: com.hdapp.core.ui.theme.Dimens
) {
    AppTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = stringResource(R.string.login_username),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        isError = isError
    )

    Spacer(modifier = Modifier.height(dimens.medium))

    AppTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = stringResource(R.string.login_password),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (isPasswordVisible) 
                stringResource(R.string.login_hide_password) 
            else 
                stringResource(R.string.login_show_password)

            IconButton(onClick = onToggleVisibility) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        isError = isError
    )
}

@Composable
private fun LoginActions(
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    dimens: com.hdapp.core.ui.theme.Dimens
) {
    TextButton(
        onClick = { /* TODO */ },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(stringResource(R.string.login_forgot_password))
        }
    }

    Spacer(modifier = Modifier.height(dimens.large))

    AppButton(
        text = stringResource(R.string.login_button),
        onClick = onLoginClick,
        isLoading = isLoading,
        enabled = !isLoading
    )
}

@Composable
private fun LoginSocialSection(dimens: com.hdapp.core.ui.theme.Dimens) {
    Spacer(modifier = Modifier.height(dimens.large))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.login_or_continue_with),
            modifier = Modifier.padding(horizontal = dimens.small),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(dimens.large))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AppOutlinedButton(
            text = stringResource(R.string.login_google),
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f).padding(end = dimens.small)
        )
        AppOutlinedButton(
            text = stringResource(R.string.login_apple),
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f).padding(start = dimens.small)
        )
    }
}

@Composable
private fun LoginFooter(dimens: com.hdapp.core.ui.theme.Dimens) {
    Spacer(modifier = Modifier.height(dimens.extraLarge))

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.login_dont_have_account))
        TextButton(onClick = { /* TODO */ }) {
            Text(
                text = stringResource(R.string.login_sign_up),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoginErrorDialog(
    error: UiText,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.login_error_title)) },
        text = { Text(text = error.asString()) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.login_ok))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginContentPreview() {
    AppTheme {
        LoginContent(
            state = LoginState(
                username = "testuser",
                isLoading = false
            ),
            onIntent = {}
        )
    }
}
