package fr.justop.hycraftQuestsAddons.objects;

import org.bukkit.Location;

public class CuboidRegion
{
    private final Location corner1;
    private final Location corner2;
    private final Location teleportLocation;

    public CuboidRegion(Location corner1, Location corner2, Location teleportLocation) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.teleportLocation = teleportLocation;
    }

    public boolean isInside(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= Math.min(corner1.getX(), corner2.getX()) && x <= Math.max(corner1.getX(), corner2.getX())
                && y >= Math.min(corner1.getY(), corner2.getY()) && y <= Math.max(corner1.getY(), corner2.getY())
                && z >= Math.min(corner1.getZ(), corner2.getZ()) && z <= Math.max(corner1.getZ(), corner2.getZ());
    }

    public Location getTeleportLocation() {
        return teleportLocation;
    }
}
