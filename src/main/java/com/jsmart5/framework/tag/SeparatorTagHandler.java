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

import static com.jsmart5.framework.tag.CssConstants.CSS_LINK_DROPDOWN_SEPARATOR;
import static com.jsmart5.framework.tag.HtmlConstants.CLOSE_LIST_ITEM_TAG;
import static com.jsmart5.framework.tag.HtmlConstants.CLOSE_TAG;
import static com.jsmart5.framework.tag.HtmlConstants.OPEN_LIST_ITEM_TAG;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspTag;

import com.jsmart5.framework.manager.SmartTagHandler;

public class SeparatorTagHandler extends SmartTagHandler {

	@Override
	public void validateTag() throws JspException {
		// DO NOTHING
	}

	@Override
	public void executeTag() throws JspException, IOException {
		JspTag parent = getParent();

		if (parent instanceof ButtonTagHandler) {
			((ButtonTagHandler) parent).addActionItem(this);

		} else if (parent instanceof LinkTagHandler) {
			((LinkTagHandler) parent).addActionItem(this);
		
		} else if (parent instanceof MenuItemTagHandler) {
			StringBuilder builder = new StringBuilder(OPEN_LIST_ITEM_TAG);
			appendClass(builder, CSS_LINK_DROPDOWN_SEPARATOR);
			builder.append(CLOSE_TAG);
			builder.append(CLOSE_LIST_ITEM_TAG);
			printOutput(builder);
		}
	}

}
