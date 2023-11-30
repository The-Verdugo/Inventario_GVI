package com.grupoventa.inventario_gvi.clases

import java.text.Normalizer

class FormatText {
    fun removeSpecialCharsAndAccents(input: String): String {
        val normalizedInput = Normalizer.normalize(input, Normalizer.Form.NFD)
        val stringBuilder = StringBuilder()
        var skipNextChar = false

        for (i in normalizedInput.indices) {
            if (!skipNextChar) {
                val c = normalizedInput[i]
                if (c == '\u0301' || c == '\u0300' || c == '\u0302' || c == '\u0303') {
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.deleteCharAt(stringBuilder.length - 1)
                    }
                    skipNextChar = true
                } else if (c == '@'|| c == '-' || c.isLetterOrDigit() || c.isWhitespace()) {
                    stringBuilder.append(c)
                }
            } else {
                skipNextChar = false
            }
        }

        return stringBuilder.toString()
    }
}