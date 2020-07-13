package org.bndly.examples.rest.servlet;

/*-
 * #%L
 * Bndly examples REST HATEOAS
 * %%
 * Copyright (C) 2020 Cybercon GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.bndly.rest.api.DefaultCharacterEncodingProvider;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.api.ResourceURIParser;
import org.bndly.rest.base.ResourceDelegatingServletBase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * This Servlet is nothing more than a bridge between sling and the actual functionality.
 * It is separated because you can use the functionality in another environment as well
 * where servlet registration may be done differently.
 */
@Component(
        service = { Servlet.class },
        property = {
                HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=/restapi/*",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME + "=RestApiServlet",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=org.osgi.service.http)"
        }
)
public class DelegatingRestServlet implements Servlet {

    private static final Logger LOG = LoggerFactory.getLogger(org.bndly.examples.rest.servlet.DelegatingRestServlet.class);

    @Reference
    private DefaultCharacterEncodingProvider defaultCharacterEncodingProvider;

    @Reference
    private ResourceDelegatingServletBase resourceDelegatingServletBase;

    private ServletConfig servletConfig;
    private ResourceURI contextUri;
    private ResourceURI linkUri = null;
    private String contextPath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        servletConfig = config;
        contextPath = "/restapi/";
        contextUri = new ResourceURIParser(defaultCharacterEncodingProvider.createPathCoder(), contextPath).parse().getResourceURI();
    }

    @Override
    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        resourceDelegatingServletBase.service(req, res, linkUri, contextUri, servletConfig);
    }

    @Override
    public String getServletInfo() {
        return this.getClass().getName();
    }

    @Override
    public void destroy() {
        servletConfig = null;
    }
}
