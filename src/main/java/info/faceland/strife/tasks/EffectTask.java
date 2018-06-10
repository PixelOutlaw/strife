package info.faceland.strife.tasks;

import static org.bukkit.Bukkit.getLogger;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.effects.Effect;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectTask extends BukkitRunnable {

	private final AttributedEntity caster;
	private final LivingEntity target;
	private List<Effect> effects;

	public EffectTask(AttributedEntity caster, LivingEntity target, List<Effect> effects) {
		this.caster = caster;
		this.target = target;
		this.effects = effects;
	}

	@Override
	public void run() {
		getLogger().info("effect task running");
		if (!caster.getEntity().isValid()) {
			getLogger().info("nvm caster ded");
			this.cancel();
		}
		for (Effect effect : effects) {
			getLogger().info("executing effect " + effect);
			effect.execute(caster, target);
		}
		getLogger().info("done");
	}
}
