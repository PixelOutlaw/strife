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
package land.face.strife.stats;

import java.util.HashMap;
import java.util.Map;

public enum AbilitySlot {

  SLOT_A(0),
  SLOT_B(1),
  SLOT_C(2),
  SLOT_D(3),
  PASSIVE_A(-1),
  PASSIVE_B(-1),
  INVALID(-1);

  private static final Map<Integer, AbilitySlot> copyOfValues = createSlotIndexMap();
  public static final AbilitySlot[] cachedValues = AbilitySlot.values();

  private static Map<Integer, AbilitySlot> createSlotIndexMap() {
    Map<Integer, AbilitySlot> values = new HashMap<>();
    for (AbilitySlot slot : AbilitySlot.values()) {
      if (slot.getSlotIndex() == -1) {
        continue;
      }
      values.put(slot.getSlotIndex(), slot);
    }
    return values;
  }

  public static AbilitySlot fromSlot(int slotIndex) {
    return copyOfValues.getOrDefault(slotIndex, INVALID);
  }

  private final int slotIndex;

  AbilitySlot(int slotIndex) {
    this.slotIndex = slotIndex;
  }

  public int getSlotIndex() {
    return slotIndex;
  }

}
