package land.face.strife.util;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.managers.BossBarManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.EnergyTask;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class UberConfig {

  public static float INITIAL_ENRAGE_DECAY, ENRAGE_DECAY, RAGE_DECAY_EXPONENT, RAGE_INACTIVE_FLAT, RAGE_INACTIVE_PERCENT,
      RAGE_ATTACK_SPEED_BONUS_MELEE, RAGE_ATTACK_SPEED_BONUS_RANGED, ENRAGE_MULTIPLIER_EFFECTS,
      ENRAGE_MULTIPLIER_ATTACK_SPEED, ENRAGE_RAGE_GAIN_MULT;

  public static float ROLL_COST_FLAT, ROLL_COST_PERCENT, AIR_JUMP_COST_FLAT, AIR_JUMP_COST_PERCENT, LONG_JUMP_COST_FLAT,
      LONG_JUMP_COST_PERCENT, ROLL_POWER, ROLL_ASCENT, AIR_DASH_ASCENT, AIR_DASH_POWER, AGILITY_PARTICLE_SPEED,
      LONG_JUMP_PARTICLE_SPEED, WALK_COST, WALK_COST_PERCENT, RUN_COST, RUN_COST_PERCENT;

  public static int SNEAK_DISABLE_MS;
  public static float DETECTION_THRESHOLD, BASE_AWARENESS_UNSEEN, BASE_AWARENESS_SEEN, AWARENESS_PER_LV_UNSEEN,
      AWARENESS_PER_LV_SEEN, SEEN_MAX_ANGLE, MAX_EXP_RANGE_SQUARED, BASE_SNEAK_EXP, SNEAK_EXP_PER_LEVEL,
      BASE_SNEAK_ATTACK_EXP, SNEAK_ATTACK_EXP_PER_LEVEL, BASE_STEALTH_PARTICLES, MOVEMENT_PARTICLE_MULT,
      SPRINT_PARTICLE_MULT, MAX_STEALTH_PARTICLES, SNEAK_SKILL_PARTICLE_REDUCTION;

  public static float BASE_ATTACK_SECONDS = 1.4f;
  public static float BASIC_ATTACK_COST;
  public static int ATTACK_SPEED_WARNING_MAX_LEVEL;

  public static float FLAT_BARRIER_PER_SECOND, PERCENT_BARRIER_PER_SECOND;
  public static int BARRIER_RECHARGE_TICKS, BARRIER_DELAY_TICKS;
  public static long BARRIER_TICK_RATE;

  public static int BLOCK_TICK = 4;
  public static float MAX_BLOCK_CHANCE, BLOCK_TICK_MULT, FLAT_BLOCK_S, PERCENT_BLOCK_S, PERCENT_MAX_BLOCK_MIN,
      MELEE_FATIGUE, PROJECTILE_FATIGUE, PHYSICAL_BLOCK_FATIGUE_MULT, PHYSICAL_BLOCK_CHANCE_MULT, GUARD_BREAK_POWER,
      COUNTER_PITCH;

  public static String COUNTER_MESSAGE;
  public static Sound COUNTER_SOUND;

  public static int ROLL_LEVI_TICKS, FALL_LEEWAY_MS, SNEAK_EFFECTIVENESS;


  public static Map<StrifeStat, Float> DUAL_WIELD_BONUS_STATS;
  public static float DUAL_WIELD_STAT_RATIO;
  public static float BASE_AIR_TICKS;
  public static float ARMOR_DENOMINATOR;

  public static Particle AGILITY_PARTICLE;

  public static float BURN_FLAT_DAMAGE, POISON_FLAT_DAMAGE, BURN_LEVEL_DAMAGE, POISON_LEVEL_DAMAGE, BLEED_FLAT,
      BLEED_PERCENT, PHYSICAL_BLEED_PERCENT;
  public static Map<Integer, Float> FIRE_DAMAGE_MAP = new HashMap<>();
  public static Map<Integer, Float> POISON_DAMAGE_MAP = new HashMap<>();

  public static float FLAT_CORRUPT_PER_TICK, PERCENT_CORRUPT_PER_TICK;

  public static TextComponent DEATH_MSG;
  public static List<TextComponent> SILLY_DEATH_MSGS;

  public static Map<String, Float> ENRAGE_ABILITY_COOLDOWN = new HashMap<>();

  public static void reload(StrifePlugin p) {

    // RAGE
    INITIAL_ENRAGE_DECAY = (float) p.getSettings().getDouble("config.mechanics.rage.initial-enrage-decay", 2);
    ENRAGE_DECAY = (float) p.getSettings().getDouble("config.mechanics.rage.enrage-decay", 0.1);
    RAGE_DECAY_EXPONENT = (float) p.getSettings().getDouble("config.mechanics.rage.enrage-decay-exponent", 1.02);
    RAGE_INACTIVE_FLAT = (float) p.getSettings().getDouble("config.mechanics.rage.inactive-decay-flat", 1);
    ENRAGE_RAGE_GAIN_MULT = (float) p.getSettings().getDouble("config.mechanics.rage.enrage-rage-gain-mult", 0.35);
    RAGE_INACTIVE_PERCENT = (float) p.getSettings().getDouble("config.mechanics.rage.inactive-decay-percent", 0.08);
    RAGE_ATTACK_SPEED_BONUS_MELEE = (float) p.getSettings()
        .getDouble("config.mechanics.rage.attack-speed-bonus-melee", 0.75);
    RAGE_ATTACK_SPEED_BONUS_RANGED = (float) p.getSettings()
        .getDouble("config.mechanics.rage.attack-speed-bonus-ranged", 0.35);
    ENRAGE_MULTIPLIER_EFFECTS = (float) p.getSettings()
        .getDouble("config.mechanics.rage.enrage-multiplier-effects", 1.25);
    ENRAGE_MULTIPLIER_ATTACK_SPEED = (float) p.getSettings()
        .getDouble("config.mechanics.rage.enrage-multiplier-attack-speed", 1.5);
    ENRAGE_ABILITY_COOLDOWN.clear();
    for (String s : p.getSettings().getStringList("config.mechanics.rage.enrage-cooldown-refunds")) {
      try {
        var strs = s.split("\\|");
        ENRAGE_ABILITY_COOLDOWN.put(strs[0], Float.valueOf(strs[1]));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // AGILITY
    ROLL_COST_FLAT = (float) p.getSettings().getDouble("config.mechanics.agility.base-roll-cost", 6f);
    ROLL_COST_PERCENT = (float) p.getSettings().getDouble("config.mechanics.agility.percent-roll-cost", 0.12f);
    AIR_JUMP_COST_FLAT = (float) p.getSettings().getDouble("config.mechanics.agility.air-jump-cost", 6f);
    AIR_JUMP_COST_PERCENT = (float) p.getSettings().getDouble("config.mechanics.agility.air-jump-percent-cost", 0.12f);
    LONG_JUMP_COST_FLAT = (float) p.getSettings().getDouble("config.mechanics.agility.long-jump-cost", 6f);
    LONG_JUMP_COST_PERCENT = (float) p.getSettings()
        .getDouble("config.mechanics.agility.long-jump-percent-cost", 0.12f);
    FALL_LEEWAY_MS = p.getSettings().getInt("config.mechanics.agility.fall-damage-crouch-reduction-ms");
    ROLL_POWER = (float) p.getSettings().getDouble("config.mechanics.agility.roll-power", 1f);
    ROLL_ASCENT = (float) p.getSettings().getDouble("config.mechanics.agility.roll-ascent", 0.12f);
    ROLL_LEVI_TICKS = p.getSettings().getInt("config.mechanics.agility.roll-levitation-ticks", 2);
    AIR_DASH_ASCENT = (float) p.getSettings().getDouble("config.mechanics.agility.air-dash-ascent", 0.275);
    AIR_DASH_POWER = (float) p.getSettings().getDouble("config.mechanics.agility.air-dash-power", 0.825);
    AGILITY_PARTICLE_SPEED = (float) p.getSettings()
        .getDouble("config.mechanics.agility.air-jump-particle-speed", 0.11);
    LONG_JUMP_PARTICLE_SPEED = (float) p.getSettings()
        .getDouble("config.mechanics.agility.long-jump-particle-speed", 0.11);
    AGILITY_PARTICLE = Particle.valueOf(
        p.getSettings().getString("config.mechanics.agility.air-jump-particle", "SPIT"));
    WALK_COST = (float) p.getSettings().getDouble("config.mechanics.energy.walk-cost-flat", 3) * EnergyTask.TICK_MULT;
    WALK_COST_PERCENT = (float) p.getSettings().getDouble("config.mechanics.energy.walk-regen-percent", 0.75);
    RUN_COST = (float) p.getSettings().getDouble("config.mechanics.energy.run-cost-flat", 10) * EnergyTask.TICK_MULT;
    RUN_COST_PERCENT = (float) p.getSettings().getDouble("config.mechanics.energy.run-regen-percent", 0.25);

    // SNEAK
    DETECTION_THRESHOLD = (float) p.getSettings().getDouble("config.mechanics.sneak.detection-threshold");
    BASE_AWARENESS_UNSEEN = (float) p.getSettings().getDouble("config.mechanics.sneak.base-detection-when-unseen");
    BASE_AWARENESS_SEEN = (float) p.getSettings().getDouble("config.mechanics.sneak.base-detection-when-seen");
    AWARENESS_PER_LV_UNSEEN = (float) p.getSettings().getDouble("config.mechanics.sneak.per-lvl-detection-when-unseen");
    AWARENESS_PER_LV_SEEN = (float) p.getSettings().getDouble("config.mechanics.sneak.per-lvl-detection-when-seen");
    SEEN_MAX_ANGLE = (float) p.getSettings().getDouble("config.mechanics.sneak.maximum-head-angle-for-seen");
    MAX_EXP_RANGE_SQUARED = (float) p.getSettings().getDouble("config.mechanics.sneak.maximum-sneak-exp-range-squared");
    SNEAK_EFFECTIVENESS = p.getSettings().getInt("config.mechanics.sneak.sneak-skill-effectiveness");
    SNEAK_DISABLE_MS = p.getSettings().getInt("config.mechanics.sneak.disable-duration");
    BASE_SNEAK_EXP = (float) p.getSettings().getDouble("config.mechanics.sneak.base-sneak-exp");
    SNEAK_EXP_PER_LEVEL = (float) p.getSettings().getDouble("config.mechanics.sneak.sneak-exp-per-level");
    BASE_SNEAK_ATTACK_EXP = (float) p.getSettings().getDouble("config.mechanics.sneak.base-sneak-attack-exp");
    SNEAK_ATTACK_EXP_PER_LEVEL = (float) p.getSettings().getDouble("config.mechanics.sneak.sneak-attack-exp-per-level");
    BASE_STEALTH_PARTICLES = (float) p.getSettings().getDouble("config.mechanics.sneak.base-particles", 2);
    MOVEMENT_PARTICLE_MULT = (float) p.getSettings().getDouble("config.mechanics.sneak.movement-particle-penalty", 2);
    SPRINT_PARTICLE_MULT = (float) p.getSettings().getDouble("config.mechanics.sneak.sprint-particle-penalty", 2);
    MAX_STEALTH_PARTICLES = (float) p.getSettings().getInt("config.mechanics.sneak.max-stealth-particles", 2);
    SNEAK_SKILL_PARTICLE_REDUCTION = (float) p.getSettings()
        .getInt("config.mechanics.sneak.stealth-levels-per-removed-particle", 25);

    // ATTACK SPEED
    BASE_ATTACK_SECONDS = (float) p.getSettings().getDouble("config.mechanics.attack-speed.base-attack-time", 1.4);
    BASIC_ATTACK_COST = (float) p.getSettings().getDouble("config.mechanics.energy.attack-cost", 5);
    ATTACK_SPEED_WARNING_MAX_LEVEL = p.getSettings().getInt("config.mechanics.energy.warn-level", 10) + 1;

    // DUAL
    DUAL_WIELD_BONUS_STATS = StatUtil.getStatMapFromSection(p.getConfig().getConfigurationSection("mechanics.dual-wield.bonus-stats"));
    DUAL_WIELD_BONUS_STATS.replaceAll((a, v) -> DUAL_WIELD_BONUS_STATS.get(a) * 0.5f);
    DUAL_WIELD_STAT_RATIO = (float) p.getSettings().getDouble("config.mechanics.dual-wield.stat-mult", 0.625);

    // AIR
    BASE_AIR_TICKS = p.getSettings().getInt("config.mechanics.base-oxygen-ticks", 300);

    // ARMOR
    ARMOR_DENOMINATOR = (float) p.getSettings().getDouble("config.mechanics.defense.armor-denominator", 110);

    // BARRIER
    BARRIER_RECHARGE_TICKS = p.getSettings().getInt("config.mechanics.barrier.base-delay-ticks", 100);
    BARRIER_TICK_RATE = p.getSettings().getInt("config.mechanics.barrier.task-tick-rate", 3);
    BARRIER_DELAY_TICKS = (int) ((float) BARRIER_RECHARGE_TICKS / BARRIER_TICK_RATE);
    FLAT_BARRIER_PER_SECOND = p.getSettings().getInt("config.mechanics.barrier.flat-per-second", 4);
    PERCENT_BARRIER_PER_SECOND = (float) p.getSettings().getDouble("config.mechanics.barrier.percent-per-second", 0.08);

    // BLOCK
    BLOCK_TICK = p.getSettings().getInt("config.mechanics.block.tick-rate", 4);
    MAX_BLOCK_CHANCE = (float) p.getSettings().getDouble("config.mechanics.block.max-block-chance", 0.55f);
    BLOCK_TICK_MULT = (float) BLOCK_TICK / 20;
    FLAT_BLOCK_S = (float) p.getSettings().getDouble("config.mechanics.block.flat-block-recovery", 10);
    PERCENT_BLOCK_S = (float) p.getSettings().getDouble("config.mechanics.block.percent-block-recovery", 0.01f);
    PERCENT_MAX_BLOCK_MIN = (float) p.getSettings().getDouble("config.mechanics.block.negative-block-percent", 0.25);
    MELEE_FATIGUE = (float) p.getSettings().getDouble("config.mechanics.block.melee-fatigue", 70);
    PROJECTILE_FATIGUE = (float) p.getSettings().getDouble("config.mechanics.block.projectile-fatigue", 45);
    PHYSICAL_BLOCK_FATIGUE_MULT = (float) p.getSettings()
        .getDouble("config.mechanics.block.physical-block-fatigue-mult", 0.75);
    PHYSICAL_BLOCK_CHANCE_MULT = (float) p.getSettings()
        .getDouble("config.mechanics.block.physical-block-chance-mult", 4.0);
    GUARD_BREAK_POWER = (float) p.getSettings().getDouble("config.mechanics.block.guard-break-multiplier", 1.5f);

    // COUNTER
    COUNTER_SOUND = Sound.valueOf(p.getSettings().getString("config.mechanics.counter.sound", "ENCHANT_THORNS_HIT"));
    COUNTER_PITCH = (float) p.getSettings().getDouble("config.mechanics.counter.pitch", 1);
    COUNTER_MESSAGE = PaletteUtil.color(p.getSettings().getString("language.status.counter-message", "形"));

    // DOT
    BURN_FLAT_DAMAGE = (float) p.getSettings().getDouble("config.mechanics.burn-flat-damage", 6);
    POISON_FLAT_DAMAGE = (float) p.getSettings().getDouble("config.mechanics.poison-flat-damage");
    BURN_LEVEL_DAMAGE = (float) p.getSettings().getDouble("config.mechanics.burn-level-damage", 6);
    POISON_LEVEL_DAMAGE = (float) p.getSettings().getDouble("config.mechanics.poison-level-damage");
    BLEED_FLAT = (float) p.getSettings().getDouble("config.mechanics.bleed-flat-damage", 1);
    BLEED_PERCENT = (float) p.getSettings().getDouble("config.mechanics.bleed-percent-damage", 0.04);
    PHYSICAL_BLEED_PERCENT = (float) p.getSettings().getDouble("config.mechanics.bleed-base-physical-percent", 0.04);
    POISON_DAMAGE_MAP = new HashMap<>();
    FIRE_DAMAGE_MAP = new HashMap<>();
    for (int i = 0; i <= 300; i++) {
      POISON_DAMAGE_MAP.put(i, POISON_FLAT_DAMAGE + i * POISON_LEVEL_DAMAGE);
      FIRE_DAMAGE_MAP.put(i, BURN_FLAT_DAMAGE + i * BURN_LEVEL_DAMAGE);
    }

    // CORRUPT
    FLAT_CORRUPT_PER_TICK = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.base-corrupt-loss", 0.15);
    PERCENT_CORRUPT_PER_TICK = 1f - (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.config.mechanics.percent-corrupt-loss", 0.05);

    // DEATH
    DEATH_MSG = BossBarManager.covertStringToRetardComponent(
        PaletteUtil.color(p.getSettings().getString("language.enemy-killed-title")));
    SILLY_DEATH_MSGS = new ArrayList<>();
    for (String s : p.getSettings().getStringList("language.silly-enemy-killed-titles")) {
      SILLY_DEATH_MSGS.add(BossBarManager.covertStringToRetardComponent(PaletteUtil.color(s)));
    }

  }
}
