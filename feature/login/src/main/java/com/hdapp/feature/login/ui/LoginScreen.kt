package com.hdapp.feature.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.hdapp.core.ui.components.AppButton
import com.hdapp.core.ui.components.AppOutlinedButton
import com.hdapp.core.ui.components.AppTextField
import com.hdapp.core.ui.theme.LocalDimens
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
    val state by viewModel.state.collectAsState()
    
    // Handle Effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LoginEffect.NavigateToDashboard -> {
                    onNavigateToDashboard(state.username)
                }
                is LoginEffect.ShowToast -> {
                    // Handle toast if needed
                }
            }
        }
    }

    LoginContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun LoginContent(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit
) {
    val dimens = LocalDimens.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

        AppTextField(
            value = state.username,
            onValueChange = { onIntent(LoginIntent.UsernameChanged(it)) },
            label = stringResource(R.string.login_username),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = state.error != null
        )

        Spacer(modifier = Modifier.height(dimens.medium))

        AppTextField(
            value = state.password,
            onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
            label = stringResource(R.string.login_password),
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (state.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (state.isPasswordVisible) 
                    stringResource(R.string.login_hide_password) 
                else 
                    stringResource(R.string.login_show_password)

                IconButton(onClick = { onIntent(LoginIntent.TogglePasswordVisibility) }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            isError = state.error != null
        )
        
        if (state.error != null) {
            AlertDialog(
                onDismissRequest = { onIntent(LoginIntent.ClearError) },
                title = { Text(text = stringResource(R.string.login_error_title)) },
                text = { Text(text = state.error.asString()) },
                confirmButton = {
                    TextButton(onClick = { onIntent(LoginIntent.ClearError) }) {
                        Text(text = stringResource(R.string.login_ok))
                    }
                }
            )
        }

        TextButton(
            onClick = { /* TODO */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.login_forgot_password))
        }

        Spacer(modifier = Modifier.height(dimens.large))

        AppButton(
            text = stringResource(R.string.login_button),
            onClick = { onIntent(LoginIntent.LoginClicked) },
            isLoading = state.isLoading,
            enabled = !state.isLoading
        )

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
