package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Title extends Effect {

	private String topTitle;
	private String lowerTitle;
	private double range;
	
	@Override
	public void apply(StrifeMob caster, StrifeMob target) {
		for (Entity e : caster.getEntity().getNearbyEntities(range, range, range)) {
			if (e instanceof Player) {
				TitleUtils.sendTitle((Player) e, topTitle, lowerTitle);
			}
		}
	}

	public void setTopTitle(String topTitle) {
		this.topTitle = PaletteUtil.color(topTitle);
	}

	public void setLowerTitle(String lowerTitle) {
		this.lowerTitle = PaletteUtil.color(lowerTitle);
	}

	public void setRange(double range) {
		this.range = range;
	}
}
