/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.SolrCache;
import org.apache.solr.search.SolrIndexSearcher;

/**
 *
 * @author jluker
 */
public class InvenioFacetComponent extends QueryComponent {

    public static final String COMPONENT_NAME = "invenio_facets";

    private HashMap<String, Integer> idMap;

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {

        Logger log = LoggerFactory.getLogger(QueryComponent.class);
        log.info(COMPONENT_NAME);

        SolrQueryRequest req = rb.req;
        SolrParams params = req.getParams();

        log.info("building idMap");
        this.idMap = new HashMap<String, Integer>();

        SolrIndexSearcher searcher = req.getSearcher();
        IndexReader reader = searcher.getIndexReader();
        SolrCache<String, Object> docIdMapCache = searcher.getCache("InvenioDocIdMapCache");

        String[] ids = FieldCache.DEFAULT.getStrings(reader, "id");
        for (int i = 0; i < ids.length; i++) {
            this.idMap.put(ids[i], i);
        }

        // let the main class do it's thing to set up the basic query
        super.prepare(rb);
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {

        Logger log = LoggerFactory.getLogger(QueryComponent.class);
        log.info(COMPONENT_NAME);

        SolrQueryRequest req = rb.req;
        SolrQueryResponse rsp = rb.rsp;
        SolrParams params = req.getParams();

        // assume we've been passed in some kind of invenio intbitset and
        // parsed it into a set of doc ids use a canned integer list
        //String[] docIds = new String[] {"16939", "16025", "16021", "15964", "14850", "14763", "14762", "14482", "14481", "14452", "13841", "13838"};
        //log.info("string id list: " + docIds);

        // use a randomly generated list of doc ids
        Random rgen = new Random();
        BitDocSet docSetFilter = new BitDocSet();
        for (int i = 0; i < 100; i++) {
            int rint = rgen.nextInt(8900000);
            log.info("rint: " + rint);
            docSetFilter.addUnique(rint);
        }

        // translate our doc ids into lucene ids
        /*
        int[] luceneIds = new int[docIds.length];
        for (int i = 0; i < docIds.length; i++) {
            log.info("i: " + i);
            log.info("docIds[i]: " + docIds[i]);
            luceneIds[i] = this.idMap.get(docIds[i]);
            log.info("luceneIds[i]: " + luceneIds[i]);
        }
         */

        long timeAllowed = (long)params.getInt( CommonParams.TIME_ALLOWED, -1 );
        SolrIndexSearcher.QueryCommand cmd = rb.getQueryCommand();
        //DocList docSetFilter = new DocSlice(0, luceneIds.length, luceneIds, null, luceneIds.length, 0);

        // use our set of doc ids as a filter
        cmd.setFilter(docSetFilter);
        cmd.setTimeAllowed(timeAllowed);

        SolrIndexSearcher searcher = req.getSearcher();
        SolrIndexSearcher.QueryResult result = new SolrIndexSearcher.QueryResult();
        searcher.search(result,cmd);
        rb.setResult( result );

        rsp.add("response",rb.getResults().docList);
        rsp.getToLog().add("hits", rb.getResults().docList.matches());

        doFieldSortValues(rb, searcher);
        doPrefetch(rb);
    }
}
