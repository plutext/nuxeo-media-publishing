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

package org.nuxeo.ecm.media.publishing.wistia.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    protected int id;

    protected String name;

    protected String description;

    protected int mediaCount;

    @JsonProperty("created")
    protected Date createdAt;

    @JsonProperty("updated")
    protected Date updatedAt;

    protected String hashedId;

    protected boolean anonymousCanUpload;

    protected boolean anonymousCanDownload;

    protected boolean isPublic;

    protected String publicId;

    protected List<Media> medias = new ArrayList<Media>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHashedId() {
        return hashedId;
    }

    public void setHashedId(String hashedId) {
        this.hashedId = hashedId;
    }

    public boolean getAnonymousCanUpload() {
        return anonymousCanUpload;
    }

    public void setAnonymousCanUpload(boolean anonymousCanUpload) {
        this.anonymousCanUpload = anonymousCanUpload;
    }

    public boolean getAnonymousCanDownload() {
        return anonymousCanDownload;
    }

    public void setAnonymousCanDownload(boolean anonymousCanDownload) {
        this.anonymousCanDownload = anonymousCanDownload;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void addMedia(Media media) {
        this.medias.add(media);
    }

    public List<Media> getMedias() {
        return this.medias;
    }

    @Override
    public String toString() {
        return "--- Project info ---" +
                "\nid: \t" + getId() +
                "\nname: \t" + getName() +
                "\ndescription: \t" + getDescription() +
                "\nmediaCount: \t" + getMediaCount() +
                "\nhashedId: \t" + getHashedId() +
                "\nanonymousCanUpload: \t" + getAnonymousCanUpload() +
                "\nanonymousCanDownload: \t" + getAnonymousCanDownload() +
                "\nisPublic: \t" + isPublic() + "\n";
    }
}
