package org.nuxeo.ecm.platform.export;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.storage.sql.Binary;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLBlob;

public class VCSBlobHelper {

    private static Log log = LogFactory.getLog(VCSBlobHelper.class);

    public static void updatePropertiesFromSQLBlob(final SQLBlob blob) {
        Binary bin = getPrivateBinaryFromBlob(blob);
        if (bin != null) {
            updatePropertiesFromBinary(bin, blob);
        }
    }

    private static Binary getPrivateBinaryFromBlob(final SQLBlob blob) {
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

    private static void updatePropertiesFromBinary(final Binary binary, SQLBlob blob) {
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

}
