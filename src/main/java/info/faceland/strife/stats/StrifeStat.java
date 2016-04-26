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
package info.faceland.strife.stats;

import info.faceland.strife.attributes.StrifeAttribute;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class StrifeStat implements Comparable<StrifeStat> {

    private final String key;
    private String name;
    private String description;
    private Map<StrifeAttribute, Double> attributeMap;
    private int order;
    private DyeColor dyeColor;
    private ChatColor chatColor;
    private int menuX;
    private int menuY;

    public StrifeStat(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<StrifeAttribute, Double> getAttributeMap() {
        return new HashMap<>(attributeMap);
    }

    public void setAttributeMap(Map<StrifeAttribute, Double> attributeMap) {
        this.attributeMap = attributeMap;
    }

    public double getAttribute(StrifeAttribute attribute) {
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
        if (!(o instanceof StrifeStat)) {
            return false;
        }

        StrifeStat that = (StrifeStat) o;

        return !(key != null ? !key.equals(that.key) : that.key != null);
    }

    @Override
    public int compareTo(StrifeStat o) {
        if (o == null) {
            return 1;
        }
        return Integer.compare(getOrder(), o.getOrder());
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    public void setDyeColor(DyeColor dyeColor) {
        this.dyeColor = dyeColor;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public void setChatColor(ChatColor chatColor) {
        this.chatColor = chatColor;
    }

    public int getMenuX() {
        return menuX;
    }

    public void setMenuX(int menuX) {
        this.menuX = menuX;
    }

    public int getMenuY() {
        return menuY;
    }

    public void setMenuY(int menuY) {
        this.menuY = menuY;
    }

}
