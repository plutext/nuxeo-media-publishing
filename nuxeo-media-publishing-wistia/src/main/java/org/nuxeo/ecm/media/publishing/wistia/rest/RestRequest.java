/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *      André Justo
 */

package org.nuxeo.ecm.media.publishing.wistia.rest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.MultiPart;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

public class RestRequest {

    protected WebResource service;

    protected String path;

    protected RequestType requestType = RequestType.GET;

    protected String data;

    protected Map<String, Object> headers = new HashMap<String, Object>();

    protected MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

    protected String contentType = MediaType.APPLICATION_JSON;

    public RestRequest(WebResource service, String path) {
        this.service = service;
        this.path = path;
    }

    public RestRequest header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public RestRequest headers(Map<String, Object> headers) {
        this.headers = headers;
        return this;
    }

    public RestRequest queryParam(String key, String value) {
        this.queryParams.add(key, value);
        return this;
    }

    public RestRequest queryParams(MultivaluedMap<String, String> queryParams) {
        if (queryParams != null) {
            this.queryParams = queryParams;
        }
        return this;
    }

    public RestRequest requestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    public RestRequest contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public RequestType getRequestType() {
        return this.requestType;
    }

    public String getData() {
        return this.data;
    }

    public Map<String, Object> getHeaders() {
        return this.headers;
    }

    public MultivaluedMap<String, String> getQueryParams() {
        return this.queryParams;
    }

    public String getContentType() {
        return this.contentType;
    }

    public RestResponse execute(MultiPart multiPart) {
        WebResource wr = service;
        wr = wr.path(path);
        if (queryParams != null && !queryParams.isEmpty()) {
            wr = wr.queryParams(queryParams);
        }

        WebResource.Builder builder = wr.type(contentType);
        for (Map.Entry<String, Object> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        ClientResponse response = null;
        switch (requestType) {
            case GET:
                response = builder.get(ClientResponse.class);
                break;
            case POST:
                if (multiPart != null) {
                    response = builder.post(ClientResponse.class, multiPart);
                }
                else {
                    response = builder.post(ClientResponse.class, data);
                }
                break;
            case PUT:
                response = builder.put(ClientResponse.class, data);
                break;
            case DELETE:
                response = builder.delete(ClientResponse.class, data);
                break;
        }
        return new RestResponse(response);
    }

    public RestResponse execute() {
        return execute(null);
    }
}
