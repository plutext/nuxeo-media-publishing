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
import org.nuxeo.ecm.media.publishing.wistia.model.Media;
import org.nuxeo.ecm.media.publishing.wistia.model.Project;
import org.nuxeo.ecm.media.publishing.wistia.model.Account;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

public class WistiaResponseParser {

    static ObjectMapper mapper = new ObjectMapper();

    public static Project asProject(ClientResponse clientResponse) {
        Project project;
        try {
            project = mapper.readValue(clientResponse.getEntityInputStream(), Project.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return project;
    }

    public static List<Project> asProjectList(ClientResponse clientResponse) {
        List<Project> projects;
        ObjectMapper mapper = new ObjectMapper();
        try {
            projects = mapper.readValue(clientResponse.getEntityInputStream(), mapper.getTypeFactory().constructCollectionType(List.class, Project.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return projects;
    }

    public static Media asMedia(ClientResponse clientResponse) {
        Media media;
        if (clientResponse.getStatusInfo().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
            return null;
        }

        try {
            media = mapper.readValue(clientResponse.getEntityInputStream(), Media.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return media;
    }

    public static List<Media> asMediaList(ClientResponse clientResponse) {
        List<Media> medias;
        try {
            medias = mapper.readValue(clientResponse.getEntityInputStream(), mapper.getTypeFactory().constructCollectionType(List.class, Media.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return medias;
    }

    public static Account asAccount(ClientResponse clientResponse) {
        Account account;
        ObjectMapper mapper = new ObjectMapper();
        try {
            account = mapper.readValue(clientResponse.getEntityInputStream(), Account.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return account;
    }
}
