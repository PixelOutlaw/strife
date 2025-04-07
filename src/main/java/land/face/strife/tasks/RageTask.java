/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.tasks;

import static land.face.strife.util.UberConfig.ENRAGE_ABILITY_COOLDOWN;
import static land.face.strife.util.UberConfig.ENRAGE_DECAY;
import static land.face.strife.util.UberConfig.ENRAGE_RAGE_GAIN_MULT;
import static land.face.strife.util.UberConfig.RAGE_DECAY_EXPONENT;
import static land.face.strife.util.UberConfig.RAGE_INACTIVE_FLAT;
import static land.face.strife.util.UberConfig.RAGE_INACTIVE_PERCENT;
import static land.face.strife.util.UberConfig.INITIAL_ENRAGE_DECAY;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import java.lang.ref.WeakReference;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.EnrageEvent;
import land.face.strife.managers.GuiManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.LogUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@SuppressWarnings("deprecation")
public class RageTask extends BukkitRunnable {

  private final WeakReference<StrifeMob> parentMob;

  private float rage;
  private float decay = 0;
  private float decayIncrease = 0;

  private static final List<GUIComponent> RAGE_METER = List.of(
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屰")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屲")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屴")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屶")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屸")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屺")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屼")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屾")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岀")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岂")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岄")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岆")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岈")), 49, 0, Alignment.CENTER)
  );
  private static final List<GUIComponent> ENRAGE_METER = List.of(
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("山")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屳")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屵")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屷")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屹")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屻")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屽")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("屿")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岁")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岃")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岅")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岇")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar2", GuiManager.noShadow(Component.text("岉")), 49, 0, Alignment.CENTER));

  public static final GUIComponent EMPTY_ATTACK2 = new GUIComponent("attack-bar2",
      GuiManager.EMPTY, 0, 0, Alignment.CENTER);

  private static final int rageLen = RAGE_METER.size() - 1;
  private static final int enrageLen = ENRAGE_METER.size() - 1;

  private static org.bukkit.Color RAGE_PARTICLE_1 = org.bukkit.Color.fromRGB(138, 21, 87);
  private static org.bukkit.Color RAGE_PARTICLE_2 = org.bukkit.Color.fromRGB(186, 30, 30);
  private static org.bukkit.Color RAGE_PARTICLE_3 = org.bukkit.Color.fromRGB(242, 120, 10);

  public RageTask(StrifeMob mob) {
    this.parentMob = new WeakReference<>(mob);
    runTaskTimer(StrifePlugin.getInstance(), 1L, 5L);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || !mob.getEntity().isValid()) {
      cancel();
      return;
    }
    if (isEnraged()) {
      spawnEnrageParticles(mob.getEntity(), rage);
      decayFromEnrage();
      return;
    }
    if (!mob.isInCombat()) {
      spawnRageParticles(mob.getEntity(), rage);
      decayFromOutOfCombat();
      return;
    }
    spawnRageParticles(mob.getEntity(), rage);
  }

  private void decayFromOutOfCombat() {
    float lostTicks = RAGE_INACTIVE_FLAT + rage * RAGE_INACTIVE_PERCENT;
    reduceRage(lostTicks);
  }

  private void decayFromEnrage() {
    decay = (float) Math.pow(decay, RAGE_DECAY_EXPONENT);
    decay += decayIncrease;
    reduceRage(decay);
  }

  public boolean isEnraged() {
    return decay > 0;
  }

  public void bumpRage(float amount) {
    StrifeMob mob = parentMob.get();
    float maxRage = mob.getMaxRage();
    if (isEnraged()) {
      amount *= ENRAGE_RAGE_GAIN_MULT;
    }
    rage += amount;
    if (rage >= maxRage) {
      rage = maxRage;
      triggerEnrage(mob);
    }
    sendRageGui();
  }

  public void reduceRage(float amount) {
    rage -= amount;
    StrifeMob mob = parentMob.get();
    if (mob == null) {
      cancel();
      return;
    }
    if (rage > 0) {
      sendRageGui();
      return;
    }
    rage = 0;
    if (!isEnraged()) {
      LogUtil.printDebug("Rage complete, removing");
      mob.endRageTask();
    } else {
      LogUtil.printDebug("Rage complete, removing");
      float cdTime = 10f * 1000f;
      cdTime /= 1 - mob.getStat(StrifeStat.COOLDOWN_REDUCTION) / 100;
      mob.setRageRestart(System.currentTimeMillis() + (int) cdTime);
      mob.endRageTask();
      mob.setCacheNeeded(true);
      mob.cacheStats();
      StrifePlugin.getInstance().getStatUpdateManager().updateAllAttributes(mob);
    }
  }

  private void triggerEnrage(@NotNull StrifeMob mob) {
    if (mob.isEnraged() || mob.hasTrait(StrifeTrait.LEVEL_HEADED)) {
      return;
    }
    setDecay(mob);
    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(), Sound.ENTITY_WARDEN_LISTENING_ANGRY, 2, 1);
    mob.setCacheNeeded(true);
    mob.cacheStats();
    StrifePlugin.getInstance().getStatUpdateManager().updateAllAttributes(mob);
    for (var entry : ENRAGE_ABILITY_COOLDOWN.entrySet()) {
      StrifePlugin.getInstance().getAbilityManager()
          .reduceCooldownPercent(mob.getEntity(), entry.getKey(), entry.getValue());
    }
    EnrageEvent event = new EnrageEvent(mob);
    Bukkit.getPluginManager().callEvent(event);
  }

  public void refreshEnrage(@NotNull StrifeMob mob) {
    if (!mob.isEnraged()) {
      return;
    }
    setDecay(mob);
  }

  private void setDecay(@NotNull StrifeMob mob) {
    float bonusDuration = mob.getStat(StrifeStat.ENRAGE_DURATION) + mob.getStat(StrifeStat.EFFECT_DURATION) / 1.5f ;
    float timeMultiplier = (float) Math.pow(1 + bonusDuration / 100f, 1.75);
    decay = 1f + INITIAL_ENRAGE_DECAY / timeMultiplier;
    decayIncrease = ENRAGE_DECAY / timeMultiplier;
  }

  private void sendRageGui() {
    StrifeMob mob = parentMob.get();
    if (mob.getEntity().getType() != EntityType.PLAYER) {
      return;
    }
    if (rage <= 0) {
      StrifePlugin.getInstance().getGuiManager().getGui((Player) mob.getEntity()).update(EMPTY_ATTACK2);
      return;
    }
    float ratio = rage / mob.getMaxRage();
    if (isEnraged()) {
      int rageStage = Math.round((rageLen * ratio));
      StrifePlugin.getInstance().getGuiManager().getGui((Player) mob.getEntity()).update(RAGE_METER.get(rageStage));
    } else {
      int rageStage = Math.round(enrageLen * ratio);
      StrifePlugin.getInstance().getGuiManager().getGui((Player) mob.getEntity()).update(ENRAGE_METER.get(rageStage));
    }
  }

  private static void spawnRageParticles(LivingEntity entity, float rageStacks) {
    entity.getWorld().spawnParticle(
        Particle.ANGRY_VILLAGER,
        entity.getEyeLocation(),
        1 + (int) (Math.max(0, rageStacks) / 30),
        0.4, 0.4, 0.4
    );
  }

  private static void spawnEnrageParticles(LivingEntity entity, float rageStacks) {
    var loc = entity.getLocation().clone();
    var amount = (int) (Math.max(0, rageStacks) / 30);
    loc.add(0, 0.15, 0);
    entity.getWorld().spawnParticle(Particle.ENTITY_EFFECT, loc, 7 + amount,
        0.16, 0.16, 0.16, 0.05, RAGE_PARTICLE_1);
    loc.add(0, 0.35, 0);
    entity.getWorld().spawnParticle(Particle.ENTITY_EFFECT, loc, 4 + amount,
        0.12, 0.12, 0.12, 0.025, RAGE_PARTICLE_2);
    loc.add(0, 0.35, 0);
    entity.getWorld().spawnParticle(Particle.ENTITY_EFFECT, loc, 1 + amount,
        0.08, 0.08, 0.08, 0.01, RAGE_PARTICLE_3);
  }
}
