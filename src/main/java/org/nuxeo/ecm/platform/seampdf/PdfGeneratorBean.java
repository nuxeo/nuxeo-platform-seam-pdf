package org.nuxeo.ecm.platform.seampdf;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.runtime.api.Framework;

@Name("pdfGeneratorHelper")
@Scope(ScopeType.EVENT)
public class PdfGeneratorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @RequestParameter
    protected String docRef;

    @RequestParameter
    protected String repository;

    protected CoreSession session;

    protected DocumentModel doc;

    protected void fetchDoc() throws Exception {

        RepositoryManager rm = Framework.getService(RepositoryManager.class);

        if (repository==null) {
            session = rm.getDefaultRepository().open();
        } else {
            session = rm.getRepository(repository).open();
        }

        doc = session.getDocument(new IdRef(docRef));

        //return "simple_pdf";
    }


    public void cleanup() {
        if (session!=null) {
            CoreInstance.getInstance().close(session);
            session=null;
        }
    }

    public DocumentModel getDoc() throws Exception {
        if (doc==null) {
            fetchDoc();
        }
        return doc;
    }

    public String getEndDocGeneration() {
        cleanup();
        return "";
    }

}
