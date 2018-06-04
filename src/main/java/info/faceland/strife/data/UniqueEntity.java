package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class UniqueEntity {

    private EntityType type;
    private String name;
    private Map<StrifeAttribute, Double> attributeMap;
    private EntityAbilitySet abilitySet;

    private ItemStack mainHandItem = null;
    private ItemStack offHandItem = null;
    private ItemStack helmetItem = null;
    private ItemStack chestItem = null;
    private ItemStack legsItem = null;
    private ItemStack bootsItem = null;

    private Particle particle;
    private int particleCount;
    private float particleRadius;

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<StrifeAttribute, Double> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<StrifeAttribute, Double> attributeMap) {
        this.attributeMap = attributeMap;
    }

    public EntityAbilitySet getAbilitySet() {
        return abilitySet;
    }

    public void setAbilitySet(EntityAbilitySet abilitySet) {
        this.abilitySet = abilitySet;
    }

    public ItemStack getMainHandItem() {
        return mainHandItem;
    }

    public void setMainHandItem(ItemStack mainHandItem) {
        this.mainHandItem = mainHandItem;
    }

    public ItemStack getOffHandItem() {
        return offHandItem;
    }

    public void setOffHandItem(ItemStack offHandItem) {
        this.offHandItem = offHandItem;
    }

    public ItemStack getHelmetItem() {
        return helmetItem;
    }

    public void setHelmetItem(ItemStack helmetItem) {
        this.helmetItem = helmetItem;
    }

    public ItemStack getChestItem() {
        return chestItem;
    }

    public void setChestItem(ItemStack chestItem) {
        this.chestItem = chestItem;
    }

    public ItemStack getLegsItem() {
        return legsItem;
    }

    public void setLegsItem(ItemStack legsItem) {
        this.legsItem = legsItem;
    }

    public ItemStack getBootsItem() {
        return bootsItem;
    }

    public void setBootsItem(ItemStack bootsItem) {
        this.bootsItem = bootsItem;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void setParticleCount(int particleCount) {
        this.particleCount = particleCount;
    }

    public float getParticleRadius() {
        return particleRadius;
    }

    public void setParticleRadius(float particleRadius) {
        this.particleRadius = particleRadius;
    }
}
