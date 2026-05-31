package com.hdapp.core.ui.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UiTextTest {

    @Test
    fun `DynamicString returns correct value`() {
        val value = "Hello"
        val uiText = UiText.DynamicString(value)
        assertThat(uiText.value).isEqualTo(value)
    }

    @Test
    fun `StringResource preserves properties`() {
        val resId = 123
        val arg = "Arg"
        val uiText = UiText.StringResource(resId, arg)
        
        assertThat(uiText.resId).isEqualTo(resId)
        assertThat(uiText.args).asList().containsExactly(arg)
    }
}
