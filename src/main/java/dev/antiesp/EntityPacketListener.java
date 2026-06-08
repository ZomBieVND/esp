package dev.antiesp;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityPacketListener extends PacketAdapter {

    private final AntiESP plugin;
    private final ProtocolManager pm;

    // Cache entity ID -> location để lookup nhanh
    private final Map<Integer, Location> entityLocationCache = new HashMap<>();

    public EntityPacketListener(AntiESP plugin, ProtocolManager pm) {
        super(plugin,
            ListenerPriority.NORMAL,
            // Packet spawn entity (mob, player, object)
            PacketType.Play.Server.SPAWN_ENTITY,
            PacketType.Play.Server.NAMED_ENTITY_SPAWN,
            // Packet update vị trí
            PacketType.Play.Server.REL_ENTITY_MOVE,
            PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
            PacketType.Play.Server.ENTITY_TELEPORT,
            // Packet metadata (tên, health...)
            PacketType.Play.Server.ENTITY_METADATA,
            // Packet equipment (item trên tay, armor...)
            PacketType.Play.Server.ENTITY_EQUIPMENT
        );
        this.plugin = plugin;
        this.pm = pm;
    }

    public void register() {
        pm.addPacketListener(this);
    }

    public void unregister() {
        pm.removePacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) return;

        Player receiver = event.getPlayer();
        if (receiver == null || !receiver.isOnline()) return;

        // Bypass cho admin
        if (plugin.allowBypass() && receiver.hasPermission("antiesp.bypass")) return;

        // Player nhận packet đang đứng trên Y threshold → chặn
        int receiverY = receiver.getLocation().getBlockY();
        if (receiverY < plugin.getHideY()) return; // Người nhận đang ở dưới → thấy bình thường

        PacketType type = event.getPacketType();
        PacketContainer packet = event.getPacket();

        // --- SPAWN ENTITY ---
        if (type == PacketType.Play.Server.SPAWN_ENTITY ||
            type == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {

            double entityY = getSpawnY(packet, type);
            if (entityY == Double.MIN_VALUE) return;

            if (entityY < plugin.getHideY()) {
                event.setCancelled(true);
                // Cache entity ID để chặn các packet tiếp theo
                int eid = packet.getIntegers().read(0);
                cacheEntityLocation(eid, receiver.getWorld(), 0, entityY, 0);
            }
            return;
        }

        // --- CÁC PACKET KHÁC: lookup entity từ world ---
        int entityId = packet.getIntegers().read(0);
        Entity entity = getEntityById(receiver.getWorld(), entityId);

        if (entity == null) return;
        if (entity.equals(receiver)) return; // Không chặn packet của chính mình

        double entityY = entity.getLocation().getY();
        if (entityY < plugin.getHideY()) {
            event.setCancelled(true);
        }
    }

    private double getSpawnY(PacketContainer packet, PacketType type) {
        try {
            if (type == PacketType.Play.Server.SPAWN_ENTITY) {
                // SPAWN_ENTITY: x(double), y(double), z(double)
                return packet.getDoubles().read(1); // index 1 = Y
            } else {
                // NAMED_ENTITY_SPAWN (player): x, y, z doubles
                return packet.getDoubles().read(1);
            }
        } catch (Exception e) {
            return Double.MIN_VALUE;
        }
    }

    private Entity getEntityById(World world, int entityId) {
        for (Entity e : world.getEntities()) {
            if (e.getEntityId() == entityId) return e;
        }
        return null;
    }

    private void cacheEntityLocation(int eid, World world, double x, double y, double z) {
        entityLocationCache.put(eid, new Location(world, x, y, z));
    }
}
