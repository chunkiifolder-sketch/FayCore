/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.mcreator.faycore.protection;

import net.mcreator.faycore.protection.SafeComponent;
import net.minecraft.network.chat.Component;

public final class TextLimiter {
    private TextLimiter() {
    }

    public static boolean check(Component component, int maxLength) {
        try {
            String text = SafeComponent.getString(component);
            return text.length() <= maxLength;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static Component sanitize(Component component, int maxLength) {
        try {
            String text = SafeComponent.getString(component);
            if (text.length() <= maxLength) {
                return component;
            }
            return Component.literal((String)text.substring(0, maxLength));
        }
        catch (Throwable throwable) {
            return Component.empty();
        }
    }
}

