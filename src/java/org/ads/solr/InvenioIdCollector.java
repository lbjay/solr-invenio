package org.ads.solr;

import java.util.*;
import java.util.BitSet;

import java.io.IOException;

import org.apache.lucene.document.LoadFirstFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvenioIdCollector extends Collector {

	public static final Logger log = LoggerFactory.getLogger(SolrResourceLoader.class);

//    private Map<String, Float> docs = new HashMap<String, Float>();
    private ArrayList<Integer> docs = new ArrayList<Integer>();
    private BitSet bs = new BitSet(9000000);
    private Scorer scorer;
    private IndexReader reader;
    private int docBase;
    private String[] idMap;

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int relativeId) throws IOException {
//		int luceneId = relativeId + this.docBase;
//		float score = this.scorer.score();
//		String idFromIdMap = this.idMap[luceneId];
//		this.docs.put(this.idMap[relativeId + this.docBase], this.scorer.score());
        this.docs.add(Integer.parseInt(this.idMap[relativeId + this.docBase]));
    	this.bs.set(Integer.parseInt(this.idMap[relativeId + this.docBase]), true);
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		this.reader = reader;
		this.docBase = docBase;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	public void initIdMap(IndexReader reader) {
		log.info("initializing idMap");
		try {
			this.idMap = FieldCache.DEFAULT.getStrings(reader, "id");
		}
		catch (IOException e) {
			SolrException.logOnce(SolrCore.log, "Exception during idMap init", e);
		}
	}

//	public Map<String, Float> getDocs() {
    public ArrayList<Integer> getDocs() {
		return this.docs;
	}

    public BitSet getBitSet() {
	    return this.bs;
    }
}
