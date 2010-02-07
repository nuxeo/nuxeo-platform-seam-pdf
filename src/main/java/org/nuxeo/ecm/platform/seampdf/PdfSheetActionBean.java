package org.nuxeo.ecm.platform.seampdf;

import static org.jboss.seam.ScopeType.EVENT;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Conversation;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.storage.sql.Binary;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLBlob;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;

@Name("pdfSheetActionBean")
@Scope(EVENT)
public class PdfSheetActionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(PdfSheetActionBean.class);

    public static String VIEW_NAME = "pdfSheetView.faces";

    @In(create = true)
    private transient NavigationContext navigationContext;

    @Factory(value = "pdfDate", scope = EVENT)
    public Date getPdfDate() {
        return Calendar.getInstance().getTime();
    }

    @Factory(value = "pdfBlobs", scope = EVENT)
    public List<SimpleBlob> getBlobs() throws ClientException {

        DocumentModel doc = navigationContext.getCurrentDocument();
        BlobHolder bh = doc.getAdapter(BlobHolder.class);
        List<Blob> blobs = bh.getBlobs();
        List<SimpleBlob> result = new ArrayList<SimpleBlob>();

        for (Blob blob : blobs) {
            if (blob instanceof SQLBlob) {
                SQLBlob sqlBlob = (SQLBlob) blob;
                updatePropertiesFromSQLBlob(sqlBlob);

                SimpleBlob sBlob = new SimpleBlob();
                sBlob.setFilename(sqlBlob.getFilename());
                sBlob.setDigest(sqlBlob.getDigest());
                sBlob.setSize(sqlBlob.getLength()/1024);
                result.add(sBlob);
            }
        }

        return result;
    }

    public void doRender() throws Exception {

        String base = BaseURL.getBaseURL(getHttpServletRequest());
        String url = base + VIEW_NAME + "?conversationId=" + Conversation.instance().getId();
        HttpServletResponse response = getHttpServletResponse();
        response.resetBuffer();
        response.sendRedirect(url);
        response.flushBuffer();
        getHttpServletRequest().setAttribute(
                URLPolicyService.DISABLE_REDIRECT_REQUEST_KEY, true);
        FacesContext.getCurrentInstance().responseComplete();
    }

    protected void updatePropertiesFromSQLBlob(final SQLBlob blob) {
        Binary bin = getPrivateBinaryFromBlob(blob);
        if (bin != null) {
            updatePropertiesFromBinary(bin, blob);
        }
    }

    private Binary getPrivateBinaryFromBlob(final SQLBlob blob) {
        if (blob == null) {
            return null;
        }
        final Field[] fields = SQLBlob.class.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            if ("binary".equals(fields[i].getName())) {
                fields[i].setAccessible(true);
                try {
                    return (Binary) fields[i].get(blob);
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private void updatePropertiesFromBinary(final Binary binary, SQLBlob blob) {
        if (binary == null) {
            return;
        }
        final Field[] fields = Binary.class.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            if ("file".equals(fields[i].getName())) {
                fields[i].setAccessible(true);
                File file = null;
                try {
                    file = (File) fields[i].get(binary);
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            } else if ("digest".equals(fields[i].getName())) {
                fields[i].setAccessible(true);
                try {
                    blob.setDigest((String) fields[i].get(binary));
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }


    private static HttpServletResponse getHttpServletResponse() {
        ServletResponse response = null;
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            response = (ServletResponse) facesContext.getExternalContext()
                    .getResponse();
        }

        if (response != null && response instanceof HttpServletResponse) {
            return (HttpServletResponse) response;
        }
        return null;
    }

    private static HttpServletRequest getHttpServletRequest() {
        ServletRequest request = null;
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            request = (ServletRequest) facesContext.getExternalContext()
                    .getRequest();
        }

        if (request != null && request instanceof HttpServletRequest) {
            return (HttpServletRequest) request;
        }
        return null;
    }


    public class SimpleBlob {

        protected String filename;

        protected String digest;

        protected long size;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getDigest() {
            return digest;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

    }

}
