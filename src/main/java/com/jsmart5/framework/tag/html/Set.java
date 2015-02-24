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

package com.jsmart5.framework.tag.html;

import java.util.ArrayList;
import java.util.List;

public class Set extends Tag {

	private List<Tag> tags;

	public Set() {
		super("");
		this.tags = new ArrayList<Tag>();
	}

	public Set addTag(Tag tag) {
		this.tags.add(tag);
		return this;
	}
	
	public StringBuilder getHtml() {
		StringBuilder builder = new StringBuilder();
		for (Tag tag : tags) {
			builder.append(tag.getHtml());
		}
		return builder;
	}
}
