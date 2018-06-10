package info.faceland.strife.tasks;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.util.LogUtil;
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
    LogUtil.printDebug("Effect task started...");
		if (!caster.getEntity().isValid()) {
      LogUtil.printDebug("Task cancelled, caster is dead");
			this.cancel();
			return;
		}
		for (Effect effect : effects) {
      LogUtil.printDebug("Executing effect " + effect.getName());
			effect.execute(caster, target);
		}
    LogUtil.printDebug("Completed effect task.");
	}
}
