package org.ads.solr.test;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrDocument;
import java.util.Map;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrDocument;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.impl.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.util.NamedList;

class InvenioSolrJTest
{
    public void query(String q)
    {
        CommonsHttpSolrServer server = null;

        try
        {
            server = new CommonsHttpSolrServer("http://localhost:8983/solr/");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        SolrQuery query = new SolrQuery();
        query.setQuery("galaxy"); //q);
        query.setQueryType("invenio_ids");

        try
        {
            QueryResponse qr = server.query(query);
            NamedList<Object> resp = qr.getResponse();


            System.out.println("Size: " + resp.size());
//            System.out.println("Found: " + sdl.getNumFound());
//            System.out.println("Start: " + sdl.getStart());
//            System.out.println("Ids Returned: " + sdl.size());
//            System.out.println("Max Score: " + sdl.getMaxScore());
            System.out.println("Query time: " + qr.getQTime());
            System.out.println("Elapsed time: " + qr.getElapsedTime());
            System.out.println("--------------------------------");
            System.out.println(resp.toString());
            System.out.println("--------------------------------");

        }
        catch (SolrServerException e)
        {
            e.printStackTrace();
        }

    }

    public static void main(String[] args)
    {
        InvenioSolrJTest solrj = new InvenioSolrJTest();
        solrj.query(StringUtils.join(args));
    }
}
