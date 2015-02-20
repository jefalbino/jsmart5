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

package com.jsmart5.framework.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspTag;

import com.jsmart5.framework.manager.SmartTagHandler;
import com.jsmart5.framework.util.SmartImage;
import com.jsmart5.framework.util.SmartUtils;

public final class CarouselItemTagHandler extends SmartTagHandler {

	private String lib;

	private String name;

	private String caption;

	private String link;

	@Override
	public boolean beforeTag() throws JspException, IOException {
		JspTag parent = getParent();
		if (parent instanceof CarouselTagHandler) {

			((CarouselTagHandler) parent).addItem(this);
			return false;
		}
		return true;
	}

	@Override
	public void validateTag() throws JspException {
		// DO NOTHING
	}

	@Override
	public void executeTag() throws JspException, IOException {
//		String libValue = (String) getTagValue(lib);
//		String nameValue = (String) getTagValue(name);
//
//		StringBuilder builder = new StringBuilder();
//		builder.append(OPEN_DIV_TAG + "id=\"" + id + "\" ");
//		appendClass(builder, CSS_CAROUSEL_ITEM);
//		builder.append(CLOSE_TAG);
//
//		if (link != null) {
//			builder.append(OPEN_LINK_TAG + "href=\"" + SmartUtils.decodePath((String) getTagValue(link)) + "\" >");
//			builder.append(IMG_TAG + "src=\"" + SmartImage.IMAGES.getImage(libValue, nameValue) + "\" alt=\"" + name + "\" />");
//			builder.append(CLOSE_LINK_TAG);
//		} else {
//			builder.append(IMG_TAG + "src=\"" + SmartImage.IMAGES.getImage(libValue, nameValue) + "\" alt=\"" + name + "\" />");
//		}
//
//		if (caption != null) {
//			builder.append(OPEN_LABEL_TAG);
//			builder.append(CLOSE_TAG);
//			if (link != null) {
//				builder.append(OPEN_LINK_TAG + "href=\"" + SmartUtils.decodePath((String) getTagValue(link)) + "\" >");
//				builder.append(getTagValue(caption));
//				builder.append(CLOSE_LINK_TAG);
//			} else {
//				builder.append(getTagValue(caption));
//			}
//			builder.append(CLOSE_LABEL_TAG);
//		}
//
//		builder.append(CLOSE_DIV_TAG);
//
//		printOutput(builder);
	}

	public void setLib(String lib) {
		this.lib = lib;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public void setLink(String link) {
		this.link = link;
	}

}
