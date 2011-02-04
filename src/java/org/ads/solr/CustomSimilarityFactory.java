/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import java.util.Iterator;
import org.apache.lucene.search.Similarity;
import org.apache.solr.schema.SimilarityFactory;
import org.apache.lucene.misc.SweetSpotSimilarity;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jluker
 */
public class CustomSimilarityFactory extends SimilarityFactory {

    public static final Logger log = LoggerFactory.getLogger(SolrResourceLoader.class);

    @Override
    public void init(SolrParams params) {
        super.init(params);
        log.info("similarity factory params: " + this.params);
        log.info("toNamedList: " + this.params.toNamedList());
        log.info("toNamedList['body']: " + this.params.toNamedList().get("body"));
    }

    @Override
    public Similarity getSimilarity() {
        SweetSpotSimilarity sim = new SweetSpotSimilarity();
        Iterator<String> itr = this.params.getParameterNamesIterator();
        while (itr.hasNext()) {
            // TODO: figure out how to do this
            log.info("field: " + itr.next());
        }
        // hardcoded field settings for now
        sim.setLengthNormFactors("body", 500, 10000, (float) 0.5, true);
        sim.setLengthNormFactors("body_syn", 500, 20000, (float) 0.5, true);
        return sim;
    }

}
