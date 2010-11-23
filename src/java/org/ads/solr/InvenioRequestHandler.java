package org.ads.solr;

import java.util.*;
import org.apache.lucene.search.Query;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvenioRequestHandler extends RequestHandlerBase {

	public static final Logger log = LoggerFactory.getLogger(SolrResourceLoader.class);

//	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public String getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
    public void handleRequestBody( SolrQueryRequest req, SolrQueryResponse rsp ) throws Exception {

	    SolrParams params = req.getParams();
	    SolrIndexSearcher searcher = req.getSearcher();
	    IndexReader reader = searcher.getReader();

	    String q = params.get( CommonParams.Q );
	    log.info(q);

        Query query = QueryParsing.parseQuery(q, params.get(CommonParams.DF),
            params, req.getSchema());

        InvenioIdCollector collector = new InvenioIdCollector();
        collector.initIdMap(reader);

        searcher.search(query, collector);

//        Map<String, Float> docIds = collector.getDocs();
        ArrayList<Integer> docIds = collector.getDocs();
        log.info("docId count: " + docIds.size());

        BitSet bs = collector.getBitSet();
        log.info("bitset: " + bs.size());

        rsp.add("bitset", bs);
		rsp.add("docs", docIds);
        rsp.add("numFound", docIds.size());
        rsp.add("start", 0);
	}
}
