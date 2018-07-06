package info.faceland.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.data.AttributedEntity;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Speak extends Effect {

	private List<String> messages;
	
	@Override
	public void apply(AttributedEntity caster, LivingEntity target) {
		if (target instanceof Player) {
			MessageUtils.sendMessage(target,
					messages.get(ThreadLocalRandom.current().nextInt(messages.size())));
		}
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
}
