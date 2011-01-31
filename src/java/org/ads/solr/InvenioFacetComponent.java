/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ads.solr;

import java.io.IOException;
import java.io.InputStream;
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
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.SolrCache;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.common.util.ContentStream;
import com.jcraft.jzlib.*;
import java.io.ByteArrayOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.util.OpenBitSet;

/**
 *
 * @author jluker
 */
public class InvenioFacetComponent extends QueryComponent {

    private HashMap<Integer, Integer> getIdMap(SolrIndexSearcher searcher) {

        Logger log = LoggerFactory.getLogger(QueryComponent.class);
        IndexReader reader = searcher.getReader();

        int cacheKey = reader.hashCode();
        log.info("Using cacheKey: " + cacheKey);

        SolrCache<Integer, Object> docIdMapCache = searcher.getCache("InvenioDocIdMapCache");
        HashMap<Integer, Integer> idMap = (HashMap<Integer, Integer>)docIdMapCache.get(cacheKey);

        if (idMap == null) {
            log.info("idMap not found in cache; generating");
            idMap = new HashMap<Integer, Integer>();
            try {
                int[] ids = FieldCache.DEFAULT.getInts(reader, "id");
                log.info("ids length: " + ids.length);
                log.info("ids[3455]: " + ids[3455]);
                for (int i = 0; i < ids.length; i++) {
                    idMap.put(ids[i], i);
                }
            } catch (IOException e) {
                SolrException.logOnce(SolrCore.log, "Exception during idMap init", e);
            }
            docIdMapCache.put(cacheKey, idMap);
        } else {
            log.info("idMap retrieved from cache");
        }
        log.info("idMap done. size: " + idMap.size());
        return idMap;
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {

        Logger log = LoggerFactory.getLogger(QueryComponent.class);
        log.info(COMPONENT_NAME);

        SolrQueryRequest req = rb.req;
        SolrQueryResponse rsp = rb.rsp;
        SolrParams params = req.getParams();
        Iterable<ContentStream> streams = req.getContentStreams();

        if (streams == null) {
//          TODO: throw IOException("No streams found! Where's my bitset?");
            log.error("No streams found!");
        }


        SolrIndexSearcher searcher = req.getSearcher();
        HashMap<Integer, Integer> idMap = getIdMap(searcher);
        InvenioBitSet bitset = null;
        BitDocSet docSetFilter = new BitDocSet();

        for (ContentStream stream : streams) {
            log.info("Got stream: " + stream.getName() +
                ", Content type: " + stream.getContentType() +
                ", stream info: " + stream.getSourceInfo());

            if (stream.getName().equals("bitset")) {
                InputStream is = stream.getStream();
                // use zlib to read in the data
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                ZInputStream zIn = new ZInputStream(is);
                byte[] buf = new byte[1024];
                while ((zIn.read(buf, 0, 1024)) != -1) {
                    bOut.write(buf);
                }

                bitset = new InvenioBitSet(bOut.toByteArray());
                log.info("bitset query: " + bitset.toString());

                int i = 0;
                while (bitset.nextSetBit(i) != -1) {
                    int nextBit = bitset.nextSetBit(i);
                    int lucene_id = idMap.get(nextBit);
                    docSetFilter.add(lucene_id);
                    i = nextBit + 1;
                }
                log.info("docSetFilter size: " + docSetFilter.size());
            }
        }

        long timeAllowed = (long)params.getInt( CommonParams.TIME_ALLOWED, -1 );
        SolrIndexSearcher.QueryCommand cmd = rb.getQueryCommand();

        // use our set of doc ids as a filter
        cmd.setFilter(docSetFilter);
        cmd.setTimeAllowed(timeAllowed);

        SolrIndexSearcher.QueryResult result = new SolrIndexSearcher.QueryResult();
        searcher.search(result,cmd);
        rb.setResult( result );

        rsp.add("response",rb.getResults().docList);
        rsp.getToLog().add("hits", rb.getResults().docList.matches());

        doFieldSortValues(rb, searcher);
        doPrefetch(rb);
    }
}
