package com.connorlinfoot.bountifulapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.connorlinfoot.bountifulapi.Utils;

public class BountifulAPI extends JavaPlugin implements Listener {
    public static BountifulAPI bountifulAPI;
    private String updateMessage;

    public void onEnable() {
        // Instances
        bountifulAPI = this;
        // Settings
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Updater
        if(getConfig().getBoolean("Updater")) {
            CLUpdate clUpdate = new CLUpdate(this);
            CLUpdate.UpdateResult updateResult = clUpdate.getResult();
            switch (updateResult) {
                default:
                case NO_UPDATE:
                    updateMessage = "No update was found, you are running the latest version.";
                    break;
                case UPDATE_AVAILABLE:
                    updateMessage = "An update for " + getDescription().getName() + " is available, new version is " + clUpdate.getVersion() + ". Your installed version is " + getDescription().getVersion() + ".\nPlease update to the latest version :)";
                    break;
            }
        }
        if (updateMessage != null) {
            getLogger().info(updateMessage);
        }

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (getConfig().getBoolean("Title On Join")) {
            sendTitle(event.getPlayer(), 20, 50, 20, getConfig().getString("Title Message"), getConfig().getString("Subtitle Message"));
        }
        if (getConfig().getBoolean("Tab Header Enabled")) {
            sendTabTitle(event.getPlayer(), getConfig().getString("Tab Header Message"), getConfig().getString("Tab Footer Message"));
        }
    }

    @Deprecated
    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
        sendTitle(player, fadeIn, stay, fadeOut, message, null);
    }

    @Deprecated
    public static void sendSubtitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
        sendTitle(player, fadeIn, stay, fadeOut, null, message);
    }

    @Deprecated
    public static void sendFullTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
    }

    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        TitleSendEvent titleSendEvent = new TitleSendEvent(player, title, subtitle);
        Bukkit.getPluginManager().callEvent(titleSendEvent);
        if (titleSendEvent.isCancelled()) {
            return;
        }

        try {
            Object e;
            Object chatTitle;
            Object chatSubtitle;
            @SuppressWarnings("rawtypes")
            Constructor subtitleConstructor;
            Object titlePacket;
            Object subtitlePacket;

            if (title != null) {
                title = ChatColor.translateAlternateColorCodes('&', title);
                title = title.replaceAll("%player%", player.getDisplayName());
                // Times packets
                e = Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object) null);
                chatTitle = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = Utils.getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], Utils.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
                titlePacket = subtitleConstructor.newInstance(new Object[]{e, chatTitle, fadeIn, stay, fadeOut});
                Utils.sendPacket(player, titlePacket);

                e = Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get((Object) null);
                chatTitle = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = Utils.getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], Utils.getNMSClass("IChatBaseComponent")});
                titlePacket = subtitleConstructor.newInstance(new Object[]{e, chatTitle});
                Utils.sendPacket(player, titlePacket);
            }

            if (subtitle != null) {
                subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
                subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
                // Times packets
                e = Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object) null);
                chatSubtitle = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = Utils.getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], Utils.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
                subtitlePacket = subtitleConstructor.newInstance(new Object[]{e, chatSubtitle, fadeIn, stay, fadeOut});
                Utils.sendPacket(player, subtitlePacket);

                e = Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get((Object) null);
                chatSubtitle = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke((Object) null, new Object[]{"{\"text\":\"" + subtitle + "\"}"});
                subtitleConstructor = Utils.getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{Utils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], Utils.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
                subtitlePacket = subtitleConstructor.newInstance(new Object[]{e, chatSubtitle, fadeIn, stay, fadeOut});
                Utils.sendPacket(player, subtitlePacket);
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    public static void clearTitle(Player player) {
        sendTitle(player, 0, 0, 0, "", "");
    }

    public static void sendTabTitle(Player player, String header, String footer) {
        if (header == null) {
            header = "";
        } else {
            header = ChatColor.translateAlternateColorCodes('&', header);
        }

        if (footer == null) {
            footer = "";
        } else {
            footer = ChatColor.translateAlternateColorCodes('&', footer);
        }

        TabTitleSendEvent tabTitleSendEvent = new TabTitleSendEvent(player, header, footer);
        Bukkit.getPluginManager().callEvent(tabTitleSendEvent);
        if (tabTitleSendEvent.isCancelled()) {
            return;
        }

        header = header.replaceAll("%player%", player.getDisplayName());
        footer = footer.replaceAll("%player%", player.getDisplayName());

        try {
            Object tabHeader = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + header + "\"}");
            Object tabFooter = Utils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + footer + "\"}");
            Constructor<?> titleConstructor = Utils.getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(Utils.getNMSClass("IChatBaseComponent"));
            Object packet = titleConstructor.newInstance(tabHeader);
            Field field = packet.getClass().getDeclaredField("b");
            field.setAccessible(true);
            field.set(packet, tabFooter);
            Utils.sendPacket(player, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendActionBar(Player player, String message) {
        ActionBarMessageEvent actionBarMessageEvent = new ActionBarMessageEvent(player, message);
        Bukkit.getPluginManager().callEvent(actionBarMessageEvent);
        if (actionBarMessageEvent.isCancelled()) {
            return;
        }

        String nmsver = Bukkit.getServer().getClass().getPackage().getName();
        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
        try {
            Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
            Object p = c1.cast(player);
            Object ppoc;
            Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
            Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
            if ((nmsver.equalsIgnoreCase("v1_8_R1") || !nmsver.startsWith("v1_8_")) && !nmsver.startsWith("v1_9_")) {
                Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
                Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
                Method m3 = c2.getDeclaredMethod("a", new Class<?>[]{String.class});
                Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
                ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(new Object[]{cbc, (byte) 2});
            } else {
                Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
                Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
                Object o = c2.getConstructor(new Class<?>[]{String.class}).newInstance(new Object[]{message});
                ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(new Object[]{o, (byte) 2});
            }
            Method m1 = c1.getDeclaredMethod("getHandle", new Class<?>[]{});
            Object h = m1.invoke(p);
            Field f1 = h.getClass().getDeclaredField("playerConnection");
            Object pc = f1.get(h);
            Method m5 = pc.getClass().getDeclaredMethod("sendPacket", new Class<?>[]{c5});
            m5.invoke(pc, ppoc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendActionBar(final Player player, final String message, int duration) {
        sendActionBar(player, message);

        if (duration >= 0) {
            // Sends empty message at the end of the duration. Allows messages shorter than 3 seconds, ensures precision.
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendActionBar(player, "");
                }
            }.runTaskLater(bountifulAPI, duration + 1);
        }

        // Re-sends the messages every 3 seconds so it doesn't go away from the player's screen.
        while (duration > 60) {
            duration -= 60;
            int sched = duration % 60;
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendActionBar(player, message);
                }
            }.runTaskLater(bountifulAPI, (long) sched);
        }
    }

    public static void sendActionBarToAllPlayers(String message) {
        sendActionBarToAllPlayers(message, -1);
    }

    public static void sendActionBarToAllPlayers(String message, int duration) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendActionBar(p, message, duration);
        }
    }
}
