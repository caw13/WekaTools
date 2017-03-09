package edu.ccsu.weka.tools;

import java.util.PrimitiveIterator;
import java.util.Random;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Simple class for creating dummy test data
 *
 * @author Chad Williams
 */
public class DummyDataCreator {
  
  private final static Random RANDOM = new Random();

  /**
   * Creates a dummy user instance that has ratings for a portion of the
   * items in the passed in instances.  Assumes 1st attribute is user id so ignores it.
   * @param instances
   * @param percentToRate Percentage of items to rate 1-5, .2 would represent 20%
   * @return A dummy user instance with ratings
   */
  public static Instance createDummyCurrentUser(Instances instances, double percentToRate) {
    Instance dummyUserInstance = new DenseInstance(instances.numAttributes());
    dummyUserInstance.setDataset(instances);
    int numRatingsGiven = 0;
    
    PrimitiveIterator.OfInt attributeIdStream = RANDOM.ints(1, instances.numAttributes() - 1).iterator();
    PrimitiveIterator.OfInt ratingStream = RANDOM.ints(1, 6).iterator();
    int numToRate = (int)((instances.numAttributes()-1) * percentToRate);
    while (numRatingsGiven < numToRate) {
      int attributeId = attributeIdStream.nextInt();
      int rating = ratingStream.nextInt();
      dummyUserInstance.setValue(attributeId, rating);
      numRatingsGiven++;
    }
    return dummyUserInstance;
  }
}
