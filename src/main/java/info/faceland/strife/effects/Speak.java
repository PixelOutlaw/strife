package info.faceland.strife.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Speak extends Effect {

	private String message;
	
	@Override
	public void execute(AttributedEntity caster, LivingEntity target) {
		for (LivingEntity le : getTargets(caster.getEntity(), target, range)) {
			if (le instanceof Player) {
				le.sendMessage(message);
			}
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
