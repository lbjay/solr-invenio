/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;

/**
 *
 * @author jluker
 */
public class InvenioQueryComponent extends QueryComponent {

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

    InvenioIdCollector collector = new InvenioIdCollector();

    SolrIndexSearcher.QueryCommand cmd = rb.getQueryCommand();
    Query query = cmd.getQuery();

    searcher.search(query, collector);

    rsp.add("response", collector.getBitSet());
   }
}
