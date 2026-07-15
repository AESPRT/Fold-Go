package com.aesprt.foldgo.core.util

import androidx.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(name = "Phone", device = "spec:width=360dp,height=800dp,dpi=440")
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
annotation class DevicePreviews

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@DevicePreviews
annotation class ScreenPreviews
