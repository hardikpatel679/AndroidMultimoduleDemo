package com.hdapp.androidmultimodule

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Check if package name starts with our base ID to be flavor-independent
        val expectedBase = "com.hdapp.androidmultimodule"
        assertTrue("Package name ${appContext.packageName} should start with $expectedBase",
            appContext.packageName.startsWith(expectedBase))
    }
}