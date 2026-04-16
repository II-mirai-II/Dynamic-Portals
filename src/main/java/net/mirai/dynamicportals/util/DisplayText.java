package net.mirai.dynamicportals.util;

import net.minecraft.resources.ResourceLocation;

public final class DisplayText {
    private DisplayText() {
    }

    public static String dimension(String id) {
        if (id == null || id.isBlank()) {
            return id;
        }

        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return humanize(id);
        }

        String path = location.getPath();
        if ("the_nether".equals(path)) {
            return "Nether";
        }
        if ("the_end".equals(path)) {
            return "End";
        }
        if ("overworld".equals(path)) {
            return "Overworld";
        }

        return humanize(path);
    }

    public static String resource(String id) {
        if (id == null || id.isBlank()) {
            return id;
        }

        ResourceLocation location = ResourceLocation.tryParse(id);
        return location == null ? humanize(id) : humanize(location.getPath());
    }

    private static String humanize(String raw) {
        String[] words = raw.split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }
        return builder.isEmpty() ? raw : builder.toString();
    }
}