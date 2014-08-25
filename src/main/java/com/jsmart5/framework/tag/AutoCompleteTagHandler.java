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

import static com.jsmart5.framework.tag.HtmlConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;

import com.jsmart5.framework.json.JSONAutoComplete;
import com.jsmart5.framework.manager.SmartTagHandler;

public final class AutoCompleteTagHandler extends SmartTagHandler {

	private static final int DEFAULT_MAX_RESULTS = 10;

	private static final String COMPLETE_VALUES = "_complete_values";

	private String value;

	private boolean async = true;

	private String complete;

	private boolean disabled;

	private boolean multiple;

	private Integer minLength;

	private Integer maxResults = DEFAULT_MAX_RESULTS;

	private String onValueSelect;

	private String placeHolder;
	
	private String label;

	private String afterAjax;

	private String beforeAjax;

	private Integer tabIndex;

	private boolean autoFocus;

	@Override
	public void validateTag() throws JspException {
		if (minLength != null && minLength <= 0) {
			throw new JspException("Attribute minLength must be greater than zero for autocomplete tag");
		}
		if (maxResults != null && maxResults <= 0) {
			throw new JspException("Attribute maxResults must be greater than zero for autocomplete tag");
		}
	}

	@Override
	public void executeTag() throws JspException, IOException {

		// Just to call nested tags
		JspFragment body = getJspBody();
		if (body != null) {
			body.invoke(null);
		}

		StringBuilder builder = new StringBuilder();
		
		if (label != null) {
			builder.append(OPEN_DIV_TAG);
			appendClass(builder, CssConstants.CSS_INPUT_GROUP);
			builder.append(">");
			builder.append(OPEN_SPAN_TAG);
			appendClass(builder, CssConstants.CSS_INPUT_LABEL);
			builder.append(">");

			String labelVal = (String) getTagValue(label);
			if (labelVal != null) {
				builder.append(labelVal);
			}
			builder.append(CLOSE_SPAN_TAG);
		}
		
		builder.append(INPUT_TAG);

		builder.append("id=\"" + id + "\" type=\"text\" ");

		String name = getTagName(J_TAG, value);
		if (name != null) {
			builder.append("name=\"" + name + "\" ");
		}
		if (style != null) {
			builder.append("style=\"" + style + "\" ");
		}
		if (styleClass != null) {
			builder.append("class=\"" + styleClass + "\" ");
		} else {
			appendClass(builder, CssConstants.CSS_AUTO_COMPLETE);
		}

		appendFormValidator(builder);

		appendRest(builder);

		if (tabIndex != null) {
			builder.append("tabindex=\"" + tabIndex + "\" ");
		}
		if (placeHolder != null) {
			builder.append("placeholder=\"" + getResourceString(placeHolder) + "\" datatype=\"text\" ");
		}
		if (autoFocus) {
			builder.append("autofocus=\"autofocus\" ");
		}
		if (disabled || isEditRowTagEnabled()) {
			builder.append("disabled=\"disabled\" ");
		}

		JSONAutoComplete jsonAutoComplete = new JSONAutoComplete();
		jsonAutoComplete.setName(getTagName(J_COMPLETE, "@{" + id + "}"));

		if (beforeAjax != null) {
			jsonAutoComplete.setBefore(beforeAjax.trim());
		}
		if (afterAjax != null) {
			jsonAutoComplete.setExec(afterAjax.trim());
		}
		if (maxResults != null) {
			jsonAutoComplete.setMaxResults(String.valueOf(maxResults));
		}
		if (minLength != null) {
			jsonAutoComplete.setMinLength(String.valueOf(minLength));
		}
		if (onValueSelect != null) {
			jsonAutoComplete.setCallback(onValueSelect.trim());
		}
		jsonAutoComplete.setMultiple(String.valueOf(multiple));

		builder.append("ajax=\"" + getJSONValue(jsonAutoComplete) + "\" ");

		builder.append(ON_KEY_UP + JSConstants.JSMART_AUTOCOMPLETE.format(async, id) + "\" ");

		appendEvent(builder);

		builder.append("/>");
		
		if (label != null) {
			builder.append(CLOSE_DIV_TAG);
		}

		List<String> completeValues = getCompleteValues();
		if (completeValues != null) {
			builder.append(OPEN_DIV_TAG + "id=\"" + id + COMPLETE_VALUES + "\" ");
			appendClass(builder, CssConstants.CSS_AUTO_COMPLETE_LIST);
			builder.append(">");
			builder.append(OPEN_UNORDERED_LIST_TAG + ">");

			for (int i = 0; i < completeValues.size(); i++) {
				if (i > maxResults) {
					break;
				}
				builder.append(OPEN_LIST_ITEM_TAG + ">" + completeValues.get(i) + CLOSE_LIST_ITEM_TAG);
			}
			builder.append(CLOSE_UNORDERED_LIST_TAG);
			builder.append(CLOSE_DIV_TAG);
		}

		printOutput(builder);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> getCompleteValues() throws JspException {
		HttpServletRequest request = getRequest();
		String tagParamName = getTagName(J_COMPLETE, "@{" + id + "}");

		if (request.getParameter(tagParamName) != null) {

			List<String> listValues = new ArrayList<String>();
			String completeValue = request.getParameter(tagParamName);

			Object tagValue = getTagValue(complete);
			
			Collection<Object> values = null;

			if (tagValue instanceof SmartAutoCompleteAdapter) {
				values = ((SmartAutoCompleteAdapter) tagValue).search(completeValue, maxResults);

			} else if (tagValue instanceof Collection) {
				values = (Collection<Object>) tagValue;

			} else if (tagValue != null) {
	 	 		throw new JspException("The value of attribute named 'complete' for autocomplete tag must be instance of an Collection or SmartAutoCompleteAdapter!");
	 	 	}

			if (values != null) {
				for (Object value : values) {
					if (value != null) {
						listValues.add(value.toString());
					}
				}
			}
			return listValues;
		}
		return null;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public void setComplete(String complete) {
		this.complete = complete;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	public void setOnValueSelect(String onValueSelect) {
		this.onValueSelect = onValueSelect;
	}

	public void setPlaceHolder(String placeHolder) {
		this.placeHolder = placeHolder;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	public void setAfterAjax(String afterAjax) {
		this.afterAjax = afterAjax;
	}

	public void setBeforeAjax(String beforeAjax) {
		this.beforeAjax = beforeAjax;
	}

	public void setTabIndex(Integer tabIndex) {
		this.tabIndex = tabIndex;
	}

	public void setAutoFocus(boolean autoFocus) {
		this.autoFocus = autoFocus;
	}

}
