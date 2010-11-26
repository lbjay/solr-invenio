/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import java.io.IOException;
import java.util.HashMap;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import java.util.Random;
import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.SolrIndexSearcher;

/**
 *
 * @author jluker
 */
public class InvenioFacetComponent extends QueryComponent {

    public static final String COMPONENT_NAME = "invenio_facets";

    public String[] docIds;

    private HashMap<String, Integer> idMap = new HashMap<String, Integer>();

    public void prepare(ResponseBuilder rb) throws IOException {

        Logger log = LoggerFactory.getLogger(QueryComponent.class);

        SolrQueryRequest req = rb.req;
        SolrParams params = req.getParams();
        if (!params.getBool(COMPONENT_NAME, true)) {
            return;
        }

        // let the main class do it's thing to set up the basic query
        log.info("calling super class");
        super.prepare(rb);
        log.info("back in the child class now");

        // assume we've been passed in some kind of invenio intbitset and
        // parsed it into a set of doc ids use a canned integer list
        // log.info("Creating canned doc id array");
        //int[] docIds = {16939, 16025, 16021, 15964, 14850, 14763, 14762, 14482, 14481, 14452, 13841, 13838, 13825, 13822, 13815, 13776, 13773, 13768, 13756, 13744, 13742, 13734, 13428, 13345, 13344, 12862, 12853, 12833, 12826, 12562, 12560, 12534, 12530, 12522, 12480, 12479, 12478, 12467, 12223, 12221, 12084, 11803, 11785, 11762, 11760, 11757, 11753, 11566, 11148, 11147, 11095, 10844, 10840, 10839, 10783, 10769, 10768, 10760, 10748, 23313, 23308, 22752, 22741, 21922, 21916, 21396, 21356, 21239, 21236, 19163, 19118, 17953, 17949, 28123, 28121, 27289, 27288, 26582, 25919, 25837, 25825, 25656, 25128, 25062, 24927, 24926, 24160, 24158, 24153, 24148, 24058, 24055, 24054, 31606, 31598, 31502, 31277, 30948, 30901, 29911, 29878, 29875, 29874, 29873, 29832, 29681, 29679, 29678, 29600, 29596, 28981, 28980, 33384, 33272, 33271, 32800, 32798, 32794, 32562, 32559, 32555, 32509, 32443, 32441, 32386, 32338};
        //ArrayList<String> ids = new ArrayList<String>();
        this.docIds = new String[] {"16939", "16025", "16021", "15964", "14850", "14763", "14762", "14482", "14481", "14452", "13841", "13838"};
        log.info("string id list: " + docIds);
//        this.docIds = new String[6000000];
//        Random rgen = new Random();
//        for (int i = 0; i < 6000000; i++) {
//            this.docIds[i] = Integer.toString(rgen.nextInt(8900000));
//        }

        log.info("building idMap");
        IndexReader reader = req.getSearcher().getReader();
        String[] ids = FieldCache.DEFAULT.getStrings(reader, "id");
        for (int i = 0; i < ids.length; i++) {
            this.idMap.put(ids[i], i);
        }
        log.info("this.idMap[47]: " + this.idMap.get("47"));
        log.info("this.idMap[105]: " + this.idMap.get("105"));
        log.info("this.idMap[10522]: " + this.idMap.get("10522"));
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {

        Logger log = LoggerFactory.getLogger(QueryComponent.class);

        SolrQueryRequest req = rb.req;
        SolrQueryResponse rsp = rb.rsp;
        SolrParams params = req.getParams();
        if (!params.getBool(COMPONENT_NAME, true)) {
            return;
        }

        long timeAllowed = (long)params.getInt( CommonParams.TIME_ALLOWED, -1 );
        SolrIndexSearcher searcher = req.getSearcher();

        SchemaField idField = req.getSchema().getUniqueKeyField();
        log.info(idField.getName());

        SolrIndexSearcher.QueryCommand cmd = rb.getQueryCommand();
        cmd.setTimeAllowed(timeAllowed);

        log.info("cmd: " + cmd);
        log.info("query: " + cmd.getQuery());
        log.info("filters: " + cmd.getFilterList());
        log.info("offset: " + cmd.getOffset());
        log.info("len: " + cmd.getLen());
        log.info("needDocSet: " + cmd.isNeedDocSet());

        // translate our doc ids into lucene ids
        int[] luceneIds = new int[this.docIds.length];
        for (int i = 0; i < this.docIds.length; i++) {
            log.info("i: " + i);
            log.info("this.docIds[i]: " + this.docIds[i]);
            luceneIds[i] = this.idMap.get(this.docIds[i]);
            log.info("luceneIds[i]: " + luceneIds[i]);
        }

        DocList docSetFilter = new DocSlice(0, luceneIds.length, luceneIds, null, luceneIds.length, 0);

        // use our set of doc ids as a filter
        cmd.setFilter(docSetFilter);

        SolrIndexSearcher.QueryResult result = new SolrIndexSearcher.QueryResult();
        searcher.search(result,cmd);
        rb.setResult( result );

        rsp.add("response",rb.getResults().docList);
        rsp.getToLog().add("hits", rb.getResults().docList.matches());

        doFieldSortValues(rb, searcher);
        doPrefetch(rb);
    }
}
