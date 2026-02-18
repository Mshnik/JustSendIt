package com.redpup.justsendit.util

/** Title cases a string. */
fun String.toTitleCase() = lowercase().replaceFirstChar { it.uppercase() }
