package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.WBankAccount
import com.example.domain.model.WBankUser
import com.example.ui.dashboard.PlatinumCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val dummyUser = WBankUser(
        customerNumber = "88888888",
        firstName = "Elena",
        lastName = "Thorne",
        nationalId = "11122233344",
        email = "elena.thorne@wbank.com",
        passwordHash = "xxx",
        mainAccountIban = "WBNK-TR44-8888-9999-7777-6666-55"
    )
    val dummyAccount = WBankAccount(
        iban = "WBNK-TR44-8888-9999-7777-6666-55",
        customerNumber = "88888888",
        balance = 250000.00,
        currency = "USD"
    )

    composeTestRule.setContent {
        MyApplicationTheme {
            PlatinumCard(
                account = dummyAccount,
                user = dummyUser,
                onCopyIban = {}
            )
        }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
