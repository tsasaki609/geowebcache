/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Arne Kepp, The Open Planning Project, Copyright 2008
 * 
 */
package org.geowebcache.service.wms;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.layer.wms.WMSLayer;

public class WMSRequests {
    private static Log log = LogFactory.getLog(org.geowebcache.service.wms.WMSRequests.class);
        
    public static void handleProxy(TileLayerDispatcher tld, ConveyorTile tile) throws GeoWebCacheException {

        WMSLayer layer = null;
        TileLayer tl = tld.getTileLayer(tile.getLayerId());

        if(tl == null) {
            throw new GeoWebCacheException(tile.getLayerId() + " is unknown.");
        }
        
        if (tl instanceof WMSLayer) {
            layer = (WMSLayer) tl;
        } else {
            throw new GeoWebCacheException(tile.getLayerId()
                    + " is not served by a WMS backend.");
        }

        String queryStr = tile.servletReq.getQueryString();
        String serverStr = layer.getWMSurl()[0];

        try {
            URL url;
            if (serverStr.endsWith("?")) {
                url = new URL(serverStr + queryStr);
            } else {
                url = new URL(serverStr + "?" + queryStr);
            }

            HttpURLConnection wmsBackendCon = (HttpURLConnection) url
                    .openConnection();
            HttpServletResponse response = tile.servletResp;

            if (wmsBackendCon.getContentEncoding() != null) {
                response.setCharacterEncoding(wmsBackendCon
                        .getContentEncoding());
            }

            response.setContentType(wmsBackendCon.getContentType());

            int read = 0;
            byte[] data = new byte[1024];
            while (read > -1) {
                read = wmsBackendCon.getInputStream().read(data);
                if (read > -1) {
                    response.getOutputStream().write(data, 0, read);
                }
            }
        } catch (IOException ioe) {
            tile.servletResp.setStatus(500);
            log.error(ioe.getMessage());
        }
    }
}
