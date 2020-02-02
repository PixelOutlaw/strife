package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class ConsoleCommand extends Effect {

  private String command;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    Bukkit.dispatchCommand(console, command.replace("%target%", target.getEntity().getName()));
  }

  public void setCommand(String command) {
    this.command = command;
  }
}