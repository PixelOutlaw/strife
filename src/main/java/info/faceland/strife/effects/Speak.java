package info.faceland.strife.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Speak extends Effect {

	private String message;
	
	@Override
	public void apply(AttributedEntity caster, LivingEntity target) {
		if (target instanceof Player) {
			MessageUtils.sendMessage(target, message);
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
