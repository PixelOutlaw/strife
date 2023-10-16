package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Player;

public class Speak extends Effect {

	private List<String> messages;
	
	@Override
	public void apply(StrifeMob caster, StrifeMob target) {
		if (target.getEntity() instanceof Player) {
			MessageUtils.sendMessage(target.getEntity(), messages.get(StrifePlugin.RNG.nextInt(messages.size())));
		}
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
}
