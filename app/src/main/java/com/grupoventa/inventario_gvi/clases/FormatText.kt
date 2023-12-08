package com.grupoventa.inventario_gvi.clases

import com.grupoventa.inventario_gvi.data.model.ItemSAP
import java.text.Normalizer
import javax.inject.Inject

class FormatText @Inject constructor() {
    fun removeSpecialCharsAndAccents(input: String): String {
        val _input = input.substringBefore("@")
        val normalizedInput = Normalizer.normalize(_input, Normalizer.Form.NFD)
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

    fun returnType(term: String, callback: (String, String) -> Unit) {
        val (result, termFormatted) = when {
            term.contains("|") -> {
                "SkuAndLot" to term.substringBefore("|")
            }
            term.length in setOf(42, 44, 43, 45) -> {
                val length = when (term.length) {
                    42 -> 8
                    44 -> 10
                    43 -> 9
                    45 -> 10
                    else -> 0 // Esta rama no debería alcanzarse, solo para evitar un valor no inicializado
                }
                "Ser" to term.takeLast(length).uppercase()
            }
            else -> {
                "SkuOrCod" to term
            }
        }
        callback(result, termFormatted)
    }


}