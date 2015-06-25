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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.el.ExpressionFactory;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.gson.Gson;
import com.jsmart5.framework.annotation.WebBean;
import com.jsmart5.framework.util.WebAlert;
import com.jsmart5.framework.util.WebAlert.AlertType;
import com.jsmart5.framework.util.WebUtils;

/**
 * This class represents the context of the request being currently processed and it allows {@link WebBean}
 * to get an instance of {@link ServletContext}, {@link HttpSession}, {@link HttpServletRequest} or 
 * {@link HttpServletResponse}.
 * <br>
 * This class also include methods to add message to client side, check if request is Ajax request or 
 * retrieve attributes from the request, session or application.
 */
public final class WebContext implements Serializable {

	private static final long serialVersionUID = -3910553204750683737L;

	private static final JspFactory JSP_FACTORY = JspFactory.getDefaultFactory();

	private static final Map<Thread, WebContext> THREADS = new ConcurrentHashMap<Thread, WebContext>();

    private static final Gson gson = new Gson();

	private static Servlet smartServlet;

	private static JspApplicationContext jspContext;

	private HttpServletRequest request;

    private String bodyContent;

	private HttpServletResponse response;

    private boolean responseWritten;

	private String redirectTo;

	private boolean invalidate;

	private PageContext pageContext;

	private Map<String, List<WebAlert>> alerts = new LinkedHashMap<String, List<WebAlert>>();

	private Map<String, Object> mappedValues = new ConcurrentHashMap<String, Object>();

    private Map<String, String> queryParams;

	private WebContext(final HttpServletRequest request, final HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	static final void setServlet(final Servlet servlet) {
		smartServlet = servlet;
		jspContext = JSP_FACTORY.getJspApplicationContext(servlet.getServletConfig().getServletContext());
	}

	private static final WebContext getCurrentInstance() {
		return THREADS.get(Thread.currentThread());
	}

	static final void initCurrentInstance(final HttpServletRequest request, final HttpServletResponse response) {
		THREADS.put(Thread.currentThread(), new WebContext(request, response));
	}

	static final void closeCurrentInstance() {
		THREADS.remove(Thread.currentThread()).close();
	}

	private void close() {
		if (invalidate) {
			request.getSession().invalidate();
		}
		invalidate = false;
		request = null;
        bodyContent = null;
		response = null;
        responseWritten = false;
		redirectTo = null;
		alerts.clear();
		alerts = null;
		mappedValues.clear();
		mappedValues = null;
		JSP_FACTORY.releasePageContext(pageContext);
		pageContext = null;
	}

	static PageContext getPageContext() {
		WebContext context = getCurrentInstance();
		return context != null ? context.getPage() : null;
	}

	private PageContext getPage() {
		if (pageContext == null) {
			pageContext = JSP_FACTORY.getPageContext(smartServlet, request, response, null, true, 8192, true);
		}
		return pageContext;
	}

	static ExpressionFactory getExpressionFactory() {
		return jspContext.getExpressionFactory();
	}

	/**
	 * Returns the current {@link ServletContext} instance associated to the request
	 * being processed.
	 *  
	 * @return a instance of {@link ServletContext}.
	 */
	public static ServletContext getApplication() {
		return smartServlet.getServletConfig().getServletContext();
	}

	/**
	 * Returns the current {@link HttpSession} instance associated to the request being
	 * processed.
	 * 
	 * @return a instance of {@link HttpSession}.
	 */
	public static HttpSession getSession() {
		WebContext context = getCurrentInstance();
		return context != null ? context.request.getSession() : null;
	}

	/**
	 * Returns the current {@link HttpServletRequest} instance associated to the request being
	 * processed.
	 * 
	 * @return a instance of {@link HttpServletRequest}.
	 */
	public static HttpServletRequest getRequest() {
		WebContext context = getCurrentInstance();
		return context != null ? context.request : null;
	}

    public static Map<String, String> getQueryParams() {
        WebContext context = getCurrentInstance();
        if (context == null) {
            return null;
        }
        if (context.queryParams == null) {
            context.queryParams = new ConcurrentHashMap<String, String>();

            final String queryParam = context.request.getQueryString();
            if (queryParam == null || queryParam.trim().isEmpty()) {
                return context.queryParams;
            }

            for (String param : context.request.getParameterMap().keySet()) {
                if (queryParam.contains(param + "=")) {
                    context.queryParams.put(param, context.request.getParameter(param));
                }
            }
        }
        return context.queryParams;
    }

	/**
	 * Returns the current {@link HttpServletResponse} instance associated to the request 
	 * being processed.
	 * 
	 * @return a instance of {@link HttpServletResponse}
	 */
	public static HttpServletResponse getResponse() {
		WebContext context = getCurrentInstance();
		return context != null ? context.response : null;
	}

	static String getRedirectTo() {
		WebContext context = getCurrentInstance();
		return context != null ? context.redirectTo : null;
	}

	/**
	 * Redirect the request to the specified link path after the current request is processed.
	 * <br>
	 * Case this method is called on {@link PostConstruct} annotated method, the redirect is done after the
	 * {@link PostConstruct} annotated method execution.
	 * 
	 * @param path path mapped on configuration file or general valid URL link.
	 */
	public static void redirectTo(final String path) {
		WebContext context = getCurrentInstance();
		if (context != null) {
			context.redirectTo = WebUtils.decodePath(path);
		}
	}

	/**
	 * Calling this method will cause the current {@link HttpSession} to be invalidated after the request
	 * processing is done. It means that the session will be invalidated after {@link WebBean} life cycle
	 * is completed.
	 * <br>
	 * Case there is a need to invalidate the session at the moment of the execution, use {@link HttpSession}
	 * invalidate method instead.  
	 */
	public static void invalidate() {
		WebContext context = getCurrentInstance();
		if (context != null) {
			context.invalidate = true;
		}
	}

	/**
	 * Returns the {@link Locale} of the client associated to the request being processed.
	 * 
	 * @return {@link Locale} instance.
	 */
	public static Locale getLocale() {
		HttpServletRequest request = getRequest();
		return request != null ? request.getLocale() : null;
	}

	/**
	 * Returns <code>true</code> if the request being process was triggered by Ajax on client side,
	 * <code>false</code> otherwise.
	 * 
	 * @return boolean value indicating if request was done using Ajax.
	 */
	public static boolean isAjaxRequest() {
		HttpServletRequest request = getRequest();
		return request != null ? "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) : false;
	}

	static List<WebAlert> getAlerts(final String id) {
		WebContext context = getCurrentInstance();
		return context != null ? context.alerts.get(id) : null;
	}

	public static void addAlert(final String id, final WebAlert alert) {
		WebContext context = getCurrentInstance();
		if (context != null && id != null && alert != null) {
			List<WebAlert> alerts = context.alerts.get(id);
			if (alerts == null) {
				context.alerts.put(id, alerts = new ArrayList<WebAlert>());
			}
			alerts.add(alert);
		}
	}

	/**
	 * Add info alert to be presented on client side after the response is returned. 
	 * <br>
	 * This method only take effect if the alert tag is mapped with specified id and with position fixed
	 * on the page returned to the client.
	 * <br>
	 * The message is placed on the same position where the the message tag is mapped.
	 * 
	 * @param id of the tag to receive the message.
	 * @param message to be presented on the client side.
	 */
	public static void addInfo(final String id, final String message) {
		WebAlert alert = new WebAlert(AlertType.INFO);
		alert.setMessage(message);
		addAlert(id, alert);
	}

	/**
	 * Add warning alert to be presented on client side after the response is returned. 
	 * <br>
	 * This method only take effect if the alert tag is mapped with specified id and with position fixed
	 * on the page returned to the client.
	 * <br>
	 * The message is placed on the same position where the the message tag is mapped.
	 * 
	 * @param id of the tag to receive the message.
	 * @param message to be presented on the client side.
	 */
	public static void addWarning(final String id, final String message) {
		WebAlert alert = new WebAlert(AlertType.WARNING);
		alert.setMessage(message);
		addAlert(id, alert);
	}
	
	/**
	 * Add success alert to be presented on client side after the response is returned. 
	 * <br>
	 * This method only take effect if the alert tag is mapped with specified id and with position fixed
	 * on the page returned to the client.
	 * <br>
	 * The message is placed on the same position where the the message tag is mapped.
	 * 
	 * @param id of the tag to receive the message.
	 * @param message to be presented on the client side.
	 */
	public static void addSuccess(final String id, final String message) {
		WebAlert alert = new WebAlert(AlertType.SUCCESS);
		alert.setMessage(message);
		addAlert(id, alert);
	}
	
	/**
	 * Add error alert to be presented on client side after the response is returned. 
	 * <br>
	 * This method only take effect if the alert tag is mapped with specified id and with position fixed
	 * on the page returned to the client.
	 * <br>
	 * The message is placed on the same position where the the message tag is mapped.
	 * 
	 * @param id of the tag to receive the message.
	 * @param message to be presented on the client side.
	 */
	public static void addError(final String id, final String message) {
		WebAlert alert = new WebAlert(AlertType.DANGER);
		alert.setMessage(message);
		addAlert(id, alert);
	}

	static Object getMappedValue(final String name) {
		WebContext context = getCurrentInstance();
		if (context != null) {
			return context.mappedValues.get(name);
		}
		return null;
	}
	
	static Object removeMappedValue(final String name) {
		WebContext context = getCurrentInstance();
		if (context != null) {
			return context.mappedValues.remove(name);
		}
		return null;
	}

	static void addMappedValue(final String name, final Object value) {
		WebContext context = getCurrentInstance();
		if (context != null) {
			context.mappedValues.put(name, value);
		}
	}

	/**
	 * Returns the attribute carried on {@link HttpServletRequest}, {@link HttpSession} or {@link ServletContext}
	 * instances associated with current request being processed.
	 * 
	 * @param name name of the attribute.
	 * @return the {@link Object} mapped by attribute name on the current request.
	 */
	public static Object getAttribute(final String name) {
		if (name != null) {
			HttpServletRequest request = getRequest();
			if (request != null && request.getAttribute(name) != null) {
				return request.getAttribute(name);
			}
			
			HttpSession session = getSession();
			if (session != null) {
				synchronized (session) {
					if (session.getAttribute(name) != null) {
						return session.getAttribute(name);
					}
				}
			}

			ServletContext application = getApplication();
			if (application.getAttribute(name) != null) {
				return application.getAttribute(name); 
			}
		}
		return null;
	}

	/**
	 * Check if attribute is carried on {@link HttpServletRequest}, {@link HttpSession} or {@link ServletContext}
	 * instances associated with current request being processed.
	 * 
	 * @param name name of the attribute.
	 * @return <code>true</code> if the attribute is contained in one of the instances {@link HttpServletRequest}, 
	 * {@link HttpSession} or {@link ServletContext}, <code>false</code> otherwise.
	 */
	public static boolean containsAttribute(final String name) {
		if (name != null) {
			HttpServletRequest request = getRequest();
			if (request != null && request.getAttribute(name) != null) {
				return true;
			}

			HttpSession session = getSession();
			if (session != null) {
				synchronized (session) {
					if (session.getAttribute(name) != null) {
						return true;
					}
				}
			}

			return getApplication().getAttribute(name) != null;
		}
		return false;
	}

	public static boolean checkReCaptcha(final String secretKey) {
		String responseField = (String) getMappedValue(ReCaptchaHandler.RESPONSE_V1_FIELD_NAME);
		if (responseField != null) {
			return ReCaptchaHandler.checkReCaptchaV1(secretKey, responseField);
		}

		responseField = (String) getMappedValue(ReCaptchaHandler.RESPONSE_V2_FIELD_NAME);
		if (responseField != null) {
			return ReCaptchaHandler.checkReCaptchaV2(secretKey, responseField);
		}
		throw new RuntimeException("ReCaptcha not found on this submit. Plase make sure the recaptcha tag is included on submitted form");
	}

    public static String getContentAsString() throws IOException {
        WebContext context = getCurrentInstance();
        if (context == null) {
            return null;
        }
        if (context.bodyContent == null) {
            String line = null;
            final StringBuffer buffer = new StringBuffer();

            BufferedReader reader = context.request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            context.bodyContent = buffer.toString();
        }
        return context.bodyContent;
    }

    public static <T> T getContentFromJson(Class<T> clazz) throws IOException {
        return gson.fromJson(getContentAsString(), clazz);
    }

    public static <T> T getContentFromXml(Class<T> clazz) throws IOException, JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(getContentAsString());
        return (T) unmarshaller.unmarshal(reader);
    }

    static boolean isResponseWritten() {
        WebContext context = getCurrentInstance();
        return context != null ? context.responseWritten : false;
    }

    public static void writeResponseAsString(String responseVal) throws IOException {
        WebContext context = getCurrentInstance();
        if (context != null) {
            context.responseWritten = true;
            PrintWriter writer = context.response.getWriter();
            writer.write(responseVal);
            writer.flush();
        }
    }

    public static void writeResponseAsJson(Object object) throws IOException {
        WebContext context = getCurrentInstance();
        if (context != null) {
            context.responseWritten = true;
            context.response.setContentType("application/json");
            PrintWriter writer = context.response.getWriter();
            writer.write(gson.toJson(object));
            writer.flush();
        }
    }

    public static void writeResponseAsXml(Object object) throws IOException, JAXBException {
        WebContext context = getCurrentInstance();
        if (context != null) {
            context.responseWritten = true;
            context.response.setContentType("application/xml");
            PrintWriter writer = context.response.getWriter();

            final JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            final Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(object, writer);
            writer.flush();
        }
    }

    public static void writeResponseAsEventStream(final String event, final String data, final Long retry) throws IOException {
        WebContext context = getCurrentInstance();
        if (context != null) {
            context.responseWritten = true;
            context.response.setContentType("text/event-stream");
            PrintWriter printWriter = context.response.getWriter();

            printWriter.write("retry:" + retry + "\n");
            printWriter.write("event:" + event + "\n");
            printWriter.write("data:" + data + "\n\n");
            printWriter.flush();
        }
    }

    public static void writeResponseAsFileStream(final File file, final int bufferSize) throws IOException {
        WebContext context = getCurrentInstance();
        if (context != null) {
            context.responseWritten = true;
            context.response.setHeader("Content-Disposition", "attachment;filename=\"" + file.getName() + "\"");
            context.response.addHeader("Content-Length", Long.toString(file.length()));
            context.response.setContentLength((int) file.length());

            String mimetype = getApplication().getMimeType(file.getName());
            context.response.setContentType((mimetype != null) ? mimetype : "application/octet-stream");

            FileInputStream fileInputStream = new FileInputStream(file);
            ServletOutputStream outputStream = context.response.getOutputStream();

            try {
                int i;
                byte[] buffer = new byte[bufferSize];
                while ((i = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, i);
                }
            } finally {
                outputStream.flush();
                fileInputStream.close();
            }
        }
    }
}