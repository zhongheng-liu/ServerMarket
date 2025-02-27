package com.blank038.servermarket.command;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.filter.FilterBuilder;
import com.blank038.servermarket.filter.impl.KeyFilterImpl;
import com.blank038.servermarket.filter.impl.TypeFilterImpl;
import com.blank038.servermarket.i18n.I18n;
import com.blank038.servermarket.data.cache.market.MarketConfigData;
import com.blank038.servermarket.gui.StoreContainerGui;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author Blank038
 */
public class MainCommand implements CommandExecutor {
    private final ServerMarket instance;

    public MainCommand(ServerMarket serverMarket) {
        instance = serverMarket;
    }

    /**
     * 命令执行器
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (instance.getConfig().getBoolean("command-help")) {
                this.sendHelp(sender, label);
            } else {
                // 打开全球市场
                if (sender instanceof Player) {
                    this.openServerMarket(sender, null);
                }
            }
        } else {
            switch (args[0]) {
                case "open":
                    this.openServerMarket(sender, args.length == 1 ? null : args[1]);
                    break;
                case "search":
                    this.searchItemsAndOpenMarket(sender, args);
                    break;
                case "show":
                    show(sender);
                    break;
                case "box":
                    if (sender instanceof Player) {
                        new StoreContainerGui((Player) sender, 1, null, null).open(1);
                    }
                    break;
                case "reload":
                    if (sender.hasPermission("servermarket.admin")) {
                        this.instance.loadConfig(false);
                        sender.sendMessage(I18n.getString("reload", true));
                    }
                    break;
                default:
                    this.sendHelp(sender, label);
                    break;
            }
        }
        return true;
    }

    /**
     * 打开全球市场
     */
    private void openServerMarket(CommandSender sender, String key) {
        if (!(sender instanceof Player)) {
            return;
        }
        ServerMarket.getApi().openMarket((Player) sender, key, 1, null);
    }

    /**
     * 搜索全球市场并打开市场
     */
    private void searchItemsAndOpenMarket(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(I18n.getString("wrong-market", true));
            return;
        }
        if (args.length == 2) {
            sender.sendMessage(I18n.getString("wrong-key", true));
            return;
        }
        ServerMarket.getApi().openMarket((Player) sender, args[1], 1, new FilterBuilder()
                .addKeyFilter(new KeyFilterImpl(args[2]))
                .setTypeFilter(new TypeFilterImpl(Lists.newArrayList("none"))));
    }

    /**
     * 发送市场状态
     */
    private void show(CommandSender sender) {
        for (String line : I18n.getStringList("show")) {
            String last = line;
            for (Map.Entry<String, MarketConfigData> entry : MarketConfigData.MARKET_DATA.entrySet()) {
                String value = "%" + entry.getValue().getMarketKey() + "%";
                if (last.contains(value)) {
                    // 开始设置变量
                    String permission = entry.getValue().getPermission();
                    if (permission != null && !"".equals(permission) && !sender.hasPermission(permission)) {
                        last = last.replace(value, instance.getConfig().getString("status-text.no-permission"));
                        continue;
                    }
                    last = last.replace(value, instance.getConfig().getString("status-text." + entry.getValue().getMarketStatus().name().toLowerCase()));
                }
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', last));
        }
    }

    /**
     * 发送命令帮助
     */
    private void sendHelp(CommandSender sender, String label) {
        for (String text : I18n.getStringList("help." +
                (sender.hasPermission("servermarket.admin") ? "admin" : "default"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', text).replace("%c", label));
        }
    }
}