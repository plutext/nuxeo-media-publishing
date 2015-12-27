/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class RestResponse {

    protected ClientResponse clientResponse;

    protected JsonNode responseAsJson;

    protected ObjectMapper objectMapper = new ObjectMapper();

    public RestResponse(ClientResponse clientResponse) {
        this.clientResponse = clientResponse;
    }

    public ClientResponse getClientResponse() {
        return this.clientResponse;
    }

    public int getStatus() {
        return this.clientResponse.getStatus();
    }

    public JsonNode asJson() {
        if (responseAsJson == null) {
            try {
                responseAsJson = objectMapper.readTree(clientResponse.getEntityInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return responseAsJson;
    }
}
