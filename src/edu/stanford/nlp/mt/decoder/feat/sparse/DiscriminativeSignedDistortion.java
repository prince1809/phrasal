package edu.stanford.nlp.mt.decoder.feat.sparse;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.mt.base.ConcreteRule;
import edu.stanford.nlp.mt.base.FeatureValue;
import edu.stanford.nlp.mt.base.Featurizable;
import edu.stanford.nlp.mt.base.IString;
import edu.stanford.nlp.mt.base.InputProperty;
import edu.stanford.nlp.mt.base.Sequence;
import edu.stanford.nlp.mt.decoder.feat.DerivationFeaturizer;
import edu.stanford.nlp.mt.decoder.feat.FeaturizerState;
import edu.stanford.nlp.util.Generics;

/**
 * Signed discriminative distortion bins. (see <code>ConcreteRule</code>)
 * 
 * @author Spence Green
 *
 */
public class DiscriminativeSignedDistortion extends DerivationFeaturizer<IString, String> {

  private static final String FEATURE_NAME = "DDIST";
  
  private final boolean addDomainFeatures;
  
  public DiscriminativeSignedDistortion() {
    this.addDomainFeatures = false;
  }
  
  public DiscriminativeSignedDistortion(String...args) {
    Properties options = SparseFeatureUtils.argsToProperties(args);
    this.addDomainFeatures = options.containsKey("domainFeature");
  }
  
  @Override
  public void initialize(int sourceInputId,
      List<ConcreteRule<IString, String>> ruleList, Sequence<IString> source) {
  }

  @Override
  public List<FeatureValue<String>> featurize(Featurizable<IString, String> f) {
    int distortion = f.prior == null ? f.sourcePosition :
      f.prior.sourcePosition + f.prior.sourcePhrase.size() - f.sourcePosition;
    List<FeatureValue<String>> features = Generics.newLinkedList();
    final String genre = addDomainFeatures && f.sourceInputProperties.containsKey(InputProperty.Domain)
        ? (String) f.sourceInputProperties.get(InputProperty.Domain) : null;
    
    if (distortion < 0) {
      String featureString = FEATURE_NAME + ":neg";
      features.add(new FeatureValue<String>(featureString, 1.0));
      if (genre != null) {
        features.add(new FeatureValue<String>(featureString + "-" + genre, 1.0));
      }

    } else if (distortion > 0) {
      String featureString = FEATURE_NAME + ":pos";
      features.add(new FeatureValue<String>(featureString, 1.0));
      if (genre != null) {
        features.add(new FeatureValue<String>(featureString + "-" + genre, 1.0));
      }
    }
//    distortion = getSignedBin(distortion);
    String featureString = String.format("%s:%d", FEATURE_NAME, distortion);
    features.add(new FeatureValue<String>(featureString, 1.0));
    if (genre != null) {
      features.add(new FeatureValue<String>(featureString + "-" + genre, 1.0));
    }
    f.setState(this, new DistortionState(distortion));
    return features;
  }
  
  /**
   * Bins: 0, 1, 2, 3, 4-6, 6-10, 10+
   * 
   * @param distortion
   * @return
   */
  private static int getSignedBin(int distortion) {
    int sign = (int) Math.signum(distortion);
    int absDistortion = Math.abs(distortion);
    if (absDistortion < 4) {
      return distortion; 
    } else if (absDistortion < 7) {
      return sign * 4;
    } else if (absDistortion < 11) {
      return sign * 5;
    } else {
      return sign * 6;
    }
  }

  private static class DistortionState extends FeaturizerState {

    private final int distortion;

    public DistortionState(int distortion) {
      this.distortion = distortion;
    }
    
    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      } else if ( ! (other instanceof DistortionState)) {
        return false;
      } else {
        DistortionState o = (DistortionState) other;
        return this.distortion == o.distortion;
      }
    }

    @Override
    public int hashCode() {
      return distortion;
    }
  }
}
