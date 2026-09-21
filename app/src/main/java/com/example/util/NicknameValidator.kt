package com.example.util

object NicknameValidator {

    // Combining diacritical marks (Zalgo text stacking)
    private val ZALGO_REGEX = Regex("[\\p{M}\\u0300-\\u036F\\u1AB0-\\u1AFF\\u1DC0-\\u1DFF\\uFE20-\\uFE2F\\u20D0-\\u20FF]")

    // Invisible, zero-width, formatting and direction control characters
    private val INVISIBLE_REGEX = Regex("[\\u200B-\\u200F\\u2028-\\u202F\\u2060-\\u206F\\uFEFF\\p{Cf}]")

    // Emojis, pictorial symbols, dingbats, surrogate pairs
    private val EMOJI_REGEX = Regex(
        "[\\uD83C-\\uD83E][\\uDC00-\\uDFFF]" +
        "|[\\u2600-\\u27BF]" +
        "|[\\u1F300-\\u1F9FF]" +
        "|[\\u1FA00-\\u1FAFF]" +
        "|[\\u2300-\\u23FF]" +
        "|[\\u2B50-\\u2B55]" +
        "|[\\uFE0E-\\uFE0F]"
    )

    // Dangerous characters: HTML/XML brackets, slashes, quotes, SQL/script punctuation, operators
    private val DANGEROUS_CHARS_REGEX = Regex("[<>{}\\[\\]()\"'`\\\\/;+=*&%$#@!|~^]")

    // Whitelist pattern: Latin, Cyrillic (including extensions), digits, underscore, dash, dot, space
    private val ALLOWED_PATTERN = Regex("^[a-zA-Z0-9\\p{IsCyrillic}_\\-. ]+$")

    fun hasZalgo(text: String): Boolean {
        if (ZALGO_REGEX.containsMatchIn(text)) return true
        if (INVISIBLE_REGEX.containsMatchIn(text)) return true
        for (i in 0 until text.length) {
            val ch = text[i]
            val type = Character.getType(ch)
            if (type == Character.COMBINING_SPACING_MARK.toInt() ||
                type == Character.NON_SPACING_MARK.toInt() ||
                type == Character.ENCLOSING_MARK.toInt() ||
                type == Character.FORMAT.toInt() ||
                type == Character.CONTROL.toInt()
            ) {
                return true
            }
        }
        return false
    }

    fun hasEmoji(text: String): Boolean {
        if (EMOJI_REGEX.containsMatchIn(text)) return true
        var i = 0
        while (i < text.length) {
            val codePoint = text.codePointAt(i)
            if (codePoint > 0xFFFF) {
                // Supplementary plane contains almost all modern emojis
                return true
            }
            val ch = text[i]
            if (Character.isSurrogate(ch)) return true
            val type = Character.getType(ch)
            if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                if (codePoint in 0x2600..0x27BF || codePoint in 0x1F000..0x1FAFF) return true
            }
            i += Character.charCount(codePoint)
        }
        return false
    }

    fun hasDangerousChars(text: String): Boolean {
        return DANGEROUS_CHARS_REGEX.containsMatchIn(text)
    }

    fun validate(nickname: String): String? {
        val trimmed = nickname.trim()
        if (trimmed.length < 2) {
            return "Никнейм должен быть от 2 до 20 символов!"
        }
        if (trimmed.length > 20) {
            return "Никнейм должен быть от 2 до 20 символов!"
        }

        if (hasZalgo(nickname)) {
            return "Обнаружены Zalgo или невидимые символы! Запрещено."
        }

        if (hasEmoji(nickname)) {
            return "Смайлики и эмодзи в никнейме запрещены!"
        }

        if (hasDangerousChars(nickname)) {
            return "Запрещены опасные спецсимволы (<, >, /, \\, ;, ', \" и др.)"
        }

        if (!ALLOWED_PATTERN.matches(trimmed)) {
            return "Разрешены только буквы (RU/EN), цифры, пробел, точка, дефис и _"
        }

        if (trimmed.contains("  ")) {
            return "Запрещено использовать несколько пробелов подряд!"
        }

        return null
    }

    fun sanitize(raw: String): String {
        var clean = raw
            .replace(ZALGO_REGEX, "")
            .replace(INVISIBLE_REGEX, "")
            .replace(EMOJI_REGEX, "")
            .replace(DANGEROUS_CHARS_REGEX, "")
        clean = clean.filter { ch ->
            !Character.isISOControl(ch) &&
            !Character.isSurrogate(ch) &&
            Character.getType(ch) != Character.NON_SPACING_MARK.toInt() &&
            Character.getType(ch) != Character.COMBINING_SPACING_MARK.toInt() &&
            Character.getType(ch) != Character.ENCLOSING_MARK.toInt() &&
            Character.getType(ch) != Character.FORMAT.toInt()
        }
        return clean.trim().take(20)
    }
}
