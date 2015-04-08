/*
 * This file is part of Strife, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
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
