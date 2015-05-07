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

package org.nuxeo.ecm.social.publishing.wistia.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Stats {

    protected int averagePercentWatched;

    protected int pageLoads;

    protected int percentOfVisitorsClickingPlay;

    protected int plays;

    protected int visitors;

    public int getAveragePercentWatched() {
        return averagePercentWatched;
    }

    public void setAveragePercentWatched(int averagePercentWatched) {
        this.averagePercentWatched = averagePercentWatched;
    }

    public int getPageLoads() {
        return pageLoads;
    }

    public void setPageLoads(int pageLoads) {
        this.pageLoads = pageLoads;
    }

    public int getPercentOfVisitorsClickingPlay() {
        return percentOfVisitorsClickingPlay;
    }

    public void setPercentOfVisitorsClickingPlay(int percentOfVisitorsClickingPlay) {
        this.percentOfVisitorsClickingPlay = percentOfVisitorsClickingPlay;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

    public int getVisitors() {
        return visitors;
    }

    public void setVisitors(int visitors) {
        this.visitors = visitors;
    }
}
