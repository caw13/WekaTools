package edu.ccsu.weka.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class DataPivoter {

  private SortedSet<Integer> movieIds = new TreeSet();
  private Map<String, Set<AttributeValuePair<Integer>>> userRatings = new HashMap();

  public DataPivoter() {

  }

  /**
   * Parses the passed data file that it expects to be tab delimited in the
   * format (note everything after the rating is currently ignored):
   * userId\tmovieId\trating\ttimestamp
   * @param dataFile
   * @throws Exception 
   */
  public void loadDataFile(File dataFile) throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(dataFile));
    String line = reader.readLine();
    while (line != null) {
      String[] parts = line.split("\t");
      Set<AttributeValuePair<Integer>> ratings = userRatings.get(parts[0]);
      if (ratings == null) {
        ratings = new HashSet();
        userRatings.put(parts[0], ratings);
      }
      ratings.add(new AttributeValuePair(parts[1], Integer.parseInt(parts[2])));

      movieIds.add(Integer.parseInt(parts[1]));
      line = reader.readLine();
    }
    System.out.println("Movie Ids found: " + movieIds.size());
    System.out.println("User Ids found: " + userRatings.size());
    // Create attribute array list
    ArrayList<Attribute> attributes = new ArrayList();
    Map<String,Integer> attributeNameIndexMap = new HashMap();
    Attribute userIdAttr = new Attribute("userId",true);
    attributes.add(new Attribute("userId",new ArrayList(userRatings.keySet())));
    for (Integer movieId : movieIds) {
      // Keys the attribute name to the index in the arraylist that will result 
      // in the attribute index of the Instances
      attributeNameIndexMap.put(movieId+"", attributes.size());
      attributes.add(new Attribute(movieId+"", false));

    }
    Instances instances = new Instances("MovieRatings", attributes, userRatings.size());
    for (String userId : userRatings.keySet()){
      Set<AttributeValuePair<Integer>> ratings = userRatings.get(userId);
      Instance instance = new DenseInstance(attributes.size());
      instance.setDataset(instances);
      instance.setValue(0, userId);
      for (AttributeValuePair<Integer> ratingPair : ratings){
        instance.setValue(attributeNameIndexMap.get(ratingPair.attrName), ratingPair.value);
        //System.out.println("Set attribute: "+attributeNameIndexMap.get(ratingPair.attrName)+" value:"+ratingPair.value);
      }
      instances.add(instance);
    }
    // Write ARFF file
    FileWriter writer = new FileWriter("data/u.data.arff");
    writer.write(instances.toString());
    writer.flush();
    writer.close();
  }

  public static void main(String[] args) {
    try {
      String ratingsFileName = "data/u.data";
      if (args.length > 0) {
        ratingsFileName = args[0];
      }
      DataPivoter pivoter = new DataPivoter();
      pivoter.loadDataFile(new File(ratingsFileName));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private class AttributeValuePair<T> {

    private String attrName;
    private T value;

    private AttributeValuePair(String attrName, T value) {
      this.attrName = attrName;
      this.value = value;
    }

    public boolean equals(Object o) {
      if (o instanceof AttributeValuePair) {
        AttributeValuePair passedPair = (AttributeValuePair) o;
        return this.attrName.equals(passedPair.attrName) && this.value.equals(passedPair.value);
      }
      return false;
    }
  }
}
