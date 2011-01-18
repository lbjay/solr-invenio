/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import org.apache.lucene.search.Similarity;
import org.apache.solr.schema.SimilarityFactory;
import org.apache.lucene.misc.SweetSpotSimilarity;

/**
 *
 * @author jluker
 */
public class CustomSimilarityFactory extends SimilarityFactory {

    @Override
    public Similarity getSimilarity() {
        SweetSpotSimilarity sim = new SweetSpotSimilarity();
        sim.setLengthNormFactors("body", 500, 10000, (float) 0.5, true);
        sim.setLengthNormFactors("body_syn", 500, 20000, (float) 0.5, true);
        return sim;
    }

}
