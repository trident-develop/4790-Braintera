package com.com2us.wannabe.android.google.global.nor.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun Transaction(onMove: () -> Unit) {
    LaunchedEffect(Unit) {
        onMove()
    }
}
