package org.ads.solr;

import java.util.*;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvenioIdCollector extends Collector {

	public static final Logger log = LoggerFactory.getLogger(SolrResourceLoader.class);

//    private Map<String, Float> docs = new HashMap<String, Float>();
    private InvenioBitSet bitset = new InvenioBitSet(1000);
    private Scorer scorer;
    private IndexReader reader;
    private int docBase;
    private int[] idMap;

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int relativeId) throws IOException {
        log.info("relativeId: " + relativeId);
        log.info("this.docBase: " + this.docBase);
        log.info("idMap length: " + this.idMap.length);
    	this.bitset.set(this.idMap[relativeId]); // + this.docBase]);
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		this.reader = reader;
		this.docBase = docBase;

		log.info("initializing idMap");
		try {
            this.idMap = FieldCache.DEFAULT.getInts(this.reader, "id");
            log.info("idMap length: " + this.idMap.length);
		}
		catch (IOException e) {
			SolrException.logOnce(SolrCore.log, "Exception during idMap init", e);
		}
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

    public InvenioBitSet getBitSet() {
	    return this.bitset;
    }
}
