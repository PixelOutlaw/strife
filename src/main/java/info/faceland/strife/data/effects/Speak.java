package info.faceland.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.data.StrifeMob;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;

public class Speak extends Effect {

	private List<String> messages;
	
	@Override
	public void apply(StrifeMob caster, StrifeMob target) {
		if (target.getEntity() instanceof Player) {
			MessageUtils.sendMessage(target.getEntity(),
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
