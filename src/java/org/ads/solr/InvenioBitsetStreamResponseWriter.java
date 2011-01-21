/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import java.io.*;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.BinaryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.SolrCore;

import com.jcraft.jzlib.*;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author jluker
 */
public class InvenioBitsetStreamResponseWriter extends BinaryResponseWriter {

    public static final Logger log = LoggerFactory.getLogger(SolrResourceLoader.class);

    @Override
    public void init(NamedList args) {
        log.info("init method called!");
    }
    @Override
    public void write(OutputStream out, SolrQueryRequest req, SolrQueryResponse rsp) {

        log.info("In the custom response writer");

        InvenioBitSet bitset = (InvenioBitSet) rsp.getValues().get("bitset");
        ZOutputStream zOut = new ZOutputStream(out, JZlib.Z_BEST_SPEED);

        log.info("bitset size: " + bitset.size());

        try {
            zOut.write(bitset.toByteArray());
            zOut.flush();
            zOut.close();
        }
        catch (IOException e) {
            SolrException.logOnce(SolrCore.log, "Exception during compression/output of bitset", e);
        }
    }
}
