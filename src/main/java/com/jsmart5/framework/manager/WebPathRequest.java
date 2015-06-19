/*
 * JSmart5 - Java Web Development Framework
 * Copyright (c) 2014, Jeferson Albino da Silva, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
*/

package com.jsmart5.framework.manager;

public abstract class WebPathRequest {

    static enum Method {
        GET, POST, PUT, OPTIONS, DELETE, HEAD, TRACE
    }

    public void get(final PathRequestHandler requestHandler) throws Exception {
        // DO NOTHING
    }

    public void post(final PathRequestHandler requestHandler) throws Exception {
        // DO NOTHING
    }

    public void put(final PathRequestHandler requestHandler) throws Exception {
        // DO NOTHING
    }

    public void options(final PathRequestHandler requestHandler) throws Exception {
        // DO NOTHING
    }

    public void delete(final PathRequestHandler requestHandler) throws Exception {
        // DO NOTHING
    }

    public void head(final PathRequestHandler requestHandler) throws Exception {
        // DO NOTHING
    }

    public void trace(final PathRequestHandler requestHandler) throws Exception {
        // DO NOTHING
    }
}