/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.core;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.websocket.server.HandshakeRequest;

import org.glassfish.tyrus.spi.SPIHandshakeRequest;
import org.glassfish.tyrus.websockets.Connection;
import org.glassfish.tyrus.websockets.WebSocketRequest;

/**
 * Implementation of all possible request interfaces. Should be only point of truth.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public final class RequestContext implements HandshakeRequest, SPIHandshakeRequest, WebSocketRequest {

    private final URI requestURI;
    private final String queryString;
    private final Connection connection;
    private final Object httpSession;
    private final boolean secure;
    private final Principal userPrincipal;
    private final Builder.IsUserInRoleDelegate isUserInRoleDelegate;

    private String requestPath;

    private final Map<String, List<String>> headers = new TreeMap<String, List<String>>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    });

    private final Map<String, List<String>> parameterMap;

    private RequestContext(URI requestURI, String requestPath, String queryString, Connection connection,
                           Object httpSession, boolean secure, Principal userPrincipal,
                           Builder.IsUserInRoleDelegate IsUserInRoleDelegate, Map<String, List<String>> parameterMap) {
        this.requestURI = requestURI;
        this.requestPath = requestPath;
        this.queryString = queryString;
        this.connection = connection;
        this.httpSession = httpSession;
        this.secure = secure;
        this.userPrincipal = userPrincipal;
        this.isUserInRoleDelegate = IsUserInRoleDelegate;
        this.parameterMap = parameterMap;
    }

    /**
     * Get headers.
     *
     * @return headers map. List items are corresponding to header declaration in HTTP request.
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Returns the header value corresponding to the name.
     *
     * @param name header name.
     * @return {@link List} of header values iff found, {@code null} otherwise.
     */
    public String getHeader(String name) {
        final List<String> stringList = headers.get(name);
        return stringList == null ? null : stringList.get(0);
    }

    /**
     * Gets the first header value from the {@link List} of header values corresponding to the name.
     *
     * @param name header name.
     * @return {@link String} value iff it exists, {@code null} otherwise.
     */
    public String getFirstHeaderValue(String name) {
        return getHeader(name);
    }

    @Override
    public void putSingleHeader(String headerName, String headerValue) {
        headers.put(headerName, Arrays.asList(headerValue));
    }

    @Override
    public String getRequestPath() {
        return requestPath;
    }

    @Override
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public URI getRequestURI() {
        return requestURI;
    }

    @Override
    public boolean isUserInRole(String role) {
        if(isUserInRoleDelegate != null) {
            return isUserInRoleDelegate.isUserInRole(role);
        }

        return false;
    }

    @Override
    public Object getHttpSession() {
        return httpSession;
    }

    @Override
    public Map<String, List<String>> getParameterMap() {
        return parameterMap;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRequestUri() {
        return requestURI.toString();
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * {@link RequestContext} builder.
     */
    public static final class Builder {

        private URI requestURI;
        private String requestPath;
        private String queryString;
        private Connection connection;
        private Object httpSession;
        private boolean secure;
        private Principal userPrincipal;
        private Builder.IsUserInRoleDelegate isUserInRoleDelegate;
        private Map<String, List<String>> parameterMap;


        /**
         * Create empty builder.
         *
         * @return empty builder instance.
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Set request URI.
         *
         * @param requestURI request URI to be set.
         * @return updated {@link RequestContext.Builder} instance.
         */
        public Builder requestURI(URI requestURI) {
            this.requestURI = requestURI;
            return this;
        }

        /**
         * Set query string.
         *
         * @param queryString query string to be set.
         * @return updated {@link RequestContext.Builder} instance.
         */
        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        /**
         * Set connection.
         *
         * @param connection connection to be set.
         * @return updated {@link RequestContext.Builder} instance.
         */
        public Builder connection(Connection connection) {
            this.connection = connection;
            return this;
        }

        /**
         * Set request path.
         *
         * @param requestPath request path to be set.
         * @return updated {@link RequestContext.Builder} instance.
         */
        public Builder requestPath(String requestPath) {
            this.requestPath = requestPath;
            return this;
        }

        /**
         * Set http session.
         *
         * @param httpSession http session to be set.
         * @return updated {@link RequestContext.Builder} instance.
         * @see {@code javax.servlet.http.HttpSession}
         */
        public Builder httpSession(Object httpSession) {
            this.httpSession = httpSession;
            return this;
        }

        /**
         * Set secure state.
         *
         * @param secure secure state to be set.
         * @return updated {@link RequestContext.Builder} instance.
         */
        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Set {@link Principal}.
         *
         * @param principal principal to be set.
         * @return updated {@link RequestContext.Builder} instance.
         */
        public Builder userPrincipal(Principal principal) {
            this.userPrincipal = principal;
            return this;
        }

        /**
         * Set delegate for {@link RequestContext#isUserInRole(String)} method.
         *
         * @param isUserInRoleDelegate delegate for {@link RequestContext#isUserInRole(String)}.
         * @return updated {@link RequestContext.Builder} instance.
         */
        public Builder isUserInRoleDelegate(IsUserInRoleDelegate isUserInRoleDelegate) {
            this.isUserInRoleDelegate = isUserInRoleDelegate;
            return this;
        }

        /**
         * Set parameter map.
         *
         * @param parameterMap parameter map. Takes map returned from ServletRequest#getParameterMap.
         * @return updated {@link RequestContext.Builder} instance.
         */
        public Builder parameterMap(Map<String, String[]> parameterMap) {
            if(parameterMap != null) {
                this.parameterMap = new HashMap<String, List<String>>();
                for(Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    this.parameterMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }
            } else {
                this.parameterMap = null;
            }

            return this;
        }


        /**
         * Build {@link RequestContext} from given properties.
         *
         * @return created {@link RequestContext}.
         */
        public RequestContext build() {
            return new RequestContext(requestURI, requestPath, queryString, connection, httpSession, secure,
                    userPrincipal, isUserInRoleDelegate,
                    parameterMap != null ? parameterMap : new HashMap<String, List<String>>());
        }

        /**
         * Is user in role delegate.
         * <p/>
         * Cannot easily query ServletContext or HttpServletRequest for this information, since it is stored only as
         * object.
         */
        public interface IsUserInRoleDelegate {

            /**
             * Returns a boolean indicating whether the authenticated user is included in the specified logical "role".
             * Roles and role membership can be defined using deployment descriptors. If the user has not been
             * authenticated, the method returns false.
             *
             * @param role a String specifying the name of the role.
             * @return a boolean indicating whether the user making this request belongs to a given role; false if the
             * user has not been authenticated.
             */
            public boolean isUserInRole(String role);
        }
    }
}
