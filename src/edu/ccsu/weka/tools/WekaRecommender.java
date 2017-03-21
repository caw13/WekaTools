/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ccsu.weka.tools;

import java.util.Comparator;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.TreeMap;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Vote;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.neighboursearch.NearestNeighbourSearch;

/**
 *
 * @author cw1491
 */
public class WekaRecommender {

  private Instances trainingInstances;
  private Classifier currentClassifier = null;
  private boolean classifierInitialized = false;

  public WekaRecommender(Instances trainingInstances, Classifier classifier) {
    this.trainingInstances = trainingInstances;
    if (classifier == null) {
      // default to kNN
      this.currentClassifier = new IBk(20);
    } else {
      this.currentClassifier = classifier;
    }
  }

  public void setClassfier(Classifier passedClassifier) {
    currentClassifier = passedClassifier;
    classifierInitialized = false;
  }

  /**
   * Returns recommendations sorted such that first element has highest
   * predicted rating, map is key is predicted rating, value is item id.
   *
   * @param passedInstance
   * @return
   * @throws Exception
   */
  public TreeMap<Double, String> getRecommendations(Instance passedInstance) throws Exception {
    TreeMap<Double, String> recommendations = new TreeMap(Comparator.naturalOrder().reversed());
    Instances currentTrainInstances = trainingInstances;
    // If KNN only do nearest neighbor comparison once
    if (currentClassifier instanceof IBk){
      IBk knnClassifier = (IBk)currentClassifier;
      trainingInstances.setClassIndex(trainingInstances.numAttributes()-1);
      knnClassifier.buildClassifier(trainingInstances);
      NearestNeighbourSearch nnSearch = knnClassifier.getNearestNeighbourSearchAlgorithm();
      nnSearch.setInstances(trainingInstances);
      currentTrainInstances = nnSearch.kNearestNeighbours(passedInstance,knnClassifier.getKNN());
    }
    // Go through each rating
    for (int attrIndex = 1; attrIndex < passedInstance.numAttributes(); attrIndex++) {
      // Only predict rating for missing attributes
      if (passedInstance.isMissing(attrIndex)) {
        currentTrainInstances.setClassIndex(attrIndex);
        currentClassifier.buildClassifier(currentTrainInstances);
        double predictedRating = currentClassifier.classifyInstance(passedInstance);
        String movieId = currentTrainInstances.attribute(attrIndex).name();
        recommendations.put(predictedRating, movieId);
      }
    }
    return recommendations;
  }



  public static void main(String[] args) {
    try {
      String trainingFile = "data/u.data.arff";
      // Read all the instances in the file (ARFF, CSV, XRFF, ...)
      DataSource source = new DataSource(trainingFile);
      Instances instances = source.getDataSet();
      Instance dummyUser = DummyDataCreator.createDummyCurrentUser(instances,.2);

      WekaRecommender recommender = new WekaRecommender(instances, new IBk(20));
      System.out.println("*** KNN ***");
      TreeMap<Double, String> recommendations = recommender.getRecommendations(dummyUser);
      for (int i = 0; i < 5; i++) {
        Map.Entry<Double, String> recommendation = recommendations.pollFirstEntry();
        System.out.println("Predicted rating: " + recommendation.getKey() + " id: " + recommendation.getValue());
      }
      System.out.println("*** Vote ***");
      recommender.setClassfier(new Vote());
      recommendations = recommender.getRecommendations(dummyUser);
      for (int i = 0; i < 5; i++) {
        Map.Entry<Double, String> recommendation = recommendations.pollFirstEntry();
        System.out.println("Predicted rating: " + recommendation.getKey() + " id: " + recommendation.getValue());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
