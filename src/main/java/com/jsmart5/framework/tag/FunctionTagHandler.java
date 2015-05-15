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
import javax.servlet.jsp.tagext.JspFragment;

import com.jsmart5.framework.exception.InvalidAttributeException;
import com.jsmart5.framework.json.Ajax;
import com.jsmart5.framework.json.Param;
import com.jsmart5.framework.manager.TagHandler;
import com.jsmart5.framework.tag.html.Input;
import com.jsmart5.framework.tag.html.Tag;
import com.jsmart5.framework.tag.type.Type;

import static com.jsmart5.framework.tag.js.JsConstants.*;

public final class FunctionTagHandler extends TagHandler {

	private String name;

	private String action;

	private Integer timeout;

	private String update;

	private String beforeSend;

	private String onError;

	private String onSuccess;

	private String onComplete;

	@Override
	public void validateTag() throws JspException {
		if (timeout != null && timeout < 0) {
			throw InvalidAttributeException.fromConstraint("function", "timeout", "greater or equal to 0"); 
		}
		if (action != null && action.trim().contains(" ")) {
			throw InvalidAttributeException.fromConflict("function", "action", "Value cannot contain space characters");
		}
	}

	@Override
	public Tag executeTag() throws JspException, IOException {

		// Just to call nested tags
		JspFragment body = getJspBody();
		if (body != null) {
			body.invoke(null);
		}

		Input input = new Input();
		input.addAttribute("id", id)
			.addAttribute("type", Type.HIDDEN.name().toLowerCase())
			.addAttribute("disabled", "disabled")
			.addAttribute("readonly", true);

		if (!args.isEmpty()) {
			String actionName = getTagName(J_SBMT_ARGS, action);
			input.addUniqueAttribute(actionName, getJsonHtmlValue(args));
		}

		for (String param : params.keySet()) {
			input.addUniqueAttribute(param, params.get(param));
		}

		Ajax jsonAjax = getJsonAjax(id);
		StringBuilder builder = new StringBuilder();
		builder.append(JSMART_AJAX.format(getJsonValue(jsonAjax)));

		appendScript(getFunction(name, builder));

		return input;
	}

	private Ajax getJsonAjax(String id) {
		Ajax jsonAjax = new Ajax();
		jsonAjax.setId(id);
		jsonAjax.setTimeout(timeout);
		jsonAjax.setTag("function");

		if (action != null) {
			jsonAjax.setMethod("post");
			jsonAjax.setAction(getTagName(J_SBMT, action));

			if (!args.isEmpty()) {
				String name = getTagName(J_SBMT_ARGS, action);
				jsonAjax.addArg(new Param(name, null));
			}

			for (String name : params.keySet()) {						
				jsonAjax.addParam(new Param(name, null));
			}
		} else if (update != null) {
			jsonAjax.setMethod("get");
		}
		if (update != null) {
			jsonAjax.setUpdate(update.trim());
		}
		if (beforeSend != null) {
			jsonAjax.setBefore((String) getTagValue(beforeSend.trim()));
		}
		if (onError != null) {
			jsonAjax.setError((String) getTagValue(onError.trim()));
		}
		if (onSuccess != null) {
			jsonAjax.setSuccess((String) getTagValue(onSuccess.trim()));
		}
		if (onComplete != null) {
			jsonAjax.setComplete((String) getTagValue(onComplete.trim()));
		}
		return jsonAjax;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public void setUpdate(String update) {
		this.update = update;
	}

	public void setBeforeSend(String beforeSend) {
		this.beforeSend = beforeSend;
	}

	public void setOnError(String onError) {
		this.onError = onError;
	}

	public void setOnSuccess(String onSuccess) {
		this.onSuccess = onSuccess;
	}

	public void setOnComplete(String onComplete) {
		this.onComplete = onComplete;
	}

}