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
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.Champion;
import info.faceland.strife.util.ItemTypeUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Random;

public class WandListener implements Listener{

    private final StrifePlugin plugin;
    private final Random random;

    private static final String ATTACK_UNCHARGED = TextUtils.color("&e&lNot charged enough!");

    public WandListener(StrifePlugin plugin) {
        this.plugin = plugin;
        random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwing(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        Player playerEntity = event.getPlayer();

        Champion playerChamp = plugin.getChampionManager().getChampion(playerEntity.getUniqueId());

        AttributedEntity pStats = plugin.getEntityStatCache().getEntity(playerEntity, true);
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

        // double attackspeed penalty for wands
        attackSpeedMult *= attackSpeedMult;
        attackSpeedMult = Math.max(0.15, attackSpeedMult);

        playerEntity.getWorld().playSound(playerEntity.getLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
        playerChamp.getAttributeValues(false);
        playerChamp.getWeaponAttributeValues();
        playerChamp.getCache().recombine();
        ShulkerBullet magicProj = playerEntity.getWorld().spawn(playerEntity.getEyeLocation().clone().add(0, -0.45, 0), ShulkerBullet.class);
        magicProj.setShooter(playerEntity);
        Vector vec = playerEntity.getLocation().getDirection();
        magicProj.setVelocity(new Vector(vec.getX() * 1.2, vec.getY() * 1.2 + 0.255, vec.getZ() * 1.2));
        double damage = playerChamp.getCache().getAttribute(StrifeAttribute.MAGIC_DAMAGE) * attackSpeedMult;
        double critMult = 0;
        double overMult = 0;
        if (random.nextDouble() <= playerChamp.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE)) {
            critMult = playerChamp.getCache().getAttribute(StrifeAttribute.CRITICAL_DAMAGE) - 1;
        }
        if (attackSpeedMult == 1.0D) {
            overMult = playerChamp.getCache().getAttribute(StrifeAttribute.OVERCHARGE);
        }
        magicProj.setMetadata("handled", new FixedMetadataValue(plugin, true));
        magicProj.setMetadata("damage", new FixedMetadataValue(plugin, damage));
        magicProj.setMetadata("overcharge", new FixedMetadataValue(plugin, overMult));
        magicProj.setMetadata("critical", new FixedMetadataValue(plugin, critMult));
        magicProj.setMetadata("armorPen", new FixedMetadataValue(plugin, playerChamp.getCache()
                .getAttribute(StrifeAttribute.ARMOR_PENETRATION)));
        magicProj.setMetadata("accuracy", new FixedMetadataValue(plugin, playerChamp.getCache()
                .getAttribute(StrifeAttribute.ACCURACY)));
        if (playerChamp.getCache().getAttribute(StrifeAttribute.FIRE_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.IGNITE_CHANCE)) {
                magicProj.setMetadata("fireDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.FIRE_DAMAGE) * attackSpeedMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.ICE_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE)) {
                magicProj.setMetadata("iceDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.ICE_DAMAGE) * attackSpeedMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE)) {
                magicProj.setMetadata("lightningDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                    .getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) * attackSpeedMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.DARK_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.CORRUPT_CHANCE)) {
                magicProj.setMetadata("darkDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                    .getAttribute(StrifeAttribute.DARK_DAMAGE) * attackSpeedMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIFE_STEAL) > 0) {
            magicProj.setMetadata("lifeSteal", new FixedMetadataValue(plugin, playerChamp.getCache()
                    .getAttribute(StrifeAttribute.LIFE_STEAL)));
        }
        event.setCancelled(true);
    }
}
