package nxt.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;


public class FakeServletRequest implements HttpServletRequest{

	
	private HttpServletRequest originalRequest = null;
	public FakeServletRequest(HttpServletRequest r){
		this.originalRequest = r;
	}
	
	@Override
	public AsyncContext getAsyncContext() {
		return originalRequest.getAsyncContext();
	}

	@Override
	public Object getAttribute(String arg0) {
		return originalRequest.getAttribute(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return originalRequest.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding() {
		return originalRequest.getCharacterEncoding();
	}

	@Override
	public int getContentLength() {
		return originalRequest.getContentLength();
	}

	@Override
	public long getContentLengthLong() {
		return originalRequest.getContentLength();
	}

	@Override
	public String getContentType() {
		return originalRequest.getContentType();
	}

	@Override
	public DispatcherType getDispatcherType() {
		return originalRequest.getDispatcherType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return originalRequest.getInputStream();
	}

	@Override
	public String getLocalAddr() {
		return originalRequest.getLocalAddr();
	}

	@Override
	public String getLocalName() {
		return originalRequest.getLocalName();
	}

	@Override
	public int getLocalPort() {
		return originalRequest.getLocalPort();
	}

	@Override
	public Locale getLocale() {
		return originalRequest.getLocale();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return originalRequest.getLocales();
	}

	@Override
	public String getParameter(String arg0) {
		return originalRequest.getParameter(arg0);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return originalRequest.getParameterMap();
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return originalRequest.getParameterNames(); 
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return originalRequest.getParameterValues(arg0);
	}

	@Override
	public String getProtocol() {
		return originalRequest.getProtocol();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return originalRequest.getReader();
	}

	@Override
	public String getRealPath(String arg0) {
		return originalRequest.getRealPath(arg0);
	}

	@Override
	public String getRemoteAddr() {
		return originalRequest.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return originalRequest.getRemoteHost();
	}

	@Override
	public int getRemotePort() {
		return originalRequest.getRemotePort();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return originalRequest.getRequestDispatcher(arg0);
	}

	@Override
	public String getScheme() {
		return originalRequest.getScheme();
	}

	@Override
	public String getServerName() {
		return originalRequest.getServerName();
	}

	@Override
	public int getServerPort() {
		return originalRequest.getServerPort();
	}

	@Override
	public ServletContext getServletContext() {
		return originalRequest.getServletContext();
	}

	@Override
	public boolean isAsyncStarted() {
		return originalRequest.isAsyncStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		return originalRequest.isAsyncSupported();
	}

	@Override
	public boolean isSecure() {
		return originalRequest.isSecure();
	}

	@Override
	public void removeAttribute(String arg0) {
		originalRequest.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		originalRequest.setAttribute(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		originalRequest.setCharacterEncoding(arg0);
		
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return originalRequest.startAsync();
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
			throws IllegalStateException {
		return originalRequest.startAsync(arg0, arg1);
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		return originalRequest.authenticate(arg0);
	}

	@Override
	public String changeSessionId() {
		return originalRequest.changeSessionId();
	}

	@Override
	public String getAuthType() {
		return originalRequest.getAuthType();
	}

	@Override
	public String getContextPath() {
		return originalRequest.getContextPath();
	}

	@Override
	public Cookie[] getCookies() {
		return originalRequest.getCookies();
	}

	@Override
	public long getDateHeader(String arg0) {
		return originalRequest.getDateHeader(arg0);
	}

	@Override
	public String getHeader(String arg0) {
		return originalRequest.getHeader(arg0);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return originalRequest.getHeaderNames();
	}

	@Override
	public Enumeration<String> getHeaders(String arg0) {
		return originalRequest.getHeaders(arg0);
	}

	@Override
	public int getIntHeader(String arg0) {
		return originalRequest.getIntHeader(arg0);
	}

	@Override
	public String getMethod() {
		return originalRequest.getMethod();
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		return originalRequest.getPart(arg0);
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return originalRequest.getParts();
	}

	@Override
	public String getPathInfo() {
		return originalRequest.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return originalRequest.getPathTranslated();
	}

	@Override
	public String getQueryString() {
		return originalRequest.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		return originalRequest.getRemoteUser();
	}

	@Override
	public String getRequestURI() {
		return originalRequest.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return originalRequest.getRequestURL();
	}

	@Override
	public String getRequestedSessionId() {
		return originalRequest.getRequestedSessionId();
	}

	@Override
	public String getServletPath() {
		return originalRequest.getServletPath();
	}

	@Override
	public HttpSession getSession() {
		return originalRequest.getSession();
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		return originalRequest.getSession(arg0);
	}

	@Override
	public Principal getUserPrincipal() {
		return originalRequest.getUserPrincipal();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return originalRequest.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return originalRequest.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return originalRequest.isRequestedSessionIdFromUrl();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return originalRequest.isRequestedSessionIdValid();
	}

	@Override
	public boolean isUserInRole(String arg0) {
		return originalRequest.isUserInRole(arg0);
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		originalRequest.login(arg0, arg1);
	}

	@Override
	public void logout() throws ServletException {
		originalRequest.logout();
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0)
			throws IOException, ServletException {
		return originalRequest.upgrade(arg0);
	}

	

	
}
