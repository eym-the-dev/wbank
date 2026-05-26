package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekNavyHeader
import com.example.ui.theme.SleekDigitalBlue
import com.example.ui.theme.SleekSecondaryBlue
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.GoldAccent

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AuthUiEvent.NavigateToDashboard -> {
                    onNavigateToDashboard()
                }
            }
        }
    }

    val glowColor1 = SleekDigitalBlue.copy(alpha = 0.15f)
    val glowColor2 = GoldAccent.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0E))
            .drawBehind {
                // Top-Left subtle blue glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor1, Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.8f
                    ),
                    radius = size.width * 0.8f,
                    center = Offset(0f, 0f)
                )
                // Bottom-Right gold glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor2, Color.Transparent),
                        center = Offset(size.width, size.height),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width, size.height)
                )

                // Technical horizontal grids/guidelines for high-tech bank look
                val gridOpacity = 0.04f
                val step = 40.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color.White.copy(alpha = gridOpacity),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = gridOpacity),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Branding Icon & Text
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SleekDigitalBlue)
                    .clickable { /* Brand tap */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "W",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "W BANK",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Secure Cryptographical Onboarding Terminal",
                color = TextSecondarySlate,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card Form Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_form_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161618)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Mode Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF28282B)),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabHeader(
                            text = "Login",
                            selected = uiState.isLogin,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("login_tab_btn")
                                .clickable { if (!uiState.isLogin) viewModel.toggleAuthMode() }
                        )
                        TabHeader(
                            text = "Sign Up",
                            selected = !uiState.isLogin,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("signup_tab_btn")
                                .clickable { if (uiState.isLogin) viewModel.toggleAuthMode() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(visible = !uiState.isLogin) {
                        Column {
                            // First Name
                            OutlinedTextField(
                                value = uiState.firstName,
                                onValueChange = viewModel::onFirstNameChanged,
                                label = { Text("First Name") },
                                isError = uiState.firstNameError != null,
                                supportingText = { uiState.firstNameError?.let { Text(it) } },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "First Name Icon", tint = SleekDigitalBlue) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("firstname_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SleekDigitalBlue,
                                    unfocusedBorderColor = Color(0xFF28282B),
                                    cursorColor = SleekDigitalBlue,
                                    focusedLabelColor = SleekDigitalBlue,
                                    unfocusedLabelColor = TextSecondarySlate
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Last Name
                            OutlinedTextField(
                                value = uiState.lastName,
                                onValueChange = viewModel::onLastNameChanged,
                                label = { Text("Last Name") },
                                isError = uiState.lastNameError != null,
                                supportingText = { uiState.lastNameError?.let { Text(it) } },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Last Name Icon", tint = SleekDigitalBlue) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lastname_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SleekDigitalBlue,
                                    unfocusedBorderColor = Color(0xFF28282B),
                                    cursorColor = SleekDigitalBlue,
                                    focusedLabelColor = SleekDigitalBlue,
                                    unfocusedLabelColor = TextSecondarySlate
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // National ID
                            OutlinedTextField(
                                value = uiState.nationalId,
                                onValueChange = viewModel::onNationalIdChanged,
                                label = { Text("National ID (11 digits)") },
                                isError = uiState.nationalIdError != null,
                                supportingText = { uiState.nationalIdError?.let { Text(it) } },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = "ID Icon", tint = SleekDigitalBlue) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("nationalid_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SleekDigitalBlue,
                                    unfocusedBorderColor = Color(0xFF28282B),
                                    cursorColor = SleekDigitalBlue,
                                    focusedLabelColor = SleekDigitalBlue,
                                    unfocusedLabelColor = TextSecondarySlate
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // Email Field (Visible in both)
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChanged,
                        label = { Text("Corporate Email Address") },
                        isError = uiState.emailError != null,
                        supportingText = { uiState.emailError?.let { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = SleekDigitalBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SleekDigitalBlue,
                            unfocusedBorderColor = Color(0xFF28282B),
                            cursorColor = SleekDigitalBlue,
                            focusedLabelColor = SleekDigitalBlue,
                            unfocusedLabelColor = TextSecondarySlate
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Password Field (Visible in both)
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text("Password (min 6 characters)") },
                        isError = uiState.passwordError != null,
                        supportingText = { uiState.passwordError?.let { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon", tint = SleekDigitalBlue) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SleekDigitalBlue,
                            unfocusedBorderColor = Color(0xFF28282B),
                            cursorColor = SleekDigitalBlue,
                            focusedLabelColor = SleekDigitalBlue,
                            unfocusedLabelColor = TextSecondarySlate
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Action Button
                    Button(
                        onClick = viewModel::submit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekDigitalBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (uiState.isLogin) "SECURE LOG IN" else "ENROLL & SIGN UP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Footer simulated warning text
            Text(
                text = "Federal regulation guarantees simulation safety up to $250,000.",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Full Page Network delay loading overlay
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = SleekDigitalBlue,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (uiState.isLogin) "Connecting to W Bank Network..." else "Provisioning Cryptographic IBAN Account...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Simulating real-world latency (1.2s)...",
                        color = SleekDigitalBlue.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

val TextPrimaryState = Color(0xFFF8FAFC)

@Composable
fun TabHeader(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) SleekDigitalBlue else Color.Transparent)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextSecondarySlate,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
