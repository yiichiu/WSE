package edu.nyu.cs.cs2580;

import java.util.*;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2 based on a refactoring of your favorite
 * Ranker (except RankerPhrase) from HW1. The new Ranker should no longer rely
 * on the instructors' {@link IndexerFullScan}, instead it should use one of
 * your more efficient implementations.
 */
public class RankerFavorite extends Ranker {

  public RankerFavorite(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
    Vector < ScoredDocument > retrieval_results = new Vector < ScoredDocument > ();
    //retrieve relavant docs
    Document nextDoc = _indexer.nextDoc(query, -1);// not sure about api
    while(nextDoc != null){
      retrieval_results.add(runquery(query, nextDoc));
      nextDoc = _indexer.nextDoc(query, nextDoc._docid);
    }
    Collections.sort(retrieval_results);
    return retrieval_results;  
  }

  public ScoredDocument runquery(Query query, Document doc){
    Vector<Double> lmprob = new Vector<Double>();
    createLmprob(doc, query, 0.5, lmprob);
    double score = language_model_score(lmprob);
    return new ScoredDocument(doc, score);
  }

  private void createLmprob(Document d, Query query,
   double lamb, Vector<Double> lmprob) {
    DocumentIndexed doc = (DocumentIndexed) d;
    int length = doc.getLength();
    //HashMap< Integer, Vector<Integer>> body = doc._body;
    // Build query vector, it should support phrase query.
    //Vector < Integer > bv = doc.getBodyTokens();
    Vector < String > qv = query._tokens;
    HashMap <Integer, Integer> qmap = countFrequency(qv);
    for(int index: qmap.keySet()){
      double score = doc.getTermFrequency(index);
      // Add query words to language model probability map.
      score /= length;
      // Smoothing.
      long tf = DocumentIndexed.getTermFrequency(s);
      long totalTf = IndexerInvertedDoconly.corpusTermFrequency();
      score = lamb * score + (1 - lamb) * ( tf / totalTf );
      lmprob.add(score);
    }
  }
  private HashMap<Integer, Integer> countFrequency(Vector<String> vs){
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
    for(String s: vs){
      int index = IndexerInvertedDoconly.getIndexByString(s);
      if(!map.conatinsKey(index)){
        map.put(index, 0);
      }
      map.put(index, map.get(index)++);
    }
  }
  private double language_model_score(Vector < Double > lmprob) {
    double lm_score = 0.;
    for (Double score : lmprob) { lm_score += Math.log(score); }
    return lm_score;
  }
}
