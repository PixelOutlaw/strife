/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.tasks.DisplayRunner;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DisplayFrame;
import land.face.strife.data.pojo.DisplayContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public class DisplayManager {

  private final StrifePlugin plugin;
  private final Map<String, DisplayContainer> displaysMap = new HashMap<>();

  public DisplayManager(StrifePlugin plugin) {
    this.plugin = plugin;
    reload();
  }

  public void reload() {
    displaysMap.clear();
    for (String key : plugin.getDisplaysYaml().getKeys(false)) {
      try {
        DisplayContainer container = new DisplayContainer();
        ConfigurationSection section = plugin.getDisplaysYaml().getConfigurationSection(key);
        ConfigurationSection frameSection = section.getConfigurationSection("frames");
        for (String s : frameSection.getKeys(false)) {
          DisplayFrame displayFrame = DisplayFrame.fromString(frameSection.getString(s));
          container.getFrames().add(displayFrame);
        }
        displaysMap.put(key, container);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void create(String id, LivingEntity livingEntity, FaceColor color) {
    DisplayContainer container = displaysMap.get(id);
    if (container == null) {
      Bukkit.getLogger().info("[Strife] Failed to create display holo named " + id);
      return;
    }
    DisplayRunner runner = new DisplayRunner(container, livingEntity, 0, 0);
    runner.setColor(color);
  }

  public void create(String id, Location location, FaceColor color) {
    DisplayContainer container = displaysMap.get(id);
    if (container == null) {
      Bukkit.getLogger().info("[Strife] Failed to create display holo named " + id);
      return;
    }
    DisplayRunner runner = new DisplayRunner(container, location);
    runner.setColor(color);
  }
}