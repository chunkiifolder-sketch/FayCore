//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package chunk.faye.mod_tog.faycore.client.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import chunk.faye.mod_tog.faycore.init.FaycoreModScreens;
import chunk.faye.mod_tog.faycore.world.inventory.FaycoreSettingsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FaycoreSettingsScreen extends AbstractContainerScreen<FaycoreSettingsMenu> implements FaycoreModScreens.FabricScreenAccessor {
	private final Level world;
	private final int x;
	private final int y;
	private final int z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_set_core_position;
	private Button button_fast_fill;
	public static int fillStage = 0;
	public static BlockPos posA = null;
	public static BlockPos posB = null;
	public static final double STEP_SIZE = (double)0.5F;
	private static final File CONFIG_FILE;
	private static boolean hasOverlayShownThisStage = false;



	public FaycoreSettingsScreen(FaycoreSettingsMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, 176, 166);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
	}

	public void updateMenuState(int e, String n, Object s) {
		this.menuStateUpdateActive = true;
		this.menuStateUpdateActive = false;
	}

	public void extractRenderState(GuiGraphicsExtractor g, int mX, int mY, float pT) {
		super.extractRenderState(g, mX, mY, pT);
	}

	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse("faycore:textures/screens/faycore_settings.png"), this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
	}

	public boolean keyPressed(KeyEvent event) {
		if (event.key() == 256) {
			this.minecraft.player.closeContainer();
			return true;
		} else {
			return super.keyPressed(event);
		}
	}

	protected void extractLabels(GuiGraphicsExtractor g, int mX, int mY) {
		g.text(this.font, Component.translatable("gui.faycore.faycore_settings.label_faycore_setting"), 48, 8, -26164, false);
	}

	public void init() {
		super.init();
		if (posA == null || posB == null) {
			loadCoordinatesFromLocal();
		};
		String displayLabel = fillStage == 2 ? "清除區域並重設" : (fillStage == 1 ? "儲存並設定 B 點" : "儲存並設定 A 點");
		this.button_set_core_position = Button.builder(Component.literal(displayLabel), (btn) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				if (fillStage == 0) {
					int ax = this.parseInputField("input_ax", mc.player.blockPosition().getX());
					int ay = this.parseInputField("input_ay", mc.player.blockPosition().getY());
					int az = this.parseInputField("input_az", mc.player.blockPosition().getZ());
					posA = new BlockPos(ax, ay, az);
					fillStage = 1;
					this.saveCoordinatesToLocal();
					mc.player.sendSystemMessage(Component.literal("§a[FayCore] §lA 點已設定: §e" + ax + ", " + ay + ", " + az));
					this.minecraft.setScreen((Screen)null);
				} else if (fillStage == 1) {
					int bx = this.parseInputField("input_bx", mc.player.blockPosition().getX());
					int by = this.parseInputField("input_by", mc.player.blockPosition().getY());
					int bz = this.parseInputField("input_bz", mc.player.blockPosition().getZ());
					int sizeX = Math.abs(posA.getX() - bx) + 1;
					int sizeY = Math.abs(posA.getY() - by) + 1;
					int sizeZ = Math.abs(posA.getZ() - bz) + 1;
					if (sizeX * sizeY * sizeZ > 32768 || sizeX > 32 || sizeY > 32 || sizeZ > 32) {
						mc.player.sendSystemMessage(Component.literal("§c[FayCore] 範圍過大！上限為 32x32x32。"));
						return;
					}

					posB = new BlockPos(bx, by, bz);
					fillStage = 2;
					this.saveCoordinatesToLocal();
					(new Thread(() -> {
						try {
							if (mc.player != null && mc.player.connection != null) {
								String forceLoadCmd = String.format("forceload add %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
								String fillSolidCore = String.format("fill %d %d %d %d %d %d minecraft:command_block[facing=up]{Custom:" + chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine.CustomName + "}", posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ());
								String fillOuterGlass = String.format("fill %d %d %d %d %d %d minecraft:red_stained_glass outline", posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ());
								int minX = Math.min(posA.getX(), posB.getX());
								int minY = Math.min(posA.getY(), posB.getY());
								int minZ = Math.min(posA.getZ(), posB.getZ());
								String lockSystemBlock = String.format("setblock %d %d %d minecraft:command_block[facing=up]{Command:\"#FAYCORE_SYSTEM_LOCK\",CustomName:" + chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine.CustomName + "} replace", minX, minY, minZ);
								mc.player.connection.send(new ServerboundChatCommandPacket(forceLoadCmd));
								Thread.sleep(20L);
								mc.player.connection.send(new ServerboundChatCommandPacket(fillSolidCore));
								Thread.sleep(50L);
								mc.player.connection.send(new ServerboundChatCommandPacket(fillOuterGlass));
								Thread.sleep(30L);
								mc.player.connection.send(new ServerboundChatCommandPacket(lockSystemBlock));
							}
						} catch (Exception var8) {
						}

					})).start();
					this.minecraft.setScreen((Screen)null);
					startParticleThread(mc);
					int curMode = (Integer)FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
					boolean curEnable = (Boolean)FayCoreMacroEngine.groupEnables.get(FayCoreMacroEngine.selectedGroupIndex);
					if (curMode == 2 && curEnable) {
						FayCoreMacroEngine.isMacroRunning = true;
					}
				} else {
					if (posA != null && posB != null && mc.player.connection != null) {
						mc.player.connection.send(new ServerboundChatCommandPacket(String.format("fill %d %d %d %d %d %d minecraft:air", posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ())));
						mc.player.connection.send(new ServerboundChatCommandPacket(String.format("forceload remove %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ())));
						hasOverlayShownThisStage = false;
					}

					fillStage = 0;
					posA = null;
					posB = null;
					this.minecraft.setScreen((Screen)null);
				}

			}
		}).bounds(this.leftPos + 32, this.topPos + 28, 110, 20).build();
		this.addRenderableWidget(this.button_set_core_position);
		this.button_fast_fill = Button.builder(Component.literal("讀取並一鍵填充"), (btn) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				this.loadCoordinatesFromLocal();
				if (posA != null && posB != null) {
					fillStage = 2;
					(new Thread(() -> {
						try {
							if (mc.player != null && mc.player.connection != null) {
								String forceLoadCmd = String.format("forceload add %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
								String fillSolidCore = String.format("fill %d %d %d %d %d %d minecraft:command_block[facing=up]{CustomName:" + chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine.CustomName + "}", posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ());
								String fillOuterGlass = String.format("fill %d %d %d %d %d %d minecraft:red_stained_glass outline", posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ());
								int minX = Math.min(posA.getX(), posB.getX());
								int minY = Math.min(posA.getY(), posB.getY());
								int minZ = Math.min(posA.getZ(), posB.getZ());
								String lockSystemBlock = String.format("setblock %d %d %d minecraft:command_block[facing=up]{Command:\"#FAYCORE_SYSTEM_LOCK\",CustomName:" + chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine.CustomName +"} replace", minX, minY, minZ);
								mc.player.connection.send(new ServerboundChatCommandPacket(forceLoadCmd));
								Thread.sleep(20L);
								mc.player.connection.send(new ServerboundChatCommandPacket(fillSolidCore));
								Thread.sleep(50L);
								mc.player.connection.send(new ServerboundChatCommandPacket(fillOuterGlass));
								Thread.sleep(30L);
								mc.player.connection.send(new ServerboundChatCommandPacket(lockSystemBlock));
							}
						} catch (Exception var8) {
						}

					})).start();
					this.minecraft.setScreen((Screen)null);
					startParticleThread(mc);
					int curMode = (Integer)FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
					boolean curEnable = (Boolean)FayCoreMacroEngine.groupEnables.get(FayCoreMacroEngine.selectedGroupIndex);
					if (curMode == 2 && curEnable) {
						FayCoreMacroEngine.isMacroRunning = true;
					}

				} else {
					mc.player.sendSystemMessage(Component.literal("§c[FayCore] 錯誤：沒有歷史紀錄！"));
				}
			}
		}).bounds(this.leftPos + 32, this.topPos + 58, 110, 20).build();
		this.button_fast_fill.active = true;
		this.addRenderableWidget(this.button_fast_fill);
		(new Thread(() -> {
			try {
				for(int tick = 0; tick != 20; ++tick) {
					Thread.sleep(50L);
					Minecraft mcInstance = Minecraft.getInstance();
					if (mcInstance != null) {
						mcInstance.execute(() -> {
							try {
								for(GuiEventListener listener : this.children()) {
									if (listener instanceof EditBox box) {
										String val = box.getValue();
										if (val != null && (val.equals("b") || val.equalsIgnoreCase("b") || val.contains("b") || val.contains("B"))) {
											box.setValue("");
										}

										box.setFocused(false);
									}
								}

								this.setFocused((GuiEventListener)null);
							} catch (Exception var5) {
							}

						});
					}
				}
			} catch (Exception var3) {
			}

		})).start();
	}

	private static void startParticleThread(Minecraft mc) {
		(new Thread(() -> {
			try {
				for(SimpleParticleType pType = ParticleTypes.GLOW; fillStage == 2 && mc.player != null; Thread.sleep(150L)) {
					if (mc.level != null && posA != null && posB != null) {
						double minX = (double)Math.min(posA.getX(), posB.getX());
						double maxX = (double)Math.max(posA.getX(), posB.getX()) + (double)1.0F;
						double minY = (double)Math.min(posA.getY(), posB.getY());
						double maxY = (double)Math.max(posA.getY(), posB.getY()) + (double)1.0F;
						double minZ = (double)Math.min(posA.getZ(), posB.getZ());
						double maxZ = (double)Math.max(posA.getZ(), posB.getZ()) + (double)1.0F;
						int sX = (int)(maxX - minX);
						int sY = (int)(maxY - minY);
						int sZ = (int)(maxZ - minZ);
						mc.execute(() -> {
							if (mc.gui != null && fillStage == 2) {
								if (fillStage != 2) {
									hasOverlayShownThisStage = false;
								}
								if (fillStage == 2 && !hasOverlayShownThisStage) {
									hasOverlayShownThisStage = true;

									mc.execute(() -> {
										if (mc.gui != null) {
											mc.gui.setOverlayMessage(Component.literal("§a§l[FayCore 選區] §e§l" + sX + "x" + sY + "x" + sZ + " §f§l(雙層指令核心)"), false);

											java.util.concurrent.ScheduledExecutorService executor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
											executor.schedule(() -> {
												mc.execute(() -> {
													if (mc.gui != null) {
														mc.gui.setOverlayMessage(Component.empty(), false);
													}
												});
												executor.shutdown();
											}, 3000, java.util.concurrent.TimeUnit.MILLISECONDS);
										}
									});
								}
							}
						});

						ClientLevel lvl = mc.level;
						if (lvl != null) {
							for(double x = minX; x <= maxX; x += (double)0.5F) {
								lvl.addParticle(pType, x, minY, minZ, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, x, maxY, minZ, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, x, minY, maxZ, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, x, maxY, maxZ, (double)0.0F, 0.01, (double)0.0F);
							}

							for(double y = minY; y <= maxY; y += (double)0.5F) {
								lvl.addParticle(pType, minX, y, minZ, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, maxX, y, minZ, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, minX, y, maxZ, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, maxX, y, maxZ, (double)0.0F, 0.01, (double)0.0F);
							}

							for(double z = minZ; z <= maxZ; z += (double)0.5F) {
								lvl.addParticle(pType, minX, minY, z, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, maxX, minY, z, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, minX, maxY, z, (double)0.0F, 0.01, (double)0.0F);
								lvl.addParticle(pType, maxX, maxY, z, (double)0.0F, 0.01, (double)0.0F);
							}
						}
					}
				}
			} catch (Exception var20) {
			}

		})).start();
	}

	private int parseInputField(String name, int defaultValue) {
		try {
			for(GuiEventListener listener : this.children()) {
				if (listener instanceof EditBox box) {
					String value = box.getValue().trim();
					if (!value.isEmpty() && value.matches("-?\\d+")) {
						return Integer.parseInt(value);
					}
				}
			}
		} catch (Exception var7) {
		}

		return defaultValue;
	}

	private void saveCoordinatesToLocal() {
		try {
			if (!CONFIG_FILE.getParentFile().exists()) {
				CONFIG_FILE.getParentFile().mkdirs();
			}

			try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
				writer.println(posA != null ? posA.getX() + "," + posA.getY() + "," + posA.getZ() : "null");
				writer.println(posB != null ? posB.getX() + "," + posB.getY() + "," + posB.getZ() : "null");
				writer.flush();
			}
		} catch (Exception var6) {
		}

	}

	private void loadCoordinatesFromLocal() {
		if (CONFIG_FILE.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
				String lineA = reader.readLine();
				String lineB = reader.readLine();
				if (lineA != null && !lineA.equalsIgnoreCase("null")) {
					String[] tokens = lineA.split(",");
					if (tokens.length == 3) {
						posA = new BlockPos(Integer.parseInt((String)Arrays.asList(tokens).get(0)), Integer.parseInt((String)Arrays.asList(tokens).get(1)), Integer.parseInt((String)Arrays.asList(tokens).get(2)));
					}
				}

				if (lineB != null && !lineB.equalsIgnoreCase("null")) {
					String[] tokens = lineB.split(",");
					if (tokens.length == 3) {
						posB = new BlockPos(Integer.parseInt((String)Arrays.asList(tokens).get(0)), Integer.parseInt((String)Arrays.asList(tokens).get(1)), Integer.parseInt((String)Arrays.asList(tokens).get(2)));
					}
				}
			} catch (Exception var7) {
			}

		}
	}

	static {
		CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_config.txt");
	}
}
