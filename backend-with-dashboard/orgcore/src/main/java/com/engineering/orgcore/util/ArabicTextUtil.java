package com.engineering.orgcore.util;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;

/**
 * Utility that applies full Arabic shaping (ligature joining) and
 * logical-to-visual BiDi reordering so iText can render the string
 * left-to-right while it looks correct to the reader.
 *
 * Steps:
 *  1. ArabicShaping – joins isolated letters into their contextual forms
 *     (initial / medial / final / isolated).
 *  2. ICU Bidi – reorders the runs from logical (keyboard) order to
 *     visual (display) order so the rightmost word ends up at the left
 *     of the text stream that iText lays out LTR.
 */
public final class ArabicTextUtil {

    private ArabicTextUtil() {}

    private static final ArabicShaping SHAPER = new ArabicShaping(
            ArabicShaping.LETTERS_SHAPE
                    | ArabicShaping.TEXT_DIRECTION_LOGICAL
                    | ArabicShaping.LENGTH_FIXED_SPACES_NEAR
    );

    /**
     * Reshape and visually reorder an Arabic (or mixed) string.
     * Pass every string that may contain Arabic before giving it to iText.
     * Latin / numeric content is preserved untouched by the algorithm.
     */
    public static String reshape(String text) {
        if (text == null || text.isBlank()) return text;

        // 1. Shape: connect letters into contextual glyph forms
        String shaped;
        try {
            shaped = SHAPER.shape(text);
        } catch (ArabicShapingException e) {
            shaped = text; // fallback – better than crashing
        }

        // 2. Reorder: logical → visual using ICU Bidi
        Bidi bidi = new Bidi();
        bidi.setPara(shaped, Bidi.RTL, null);
        return bidi.writeReordered(Bidi.DO_MIRRORING | Bidi.REMOVE_BIDI_CONTROLS);
    }

    /** Convenience: returns true if the string contains at least one Arabic character. */
    public static boolean containsArabic(String text) {
        if (text == null) return false;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC) return true;
        }
        return false;
    }

    /**
     * Reshape only if the string contains Arabic, otherwise return as-is.
     * Useful for mixed product names / branch names that may be Latin.
     */
    public static String reshapeIfArabic(String text) {
        return containsArabic(text) ? reshape(text) : text;
    }
}
