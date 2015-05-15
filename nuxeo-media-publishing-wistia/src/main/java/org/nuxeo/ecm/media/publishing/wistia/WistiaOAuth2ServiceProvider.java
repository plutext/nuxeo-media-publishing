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
package org.nuxeo.ecm.media.publishing.wistia;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import org.nuxeo.ecm.platform.oauth2.providers.AbstractOAuth2UserEmailProvider;

import java.io.IOException;

/**
 * @since 7.3
 */
public class WistiaOAuth2ServiceProvider extends AbstractOAuth2UserEmailProvider {

    private static final String ACCOUNT_INFO_URL = "https://api.wistia.com/v1/account.json";

    private static final HttpRequestFactory requestFactory =
        HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            }
        });

    @Override
    protected String getUserEmail(String accessToken) throws IOException {
        GenericUrl url = new GenericUrl(ACCOUNT_INFO_URL);
        url.set("api_password", accessToken);

        HttpResponse response = requestFactory.buildGetRequest(url).execute();
        GenericJson json = response.parseAs(GenericJson.class);
        return (String) json.get("name");
    }
}
