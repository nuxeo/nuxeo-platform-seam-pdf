package org.nuxeo.ecm.platform.export;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.core.Manager;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.contentview.ContentView;
import org.nuxeo.ecm.webapp.contentbrowser.ContentViewActions;
import org.nuxeo.runtime.api.Framework;

public abstract class AbstractExportBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true, required = false)
    protected ContentViewActions contentViewActions;

    protected String contentViewName;

    protected static final Log log = LogFactory.getLog(AbstractExportBean.class);

    protected String getCurrentContentViewName() {
        String currentContentViewName = null;

        // fetch contentViewName from context
        if (currentContentViewName != null) {
            ContentView cv = contentViewActions.getCurrentContentView();
            if (cv != null) {
                currentContentViewName = cv.getName();
            }
        }

        // try to fetch from type
        if (currentContentViewName == null) {
            NavigationContext navigationContext = (NavigationContext) Component.getInstance("navigationContext");
            DocumentModel currentDoc = navigationContext.getCurrentDocument();
            String docType = currentDoc.getType();
            try {
                TypeManager tm = Framework.getService(TypeManager.class);
                Type dType = tm.getType(docType);
                currentContentViewName = dType.getContentViews("content")[0];
            } catch (Throwable t) {
                log.error("Unable to get ContentView from type", t);
            }
        }
        return currentContentViewName;
    }

    public String getContentViewName() {
        if (contentViewName == null || contentViewName.trim().length() == 0) {
            return getCurrentContentViewName();
        }
        return contentViewName;
    }

    public void setContentViewName(String contentViewName) {
        this.contentViewName = contentViewName;
    }

    protected String getConversationId() {
        return Manager.instance().getCurrentConversationId();
    }

    protected DocumentModel getCurrentDocument() throws ClientException {
        NavigationContext navigationContext = (NavigationContext) Component.getInstance("navigationContext");
        return navigationContext.getCurrentDocument();
    }
}
