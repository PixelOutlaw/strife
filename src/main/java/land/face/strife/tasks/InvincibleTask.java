package land.face.strife.tasks;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.GuiManager;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class InvincibleTask extends BukkitRunnable {

  private final static GUIComponent INVINCIBLE =
      new GUIComponent("invincible",  GuiManager.noShadow(new TextComponent("㒥")), 181, -1, Alignment.CENTER);
  private final static GUIComponent EMPTY =
      new GUIComponent("invincible", new TextComponent(""), 0, 0, Alignment.CENTER);

  private final WeakReference<StrifeMob> parentMob;
  private int ticks;
  private boolean isPlayer = false;

  public InvincibleTask(StrifeMob parentMob, int ticks) {
    this.parentMob = new WeakReference<>(parentMob);
    this.ticks = ticks;
    if (parentMob.getEntity() instanceof Player) {
      StrifePlugin.getInstance().getGuiManager().updateComponent(
          (Player) parentMob.getEntity(), INVINCIBLE);
      isPlayer = true;
    }
    this.runTaskTimer(StrifePlugin.getInstance(), 1L, 1);
  }

  public void bump(int ticks) {
    if (ticks > this.ticks) {
      this.ticks = ticks;
    }
  }

  @Override
  public void run() {
    ticks--;
    if (ticks == 0) {
      parentMob.get().cancelInvincibility();
    }
  }

  public void sendGuiUpdate() {
    if (isPlayer) {
      StrifePlugin.getInstance().getGuiManager()
          .updateComponent((Player) parentMob.get().getEntity(), EMPTY);
    }
  }
}
