/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryResponse;

/**
 *
 * @author jluker
 */
public class BitSetScoreCollector extends BitSetFieldCollector {

    private Scorer scorer;
    private IndexReader reader;
    private int docBase;
    private String fieldName = "score";
    private String responseFieldName = "score";

    private InvenioBitSet bitset;
    private int[] valueMap;

    @Override
    public void addValuesToResponse(SolrQueryResponse rsp) {
        rsp.add(this.getResponseFieldName(), this.bitset);
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public String getResponseFieldName() {
	return this.responseFieldName;
    }

    @Override
    public void setResponseFieldName(String responseFieldName) {
	this.responseFieldName = responseFieldName;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
	this.scorer = scorer;
    }

    @Override
    public void collect(int doc) throws IOException {
	float score = this.scorer.score();
	this.bitset.set((int) (score * 10000));
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
	this.reader = reader;
	this.docBase = docBase;
	log.info("initializing idMap");
        try {
            this.valueMap = FieldCache.DEFAULT.getInts(reader, this.fieldName);
            log.info("idMap length: " + this.valueMap.length);
	}
	catch (IOException e) {
            SolrException.logOnce(SolrCore.log, "Exception during idMap init", e);
	}
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
	return true;
    }

}
