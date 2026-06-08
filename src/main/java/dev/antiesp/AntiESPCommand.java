package dev.antiesp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AntiESPCommand implements CommandExecutor {

    private final AntiESP plugin;

    public AntiESPCommand(AntiESP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("antiesp.admin")) {
            sender.sendMessage("§cBạn không có quyền dùng lệnh này.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§eAntiESP §7- §fHide Y: §a" + plugin.getHideY());
            sender.sendMessage("§7Dùng: /antiesp reload | status");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage("§aAntiESP config reloaded. Hide Y=" + plugin.getHideY());
                break;
            case "status":
                sender.sendMessage("§eAntiESP §ađang chạy §7| Hide below Y=" + plugin.getHideY()
                        + " | Bypass: " + plugin.allowBypass());
                break;
            default:
                sender.sendMessage("§7Dùng: /antiesp reload | status");
        }
        return true;
    }
}
