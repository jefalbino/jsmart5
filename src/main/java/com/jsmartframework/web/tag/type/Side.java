/*
 * JSmart Framework - Java Web Development Framework
 * Copyright (c) 2014, Jeferson Albino da Silva, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
*/

package com.jsmartframework.web.tag.type;

public enum Side {

    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    public static boolean validate(String side) {
        try {
            Side.valueOf(side.toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String[] getValues() {
        int index = 0;
        Side[] sides = values();
        String[] values = new String[sides.length];

        for (Side side : sides) {
            values[index++] = side.name().toLowerCase();
        }
        return values;
    }

    public boolean equalsIgnoreCase(String string) {
        return this.name().equalsIgnoreCase(string);
    }

}
