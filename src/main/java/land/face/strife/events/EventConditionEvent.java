/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package land.face.strife.events;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.StrifeMob;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EventConditionEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  @Getter
  private final StrifeMob caster;
  @Getter
  private final StrifeMob target;
  @Getter
  private final Map<String, String> dataKeys;
  @Getter @Setter
  private boolean met;

  public EventConditionEvent(StrifeMob caster, StrifeMob target, Map<String, String> dataKeys,
      boolean defaultSuccess) {
    this.caster = caster;
    this.target = target;
    this.dataKeys = new HashMap<>();
    this.dataKeys.putAll(dataKeys);
    this.met = defaultSuccess;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

}