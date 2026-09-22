/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.cursor.CursorTypes
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.GuiGraphicsExtractor$HoveredTextEffects
 *  net.minecraft.client.gui.components.AbstractSliderButton
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.screens.MenuScreens
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.ARGB
 *  net.minecraft.util.Mth
 */
package net.mcreator.faycore.init;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.text.DecimalFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mcreator.faycore.client.gui.FaycoreSettingsScreen;
import net.mcreator.faycore.init.FaycoreModMenus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class FaycoreModScreens {
    public static void clientLoad() {
        MenuScreens.register(FaycoreModMenus.FAYCORE_SETTINGS, FaycoreSettingsScreen::new);
    }

    public static class ExtendedSlider
    extends AbstractSliderButton {
        protected Component prefix;
        protected Component suffix;
        protected double minValue;
        protected double maxValue;
        protected double stepSize;
        protected boolean drawString;
        private final DecimalFormat format;

        public ExtendedSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
            super(x, y, width, height, (Component)Component.empty(), 0.0);
            this.prefix = prefix;
            this.suffix = suffix;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.stepSize = Math.abs(stepSize);
            this.value = this.snapToNearest((currentValue - minValue) / (maxValue - minValue));
            this.drawString = drawString;
            if (stepSize == 0.0) {
                precision = Math.min(precision, 4);
                StringBuilder builder = new StringBuilder("0");
                if (precision > 0) {
                    builder.append('.');
                }
                while (precision-- > 0) {
                    builder.append('0');
                }
                this.format = new DecimalFormat(builder.toString());
            } else {
                this.format = Mth.equal((double)this.stepSize, (double)Math.floor(this.stepSize)) ? new DecimalFormat("0") : new DecimalFormat(Double.toString(this.stepSize).replaceAll("\\d", "0"));
            }
            this.updateMessage();
        }

        public ExtendedSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString) {
            this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, 1.0, 0, drawString);
        }

        public double getValue() {
            return this.value * (this.maxValue - this.minValue) + this.minValue;
        }

        public long getValueLong() {
            return Math.round(this.getValue());
        }

        public int getValueInt() {
            return (int)this.getValueLong();
        }

        public void setValue(double value) {
            this.setFractionalValue((value - this.minValue) / (this.maxValue - this.minValue));
        }

        public String getValueString() {
            return this.format.format(this.getValue());
        }

        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            this.setValueFromMouse(event.x());
        }

        protected void onDrag(MouseButtonEvent event, double dx, double dy) {
            super.onDrag(event, dx, dy);
            this.setValueFromMouse(event.x());
        }

        public boolean keyPressed(KeyEvent event) {
            boolean flag;
            int keyCode = event.key();
            boolean bl = flag = keyCode == 263;
            if (flag || keyCode == 262) {
                float f;
                if (this.minValue > this.maxValue) {
                    flag = !flag;
                }
                float f2 = f = flag ? -1.0f : 1.0f;
                if (this.stepSize <= 0.0) {
                    this.setFractionalValue(this.value + (double)(f / (float)(this.width - 8)));
                } else {
                    this.setValue(this.getValue() + (double)f * this.stepSize);
                }
            }
            return false;
        }

        private void setValueFromMouse(double mouseX) {
            this.setFractionalValue((mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8));
        }

        private void setFractionalValue(double fractionalValue) {
            double oldValue = this.value;
            this.value = this.snapToNearest(fractionalValue);
            if (!Mth.equal((double)oldValue, (double)this.value)) {
                this.applyValue();
            }
            this.updateMessage();
        }

        private double snapToNearest(double value) {
            if (this.stepSize <= 0.0) {
                return Mth.clamp((double)value, (double)0.0, (double)1.0);
            }
            value = Mth.lerp((double)Mth.clamp((double)value, (double)0.0, (double)1.0), (double)this.minValue, (double)this.maxValue);
            value = this.stepSize * (double)Math.round(value / this.stepSize);
            value = this.minValue > this.maxValue ? Mth.clamp((double)value, (double)this.maxValue, (double)this.minValue) : Mth.clamp((double)value, (double)this.minValue, (double)this.maxValue);
            return Mth.map((double)value, (double)this.minValue, (double)this.maxValue, (double)0.0, (double)1.0);
        }

        protected void updateMessage() {
            if (this.drawString) {
                this.setMessage((Component)Component.literal((String)"").append(this.prefix).append(this.getValueString()).append(this.suffix));
            } else {
                this.setMessage((Component)Component.empty());
            }
        }

        protected void applyValue() {
        }

        public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white((float)this.alpha));
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getHandleSprite(), this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, this.getHeight(), ARGB.white((float)this.alpha));
            int i = this.active ? 0xFFFFFF : 0xA0A0A0;
            MutableComponent message = this.getMessage().copy().withStyle(style -> style.withColor(i));
            this.extractScrollingStringOverContents(guiGraphics.textRendererForWidget((AbstractWidget)this, GuiGraphicsExtractor.HoveredTextEffects.NONE), (Component)message, 2);
            if (this.isHovered()) {
                guiGraphics.requestCursor(this.dragging ? CursorTypes.RESIZE_EW : CursorTypes.POINTING_HAND);
            }
        }
    }

    public static interface FabricScreenAccessor {
        public void updateMenuState(int var1, String var2, Object var3);
    }
}

