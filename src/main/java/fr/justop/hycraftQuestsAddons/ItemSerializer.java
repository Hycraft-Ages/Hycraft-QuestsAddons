package fr.justop.hycraftQuestsAddons;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemSerializer {

    public static byte[] serialize(ItemStack[] items) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos);
        oos.writeInt(items.length);
        for (ItemStack item : items) {
            oos.writeObject(item);
        }
        oos.close();
        return baos.toByteArray();
    }

    public static ItemStack[] deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BukkitObjectInputStream ois = new BukkitObjectInputStream(bais);
        ItemStack[] items = new ItemStack[ois.readInt()];
        for (int i = 0; i < items.length; i++) {
            items[i] = (ItemStack) ois.readObject();
        }
        ois.close();
        return items;
    }
}
