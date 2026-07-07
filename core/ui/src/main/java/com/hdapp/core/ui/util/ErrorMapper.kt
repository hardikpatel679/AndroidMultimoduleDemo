package com.hdapp.core.ui.util

import com.hdapp.domain.model.AppError
import com.hdapp.core.ui.R

fun AppError.toUiText(): UiText {
    return when (this) {
        is AppError.NetworkError -> UiText.StringResource(R.string.error_network)
        is AppError.ServerError -> UiText.StringResource(R.string.error_server)
        is AppError.Unauthorized -> UiText.StringResource(R.string.error_unauthorized)
        is AppError.ValidationError -> UiText.StringResource(R.string.error_empty_fields)
        is AppError.BusinessError -> UiText.DynamicString(this.message)
        is AppError.UnknownError -> UiText.StringResource(R.string.error_unknown)
    }
}
