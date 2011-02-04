/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import com.jcraft.jzlib.JZlib;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;

import org.ads.solr.InvenioBitSet;
import com.jcraft.jzlib.ZInputStream;
import java.util.zip.DeflaterOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterInputStream;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jluker
 */
public class InvenioQueryComponent extends QueryComponent {

   public static final Logger log = LoggerFactory.getLogger(SolrResourceLoader.class);

  /**
   * Actually run the query
   */
  @Override
  public void process(ResponseBuilder rb) throws IOException
  {
    SolrQueryRequest req = rb.req;
    SolrQueryResponse rsp = rb.rsp;
    SolrIndexSearcher searcher = req.getSearcher();
    IndexReader reader = searcher.getReader();

    SolrParams params = req.getParams();
    ModifiableSolrParams modParams = new ModifiableSolrParams(params);
    modParams.set("wt", "bitset_stream");
    req.setParams(modParams);

    log.info("wt: " + req.getParams().get("wt"));

    InvenioIdCollector collector = new InvenioIdCollector();

    SolrIndexSearcher.QueryCommand cmd = rb.getQueryCommand();
    Query query = cmd.getQuery();

    searcher.search(query, collector);

    log.info("Fetchting bitset from collector");
//    InvenioBitSet bitset = collector.getBitSet();

//    rsp.add("bitset", bitset);
    rsp.add("ids", collector.getIds());
    log.info("bitset stream added to response");

   }
}
