package com.fae.adminportal.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility for generating URL-safe slugs from arbitrary strings.
 *
 * <p>Handles:
 * <ul>
 *   <li>Latin letters (accented → ASCII) and digits</li>
 *   <li>Arabic script — preserves Arabic letters instead of dropping them</li>
 *   <li>Whitespace / punctuation / diacritics → single hyphen</li>
 *   <li>Multiple consecutive separators collapsed, trimmed from ends</li>
 *   <li>Length capped at 200 chars (safe under VARCHAR(255))</li>
 * </ul>
 */
public final class SlugUtils {

    /** Max slug length. Fits comfortably in a VARCHAR(255) column. */
    public static final int MAX_LENGTH = 200;

    /** Fallback slug when input produces nothing usable. */
    public static final String FALLBACK = "item";

    private static final Pattern NON_SLUG =
            Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsArabic}\\p{IsCyrillic}]+");

    private static final Pattern MULTI_HYPHEN = Pattern.compile("-{2,}");

    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-+|-+$");

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    private SlugUtils() {
        // utility class
    }

    /**
     * Generate a slug from a source string.
     * Never returns {@code null} or empty — falls back to {@link #FALLBACK}.
     */
    public static String toSlug(String source) {
        if (source == null) {
            return FALLBACK;
        }

        // 1. Normalize unicode and strip combining diacritics (é → e, ü → u …)
        String normalized = Normalizer.normalize(source, Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");

        // 2. Lowercase (Locale.ROOT to avoid Turkish İ → i̇ surprises)
        String lower = normalized.toLowerCase(Locale.ROOT).trim();

        // 3. Replace anything not letter/digit (incl. Arabic, Cyrillic) with '-'
        String dashed = NON_SLUG.matcher(lower).replaceAll("-");

        // 4. Collapse and trim hyphens
        String cleaned = MULTI_HYPHEN.matcher(dashed).replaceAll("-");
        cleaned = EDGE_HYPHENS.matcher(cleaned).replaceAll("");

        // 5. Truncate without leaving a trailing hyphen
        if (cleaned.length() > MAX_LENGTH) {
            cleaned = cleaned.substring(0, MAX_LENGTH);
            cleaned = EDGE_HYPHENS.matcher(cleaned).replaceAll("");
        }

        return cleaned.isEmpty() ? FALLBACK : cleaned;
    }

    /**
     * Generate a unique slug, appending {@code -2}, {@code -3}, … until
     * {@code existsCheck} returns {@code false}.
     *
     * @param source      base string to slugify
     * @param existsCheck predicate returning {@code true} if the slug is already taken
     */
    public static String toUniqueSlug(String source, java.util.function.Predicate<String> existsCheck) {
        String base = toSlug(source);
        if (existsCheck == null || !existsCheck.test(base)) {
            return base;
        }
        int suffix = 2;
        String candidate;
        do {
            candidate = base + "-" + suffix++;
        } while (existsCheck.test(candidate));
        return candidate;
    }

    /**
     * True if the given string is already a valid slug (i.e. {@code toSlug(s).equals(s)}).
     * Empty/null returns {@code false}.
     */
    public static boolean isValid(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }
        return slug.equals(toSlug(slug)) && slug.length() <= MAX_LENGTH;
    }
}