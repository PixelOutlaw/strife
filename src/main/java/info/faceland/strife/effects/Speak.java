package info.faceland.strife.effects;

import static org.bukkit.Bukkit.getLogger;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.LogUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Speak extends Effect {

	private String message;
	
	@Override
	public void execute(AttributedEntity caster, LivingEntity target) {
    LogUtil.printDebug("casting " + name + " in range of " + range);
		for (Entity e : target.getNearbyEntities(range, range, range)) {
			if (e instanceof Player) {
				MessageUtils.sendMessage(e, message);
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
