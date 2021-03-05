package com.blank038.servermarket.listener;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.config.LangConfiguration;
import com.blank038.servermarket.data.MarketData;
import com.blank038.servermarket.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * @author Blank038
 * @date 2021/03/05
 */
public class PlayerListener implements Listener {

    /**
     * 玩家加入服务器事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String name = event.getPlayer().getName();
        ServerMarket.getInstance().datas.put(name, new PlayerData(name));
        if (ServerMarket.getInstance().results.containsKey(name)) {
            double price = ServerMarket.getInstance().results.remove(name), last = ServerMarket.getInstance().getApi().getLastMoney(event.getPlayer(), price);
            DecimalFormat df = new DecimalFormat("#.00");
            ServerMarket.getInstance().getEconomyBridge().give(event.getPlayer(), last);
            event.getPlayer().sendMessage(LangConfiguration.getString("sale-sell", true).replace("%money%", df.format(price))
                    .replace("%last%", df.format(last)));
        }
    }

    /**
     * 玩家离开服务器事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (ServerMarket.getInstance().datas.containsKey(event.getPlayer().getName())) {
            ServerMarket.getInstance().datas.remove(event.getPlayer().getName()).save();
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        for (Map.Entry<String, MarketData> entry : MarketData.MARKET_DATA.entrySet()) {
            if (entry.getValue().performSellCommand(event.getPlayer(), event.getMessage())) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
