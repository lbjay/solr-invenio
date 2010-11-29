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


    @Override
    public void prepare(ResponseBuilder rb) throws IOException {

        Logger log = LoggerFactory.getLogger(QueryComponent.class);
        log.info(COMPONENT_NAME);

        SolrQueryRequest req = rb.req;
        SolrParams params = req.getParams();

        log.info("building idMap");

        SolrIndexSearcher searcher = req.getSearcher();
        IndexReader reader = searcher.getIndexReader();
        SolrCache<String, Object> docIdMapCache = searcher.getCache("InvenioDocIdMapCache");

        String cacheKey = reader.hashCode();
        log.info("Using cacheKey: " + cacheKey);

        HashMap<String, Integer> idMap = docIdMapCache.get(cacheKey);

        if (idMap == null) {
            log.info("idMap not found in cache; generating");
            String[] ids = FieldCache.DEFAULT.getStrings(reader, "id");
            idMap = new HashMap<String, Integer>();
            for (int i = 0; i < ids.length; i++) {
                idMap.put(ids[i], i);
            }
            docIdMapCache.put(cacheKey, idMap);
        } else {
            log.info("idMap retrieved from cache");
        }

        // let the main class do it's thing to set up the basic query
        super.prepare(rb);
    }

    private HashMap<String, Integer> getIdMap(IndexReader reader) {
        String cacheKey = reader.hashCode();
        log.info("Using cacheKey: " + cacheKey);

        HashMap<String, Integer> idMap = docIdMapCache.get(cacheKey);

        if (idMap == null) {
            log.info("idMap not found in cache; generating");
            String[] ids = FieldCache.DEFAULT.getStrings(reader, "id");
            idMap = new HashMap<String, Integer>();
            for (int i = 0; i < ids.length; i++) {
                idMap.put(ids[i], i);
            }
            docIdMapCache.put(cacheKey, idMap);
        } else {
            log.info("idMap retrieved from cache");
        }
        log.info("idMap done");
        return idMap;
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {

        Logger log = LoggerFactory.getLogger(QueryComponent.class);
        log.info(COMPONENT_NAME);

        SolrQueryRequest req = rb.req;
        SolrQueryResponse rsp = rb.rsp;
        SolrParams params = req.getParams();

        IndexReader reader = req.getSearcher().getIndexReader();
        HashMap<String, Integer> idMap = this.getIdMap(reader);

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
            int lucene_id = idMap.get(Integer.parseString(rint));
            log.info("lucene_id: " + lucene_id);
            docSetFilter.addUnique(lucene_id);
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
