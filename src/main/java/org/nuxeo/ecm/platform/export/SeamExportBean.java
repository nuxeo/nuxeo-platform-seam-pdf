package org.nuxeo.ecm.platform.export;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.ScopeType.EVENT;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLBlob;
import org.nuxeo.ecm.platform.ui.web.contentview.ContentView;

@Name("seamExportActions")
@Scope(CONVERSATION)
public class SeamExportBean extends AbstractExportBean {

    private static final long serialVersionUID = 1L;

    public String getRssFeedUrlParameters() {
        return "?contentView=" + getContentViewName() + "&conversationId=" + getConversationId();
    }

    public String getRssDocUrlParameters() {
        return "?conversationId=" + getConversationId();
    }

    public List<?> getRssDocuments() throws ClientException {
        String contentViewName = getContentViewName();
        List<SortInfo> sortInfos = new ArrayList<SortInfo>();
        sortInfos.add(new SortInfo("dc:modified", true));
        ContentView cv = contentViewActions.getContentViewWithProvider(contentViewName, null, sortInfos, 0, null);
        return cv.getPageProvider().getCurrentPage();
    }

    public ContentView getContentView() throws ClientException {
        return contentViewActions.getContentViewWithProvider(getContentViewName());
    }

    @Factory(value="documentAttributes", scope=ScopeType.EVENT)
    public Map<String, String> getDocumentAttributes() throws ClientException {

        Map<String, String> attributes = new HashMap<String, String>();

        DocumentModel currentDoc = getCurrentDocument();

        for (String schema : currentDoc.getDeclaredSchemas()) {

            DocumentPart part = currentDoc.getPart(schema);
            String prefix = schema + ".";
            if (part.getSchema().getNamespace().hasPrefix()) {
                prefix="";
            }

            for (Property prop :  part.getChildren()) {
                String name = prefix + prop.getName();
                String value = getStringValue(prop);

                attributes.put(name, value);
            }
        }

        List<String> attrs = new ArrayList<String>();

        attrs.add("dc:title");
        attrs.add("dc:description");
        attrs.add("dc:modified");
        attrs.add("dc:created");

        return attributes;
    }

    protected String getStringValue(Property prop) throws ClientException {
        if (prop==null || prop.getValue()==null) {
            return "<null>";
        }

        Serializable value = prop.getValue();
        String stringValue=value.toString();
        if (value instanceof Blob) {
            SQLBlob sqlBlob = (SQLBlob) value;
            VCSBlobHelper.updatePropertiesFromSQLBlob(sqlBlob);
            stringValue = sqlBlob.getFilename() + "(" + sqlBlob.getDigest() + ")";
        } else if (value instanceof Calendar) {
            Calendar cal = (Calendar) value;
            DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            stringValue=df.format(cal.getTime());
        } else if (value instanceof String[]) {
            stringValue = "";
            for (String item : (String[]) value) {
                stringValue = item + ",";
            }
        }
        return stringValue;
    }

    @Factory(value = "generationDate", scope = EVENT)
    public Date getPdfDate() {
        return Calendar.getInstance().getTime();
    }

    @Factory(value = "currentDocumentSimpleBlobs", scope = EVENT)
    public List<SimpleBlob> getBlobs() throws ClientException {

        DocumentModel doc = getCurrentDocument();
        BlobHolder bh = doc.getAdapter(BlobHolder.class);
        List<Blob> blobs = bh.getBlobs();
        List<SimpleBlob> result = new ArrayList<SimpleBlob>();

        if (blobs==null) {
            return result;
        }

        for (Blob blob : blobs) {
            if (blob instanceof SQLBlob) {
                SQLBlob sqlBlob = (SQLBlob) blob;
                VCSBlobHelper.updatePropertiesFromSQLBlob(sqlBlob);
                SimpleBlob sBlob = new SimpleBlob();
                sBlob.setFilename(sqlBlob.getFilename());
                sBlob.setDigest(sqlBlob.getDigest());
                sBlob.setSize(sqlBlob.getLength()/1024);
                result.add(sBlob);
            }
        }
        return result;
    }

}
