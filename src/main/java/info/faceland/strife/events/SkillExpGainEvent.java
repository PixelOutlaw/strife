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
package info.faceland.strife.events;

import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.LifeSkillType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkillExpGainEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  private final Player player;
  private final float amount;
  private final LifeSkillType skillType;

  public SkillExpGainEvent(Player player, LifeSkillType skillType, float amount) {
    this.player = player;
    this.amount = amount;
    this.skillType = skillType;
  }

  public SkillExpGainEvent(Champion champion, LifeSkillType skillType, float amount) {
    this.player = champion.getPlayer();
    this.amount = amount;
    this.skillType = skillType;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public Player getPlayer() {
    return player;
  }

  public float getAmount() {
    return amount;
  }

  public LifeSkillType getSkillType() {
    return skillType;
  }

}