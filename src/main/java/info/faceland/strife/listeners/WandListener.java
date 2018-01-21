/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.ItemTypeUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class WandListener implements Listener{

    private final StrifePlugin plugin;

    private static final String ATTACK_UNCHARGED = TextUtils.color("&e&lNot charged enough!");

    public WandListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwing(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        Player playerEntity = event.getPlayer();

        AttributedEntity pStats = plugin.getEntityStatCache().getAttributedEntity(playerEntity);
        double attackSpeedMult = plugin.getAttackSpeedTask().getAttackMultiplier(pStats);

        ItemStack wand = playerEntity.getEquipment().getItemInMainHand();

        if (!ItemTypeUtil.isWand(wand)) {
            return;
        }

        if (attackSpeedMult <= 0.25) {
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_UNCHARGED, playerEntity);
            playerEntity.getWorld().playSound(playerEntity.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.5f, 2.0f);
            return;
        }

        playerEntity.getWorld().playSound(playerEntity.getLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
        ShulkerBullet magicProj = playerEntity.getWorld().spawn(playerEntity.getEyeLocation().clone().add(0, -0.45, 0), ShulkerBullet.class);
        magicProj.setShooter(playerEntity);
        Vector vec = playerEntity.getLocation().getDirection();
        magicProj.setVelocity(new Vector(vec.getX() * 1.2, vec.getY() * 1.2 + 0.255, vec.getZ() * 1.2));
        event.setCancelled(true);
    }
}
