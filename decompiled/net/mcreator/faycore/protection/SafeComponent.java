/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.mcreator.faycore.protection;

import net.mcreator.faycore.config.CrashProtectionConfig;
import net.minecraft.network.chat.Component;

public final class SafeComponent {
    private SafeComponent() {
    }

    public static String getString(Component component) {
        if (component == null) {
            return "";
        }
        if (!CrashProtectionConfig.enableComponentProtection) {
            try {
                return component.getString();
            }
            catch (Throwable ignored) {
                return "";
            }
        }
        try {
            String text = component.getString();
            if (text == null) {
                return "";
            }
            if (text.length() > CrashProtectionConfig.maxResolvedComponentLength) {
                if (CrashProtectionConfig.debugLog) {
                    System.out.println("[FayCore] Blocked oversized Component: " + text.length());
                }
                return text.substring(0, CrashProtectionConfig.maxResolvedComponentLength);
            }
            return text;
        }
        catch (OutOfMemoryError error) {
            if (CrashProtectionConfig.debugLog) {
                System.out.println("[FayCore] Blocked recursive Component");
            }
            return "";
        }
        catch (Throwable throwable) {
            if (CrashProtectionConfig.debugLog) {
                throwable.printStackTrace();
            }
            return "";
        }
    }

    public static boolean isSafe(Component component) {
        String text = SafeComponent.getString(component);
        return text.length() <= CrashProtectionConfig.maxResolvedComponentLength;
    }

    public static Component limit(Component component) {
        if (component == null) {
            return Component.empty();
        }
        String text = SafeComponent.getString(component);
        if (text.length() <= CrashProtectionConfig.maxResolvedComponentLength) {
            return component;
        }
        return Component.literal((String)text.substring(0, CrashProtectionConfig.maxResolvedComponentLength));
    }
}

