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
package land.face.strife.data.champion;

import java.util.List;
import java.util.Map;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;

@Getter
@Setter
public class StrifeAttribute {

  private final String key;
  private String name;
  private String itemModel;
  private List<String> description;
  private Sound clickSound;
  private float clickPitch;
  private Sound levelSound;
  private float levelPitch;
  private int slot;
  private int startCap;
  private int maxCap;
  private int levelsToRaiseCap;
  private Map<String, Integer> baseStatRequirements;
  private Map<String, Integer> statIncreaseIncrements;
  private Map<StrifeStat, Float> attributeMap;

  public StrifeAttribute(String key) {
    this.key = key;
  }

  public double getAttribute(StrifeStat attribute) {
    if (attributeMap.containsKey(attribute)) {
      return attributeMap.get(attribute);
    }
    return 0;
  }

  @Override
  public int hashCode() {
    return key != null ? key.hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StrifeAttribute)) {
      return false;
    }

    StrifeAttribute that = (StrifeAttribute) o;

    return !(key != null ? !key.equals(that.key) : that.key != null);
  }
}
