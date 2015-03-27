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
 *      Andre Justo
 */

package org.nuxeo.ecm.social.publishing;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.admin.oauth.DirectoryBasedEditor;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.oauth2.tokens.OAuth2TokenStore;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.runtime.api.Framework;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Name("oauthUserTokens")
@Scope(ScopeType.EVENT)
public class OAuthUserTokens extends DirectoryBasedEditor implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Serializable> filter = new HashMap<String, Serializable>();

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @Override
    protected String getDirectoryName() {
        return OAuth2TokenStore.DIRECTORY_NAME;
    }

    @Override
    protected String getSchemaName() {
        return "oauth2Token";
    }

    @Override
    protected Map<String, Serializable> getQueryFilter() {
        return filter;
    }

    public DocumentModelList getProviderAccounts(String provider) {
        filter.clear();
        filter.put("serviceName", provider);
        super.refresh();
        return super.getEntries();
    }

    public DocumentModelList getCurrentUserTokens() {
        filter.clear();
        filter.put("nuxeoLogin", this.documentManager.getPrincipal().getName());
        super.refresh();
        return super.getEntries();
    }

    public String getAuthorizationFlow(String provider) {
        ServletRequest servletRequest = (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String serverURL = VirtualHostHelper.getServerURL(servletRequest, false);
        return getSocialPublishingService().getProvider(provider).getAuthorizationURL(serverURL);
    }

    private SocialPublishingService getSocialPublishingService() {
        return Framework.getService(SocialPublishingService.class);
    }
}
