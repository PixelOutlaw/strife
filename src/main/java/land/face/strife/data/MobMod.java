package land.face.strife.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class MobMod {

  private String id;
  private String prefix;
  private int weight;
  private EntityAbilitySet abilitySet;
  private final Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();
  private Map<StrifeStat, Float> baseStats = new HashMap<>();
  private Map<StrifeStat, Float> perLevelStats = new HashMap<>();

  private final Set<EntityType> validEntities = new HashSet<>();
  private final Set<EntityType> invalidEntities = new HashSet<>();
  private final Set<String> validRegionIds = new HashSet<>();
  private final Set<Biome> validBiomes = new HashSet<>();
}
