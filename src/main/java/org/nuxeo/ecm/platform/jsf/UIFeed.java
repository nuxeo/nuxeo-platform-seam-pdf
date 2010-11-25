/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.platform.jsf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.contexts.Contexts;

import yarfraw.core.datamodel.ChannelFeed;
import yarfraw.core.datamodel.YarfrawException;
import yarfraw.io.FeedWriter;

/**
 * Override for better resolution of feedFormat variable.
 * <p>
 * When this is a value expression, the original component does not decode the
 * value correctly.
 *
 * @author Anahide Tchertchian
 * @since 5.4
 */
public class UIFeed extends org.jboss.seam.rss.ui.UIFeed {

    public static final String COMPONENT_TYPE = "org.nuxeo.ecm.platform.jsf.UIFeed";

    private static final String MIMETYPE = "text/xml";

    @Override
    public void encodeEnd(FacesContext facesContext) throws IOException {
        ChannelFeed channelFeed = (ChannelFeed) Contexts.getEventContext().get(
                FEED_IMPL_KEY);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            // XXX: retrieve fee format with getter instead of using the static
            // value set.
            FeedWriter.writeChannel(getFeedFormat(), channelFeed, byteStream);
        } catch (YarfrawException e) {
            /**
             * Was IOException, but 1.5 does not have this constructor
             * http://java.sun.com/j2se/1.5.0/docs/api/java/io/IOException.html
             */
            throw new RuntimeException("Could not create feed", e);
        }
        Writer responseWriter = ((HttpServletResponse) facesContext.getExternalContext().getResponse()).getWriter();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType(MIMETYPE);
        response.setContentLength(byteStream.size());
        responseWriter.write(byteStream.toString());
        response.flushBuffer();
        facesContext.responseComplete();
    }

}
