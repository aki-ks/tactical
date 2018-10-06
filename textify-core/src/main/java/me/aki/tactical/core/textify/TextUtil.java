package me.aki.tactical.core.textify;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class TextUtil {
    private final static Set<String> KEYWORDS = new HashSet<>(Set.of(
            "package",

            // class/field/method flag keywords
            "public", "private", "protected", "final", "static", "throws", "synchronized", "bridge",
            "varargs", "strict", "native", "volatile", "transitive", "synthetic", "deprecated",
            "open", "mandated", "static-phase",

            // class keywords
            "class", "interface", "enum", "module",
            "inner", "enclosing", "nest",

            // type keywords
            "boolean", "byte", "short", "char", "int", "long", "float", "double", "void", "null",

            // insn keywords
            "goto", "return", "throw", "monitor", "new", "length", "instanceof", "try", "catch",

            // module keywords
            "version", "main", "requires", "export", "opens", "uses", "provides", "with", "to"
    ));

    /**
     * Is it necessary to escaped the string:
     * - the string is empty
     * - the string starts with a digit
     * - a character is neither a letter, digit, '_' nor '$'.
     * - the string is a keyword
     *
     * @param text must this text be escaped
     * @return whether the string must be escaped
     */
    public static boolean shouldEscape(String text) {
        int length = text.length();

        if(length == 0 || !Character.isLetter(text.charAt(0)) || KEYWORDS.contains(text))
            return true;

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if(!Character.isLetterOrDigit(c) && c != '_' && c != '$')
                return true;
        }

        return false;
    }

    /**
     * Print a string that gets escaped if necessary.
     *
     * @param builder where to write
     * @param text that will get appended
     */
    public static void printMaybeEscapedString(StringBuilder builder, String text) {
        if (shouldEscape(text)) {
            printEscapedString(builder, text, '`');
        } else {
            builder.append(text);
        }
    }

    public static void printEscapedString(StringBuilder builder, String text, char quote) {
        builder.append(quote);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            printEscapedChar(builder, c, quote);
        }
        builder.append(quote);
    }

    public static void printEscapedChar(StringBuilder builder, char c, char quote) {
        switch (c) {
            case '\t':
                builder.append("\\t");
                return;

            case '\b':
                builder.append("\\b");
                return;

            case '\n':
                builder.append("\\n");
                return;

            case '\r':
                builder.append("\\r");
                return;

            case '\f':
                builder.append("\\f");
                return;

            case '\\':
                builder.append("\\\\");
                return;

            default:
                if (c == quote) {
                    builder.append('\\');
                    builder.append(quote);
                } else {
                    boolean isHumanReadable = 32 <= c && c <= 126;
                    if (isHumanReadable) {
                        builder.append(c);
                    } else {
                        builder.ensureCapacity(6);
                        builder.append("\\u");
                        builder.append(String.format("%04X", (int) c));
                    }
                }
                return;
        }
    }

    public static <T> void joined(Iterable<T> iterable, Consumer<T> appendElement, Runnable appendSeperator) {
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            appendElement.accept(iterator.next());

            if (iterator.hasNext()) {
                appendSeperator.run();
            }
        }
    }

    public static <T> T assertionError() {
        throw new AssertionError();
    }
}
