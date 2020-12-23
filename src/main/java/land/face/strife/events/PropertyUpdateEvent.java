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

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PropertyUpdateEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  private final String id;
  private final float baseValue;
  private float appliedValue;

  public PropertyUpdateEvent(String id, float baseValue) {
    this.id = id;
    this.baseValue = baseValue;
    appliedValue = baseValue;
  }

  public String getId() {
    return id;
  }

  public float getBaseValue() {
    return baseValue;
  }

  public float getAppliedValue() {
    return appliedValue;
  }

  public void setAppliedValue(float appliedValue) {
    this.appliedValue = appliedValue;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

}