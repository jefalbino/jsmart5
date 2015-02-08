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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.JspTag;

import com.jsmart5.framework.json.JsonParam;
import com.jsmart5.framework.json.JsonParams;
import com.jsmart5.framework.manager.SmartTagHandler;

import static com.jsmart5.framework.tag.JsConstants.*;
import static com.jsmart5.framework.tag.CssConstants.*;
import static com.jsmart5.framework.tag.HtmlConstants.*;


public final class ListTagHandler extends SmartTagHandler {

	static final String ORDERED = "ordered";

	static final String UNORDERED = "unordered";

	static final String DEFINITION = "definition";

	private String var;

	private String value;

	private String select;

	private String type;

	private String emptyMessage;

	private boolean async = true;

	private final List<RowTagHandler> rows;

	public ListTagHandler() {
		rows = new ArrayList<RowTagHandler>();
	}

	@Override
	public boolean beforeTag() throws JspException, IOException {
		JspTag parent = getParent();
		if (parent instanceof GridTagHandler) {

			((GridTagHandler) parent).addTag(this);
			return false;
		}
		return true;
	}

	@Override
	public void validateTag() throws JspException {
		if (type != null && !type.equals(ORDERED) && !type.equals(UNORDERED) && !type.equals(DEFINITION)) {
			throw new JspException("Invalid type value for list tag. Valid values are "
					+ ORDERED + ", " + UNORDERED + ", " + DEFINITION);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void executeTag() throws JspException, IOException {

		// Just to call nested tags
		JspFragment body = getJspBody();
		if (body != null) {
			body.invoke(null);
		}

		HttpServletRequest request = getRequest();

		StringBuilder builder = new StringBuilder();

		if (type != null && type.equals(DEFINITION)) {
			builder.append(OPEN_DEFINITION_LIST_TAG);

		} else if (type != null && type.equals(ORDERED)) {
			builder.append(OPEN_ORDERED_LIST_TAG);

		} else { // UNORDERED
			builder.append(OPEN_UNORDERED_LIST_TAG);
		}

		if (id != null) {
			builder.append("id=\"" + id + "\" ");
		}
		if (style != null) {
			builder.append("style=\"" + style + "\" ");
		}
		if (styleClass != null) {
			builder.append("class=\"" + styleClass + "\" ");
		} else {
			appendClass(builder, CSS_LIST);
		}

		appendEvent(builder);
		builder.append(CLOSE_TAG);

		Object object = getTagValue(value);
		if (object instanceof List<?>) {

			List<Object> list = (List<Object>) object;

			if (list != null && !list.isEmpty()) {

				String command = ajaxCommand;

 				if (select != null) {
 					JsonParams jsonParams = new JsonParams();
 					jsonParams.addParam(new JsonParam(getTagName(J_SEL, select), getTagName(J_SEL, value)));
 					jsonParams.addParam(new JsonParam(getTagName(J_SEL_VAL, select), "%s"));

 					String parameters = "ajaxoutside=\"" + getJsonValue(jsonParams) + "\" ";

 					if (command != null) {
 						if (command.startsWith(ON_CLICK)) {
 							if (command.contains(JSMART_AJAX.toString())) {
 								command += parameters;
 							} else {
 								command = command.replace(ON_CLICK, ON_CLICK + JSMART_LIST.format(async, "$(this)")) + parameters;
 							}
 						} else {
 							command += ON_CLICK + JSMART_LIST.format(async, "$(this)") + "\" " + parameters;
 						}
 					} else {
 						command = ON_CLICK + JSMART_LIST.format(async, "$(this)") + "\" " + parameters;
 					}
 				}

				for (int i = 0; i < list.size(); i++) {
					request.setAttribute(var, list.get(i));

					for (RowTagHandler row : rows) {
	 					StringWriter sw = new StringWriter();
	 					row.setOutputWriter(sw);
	 					row.setType(type);
	 					setEvents(row);

	 					if (command != null) {
	 						if (select != null) {
	 							row.setAjaxCommand(String.format(command, i));
	 						} else {
	 							row.setAjaxCommand(command);
	 						}
	 					}

	 					row.executeTag();
	 					builder.append(sw.toString());
	 				}

					request.removeAttribute(var);
				}

			} else {

				String empty = (String) (emptyMessage != null ? getTagValue(emptyMessage) : "");
				if (type.equals(DEFINITION)) {
					builder.append(OPEN_DEFINITION_TITLE_TAG + empty + CLOSE_DEFINITION_TITLE_TAG);
				} else {
					builder.append(OPEN_LIST_ITEM_TAG);
					appendClass(builder, CSS_LIST_ROW);
					builder.append(CLOSE_TAG);
					builder.append(empty);
					builder.append(CLOSE_LIST_ITEM_TAG);
				}
			}
		}
		
		if (type != null && type.equals(DEFINITION)) {
			builder.append(CLOSE_DEFINITION_LIST_TAG);
		} else if (type != null && type.equals(ORDERED)) {
			builder.append(CLOSE_ORDERED_LIST_TAG);
		} else { // UNORDERED
			builder.append(CLOSE_UNORDERED_LIST_TAG);
		}
		
		printOutput(builder);
	}

	void addRow(RowTagHandler row) {
		rows.add(row);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setEmptyMessage(String emptyMessage) {
		this.emptyMessage = emptyMessage;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

}
