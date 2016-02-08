/*
 * Twistoast - NavitiaTrafficAlert
 * Copyright (C) 2013-2016 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.android.transport.keolis;

import java.io.Serializable;

import fr.outadev.android.transport.INetwork;
import fr.outadev.android.transport.ITrafficAlert;

/**
 * Traffic alert. Used to inform the user of traffic perturbations.
 *
 * @author outadoc
 */
public class NavitiaTrafficAlert implements ITrafficAlert, Serializable {

    private int mId;
    private String mLabel;
    private String mUrl;

    /**
     * Creates a new traffic alert.
     *
     * @param id    the id of the alert
     * @param label the label (title) of the alert
     * @param url   the URL to redirect to, to get more info
     */
    public NavitiaTrafficAlert(int id, String label, String url) {
        this.mId = id;
        this.mLabel = label;
        this.mUrl = url;
    }

    @Override
    public INetwork getNetwork() {
        return null;
    }

    public int getId() {
        return mId;
    }

    @Override
    public String getTitle() {
        return mLabel;
    }

    @Override
    public void setTitle(String title) {
        mLabel = title;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(String description) {
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    public String toString() {
        return "NavitiaTrafficAlert{" +
                "id=" + mId +
                ", label='" + mLabel + '\'' +
                ", url='" + mUrl + '\'' +
                '}';
    }
}
