package net.mirai.dynamicportals.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.mirai.dynamicportals.DynamicPortals;

final class GeneratedTomlFormatter {
    private static final Set<String> TARGET_KEYS = Set.of(
        "killRequirements",
        "itemRequirements",
        "advancementRequirements",
        "consumeBypassItems"
    );

    private static final Pattern INLINE_LIST = Pattern.compile("^(\\s*)([A-Za-z0-9_]+)\\s*=\\s*\\[(.*)]\\s*$");

    private GeneratedTomlFormatter() {
    }

    static void formatIfNeeded(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }

        try {
            String original = Files.readString(path);
            String formatted = format(original);
            if (!formatted.equals(original)) {
                Files.writeString(path, formatted);
                DynamicPortals.LOGGER.info("Formatted generated config file {} for readability.", path);
            }
        } catch (IOException ex) {
            DynamicPortals.LOGGER.warn("Failed to format generated config file {}.", path, ex);
        }
    }

    static String format(String content) {
        String[] lines = content.split("\\R", -1);
        String lineSeparator = content.contains("\r\n") ? "\r\n" : "\n";
        StringBuilder builder = new StringBuilder(content.length() + 256);

        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            builder.append(formatLine(line, lineSeparator));
            if (index < lines.length - 1) {
                builder.append(lineSeparator);
            }
        }

        return builder.toString();
    }

    private static String formatLine(String line, String lineSeparator) {
        Matcher matcher = INLINE_LIST.matcher(line);
        if (!matcher.matches()) {
            return line;
        }

        String indent = matcher.group(1);
        String key = matcher.group(2);
        if (!TARGET_KEYS.contains(key)) {
            return line;
        }

        String rawItems = matcher.group(3).trim();
        if (rawItems.isEmpty()) {
            return indent + key + " = []";
        }

        String[] items = rawItems.split(",");
        StringBuilder builder = new StringBuilder();
        builder.append(indent).append(key).append(" = [");
        builder.append(lineSeparator);

        String itemIndent = indent + "    ";
        for (int index = 0; index < items.length; index++) {
            String item = items[index].trim();
            if (item.isEmpty()) {
                continue;
            }

            builder.append(itemIndent).append(item);
            if (index < items.length - 1) {
                builder.append(',');
            }
            builder.append(lineSeparator);
        }

        builder.append(indent).append(']');
        return builder.toString();
    }
}