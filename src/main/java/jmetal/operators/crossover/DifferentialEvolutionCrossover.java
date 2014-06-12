//  DifferentialEvolutionCrossover.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.operators.crossover;

import jmetal.core.Solution;
import jmetal.core.SolutionType;
import jmetal.encodings.solutiontype.ArrayRealSolutionType;
import jmetal.encodings.solutiontype.RealSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.random.PseudoRandom;
import jmetal.util.wrapper.XReal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Differential evolution crossover operators
 * Comments:
 * - The operator receives two parameters: the current individual and an array
 * of three parent individuals
 * - The best and rand variants depends on the third parent, according whether
 * it represents the current of the "best" individual or a randon one.
 * The implementation of both variants are the same, due to that the parent
 * selection is external to the crossover operator.
 * - Implemented variants:
 * - rand/1/bin (best/1/bin)
 * - rand/1/exp (best/1/exp)
 * - current-to-rand/1 (current-to-best/1)
 * - current-to-rand/1/bin (current-to-best/1/bin)
 * - current-to-rand/1/exp (current-to-best/1/exp)
 */
public class DifferentialEvolutionCrossover extends Crossover {

  private static final double DEFAULT_CR = 0.5;
  private static final double DEFAULT_F = 0.5;
  private static final double DEFAULT_K = 0.5;

  private static final String DEFAULT_DE_VARIANT = "rand/1/bin";

  /**
   * Valid solution types to apply this operator
   */
  private static final List<Class<? extends SolutionType>> VALID_TYPES =
    Arrays.asList(RealSolutionType.class,
      ArrayRealSolutionType.class);

  private double cr_;
  private double f_;
  private double k_;
  // DE variant (rand/1/bin, rand/1/exp, etc.)
  private String differentialEvolutionVariant_;

  /**
   * Constructor
   */
  @Deprecated
  public DifferentialEvolutionCrossover(HashMap<String, Object> parameters) {
    super(parameters);

    cr_ = DEFAULT_CR;
    f_ = DEFAULT_F;
    k_ = DEFAULT_K;
    differentialEvolutionVariant_ = DEFAULT_DE_VARIANT;

    if (parameters.get("CR") != null) {
      cr_ = (Double) parameters.get("CR");
    }
    if (parameters.get("F") != null) {
      f_ = (Double) parameters.get("F");
    }
    if (parameters.get("K") != null) {
      k_ = (Double) parameters.get("K");
    }
    if (parameters.get("DE_VARIANT") != null) {
      differentialEvolutionVariant_ = (String) parameters.get("DE_VARIANT");
    }
  }

  public DifferentialEvolutionCrossover(Builder builder) {
    super(new HashMap<String, Object>()) ;

    cr_ = builder.cr_ ;
    f_ = builder.f_ ;
    k_ = builder.k_ ;
    differentialEvolutionVariant_ = builder.differentialEvolutionVariant_ ;
  }

  /**
   * Executes the operation
   *
   * @param object An object containing an array of three parents
   * @return An object containing the offSprings
   */
  public Object execute(Object object) throws JMException {
    Object[] parameters = (Object[]) object;
    Solution current = (Solution) parameters[0];
    Solution[] parent = (Solution[]) parameters[1];

    Solution child;

    if (!(VALID_TYPES.contains(parent[0].getType().getClass()) &&
      VALID_TYPES.contains(parent[1].getType().getClass()) &&
      VALID_TYPES.contains(parent[2].getType().getClass()))) {

      Configuration.logger_.severe("DifferentialEvolutionCrossover.execute: " +
        " the solutions " +
        "are not of the right type. The type should be 'Real' or 'ArrayReal', but " +
        parent[0].getType() + " and " +
        parent[1].getType() + " and " +
        parent[2].getType() + " are obtained");

      Class<String> cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    }

    int jrand;

    child = new Solution(current);

    XReal xParent0 = new XReal(parent[0]);
    XReal xParent1 = new XReal(parent[1]);
    XReal xParent2 = new XReal(parent[2]);
    XReal xCurrent = new XReal(current);
    XReal xChild = new XReal(child);

    int numberOfVariables = xParent0.getNumberOfDecisionVariables();
    jrand = PseudoRandom.randInt(0, numberOfVariables - 1);

    // STEP 4. Checking the DE variant
    if (("rand/1/bin".equals(differentialEvolutionVariant_)) ||
      "best/1/bin".equals(differentialEvolutionVariant_)) {
      for (int j = 0; j < numberOfVariables; j++) {
        if (PseudoRandom.randDouble(0, 1) < cr_ || j == jrand) {
          double value;
          value = xParent2.getValue(j) + f_ * (xParent0.getValue(j) -
            xParent1.getValue(j));

          if (value < xChild.getLowerBound(j)) {
            value = xChild.getLowerBound(j);
          }
          if (value > xChild.getUpperBound(j)) {
            value = xChild.getUpperBound(j);
          }
          xChild.setValue(j, value);
        } else {
          double value;
          value = xCurrent.getValue(j);
          xChild.setValue(j, value);
        }
      }
    } else if ("rand/1/exp".equals(differentialEvolutionVariant_) ||
      "best/1/exp".equals(differentialEvolutionVariant_)) {
      for (int j = 0; j < numberOfVariables; j++) {
        if (PseudoRandom.randDouble(0, 1) < cr_ || j == jrand) {
          double value;
          value = xParent2.getValue(j) + f_ * (xParent0.getValue(j) -
            xParent1.getValue(j));

          if (value < xChild.getLowerBound(j)) {
            value = xChild.getLowerBound(j);
          }
          if (value > xChild.getUpperBound(j)) {
            value = xChild.getUpperBound(j);
          }

          xChild.setValue(j, value);
        } else {
          cr_ = 0.0;
          double value;
          value = xCurrent.getValue(j);
          xChild.setValue(j, value);
        }
      }
    } else if ("current-to-rand/1".equals(differentialEvolutionVariant_) ||
      "current-to-best/1".equals(differentialEvolutionVariant_)) {
      for (int j = 0; j < numberOfVariables; j++) {
        double value;
        value = xCurrent.getValue(j) + k_ * (xParent2.getValue(j) -
          xCurrent.getValue(j)) +
          f_ * (xParent0.getValue(j) - xParent1.getValue(j));

        if (value < xChild.getLowerBound(j)) {
          value = xChild.getLowerBound(j);
        }
        if (value > xChild.getUpperBound(j)) {
          value = xChild.getUpperBound(j);
        }

        xChild.setValue(j, value);
      }
    } else if ("current-to-rand/1/bin".equals(differentialEvolutionVariant_) ||
      "current-to-best/1/bin".equals(differentialEvolutionVariant_)) {
      for (int j = 0; j < numberOfVariables; j++) {
        if (PseudoRandom.randDouble(0, 1) < cr_ || j == jrand) {
          double value;
          value = xCurrent.getValue(j) + k_ * (xParent2.getValue(j) -
            xCurrent.getValue(j)) +
            f_ * (xParent0.getValue(j) - xParent1.getValue(j));

          if (value < xChild.getLowerBound(j)) {
            value = xChild.getLowerBound(j);
          }
          if (value > xChild.getUpperBound(j)) {
            value = xChild.getUpperBound(j);
          }

          xChild.setValue(j, value);
        } else {
          double value;
          value = xCurrent.getValue(j);
          xChild.setValue(j, value);
        }
      }
    } else if ("current-to-rand/1/exp".equals(differentialEvolutionVariant_) ||
      "current-to-best/1/exp".equals(differentialEvolutionVariant_)) {
      for (int j = 0; j < numberOfVariables; j++) {
        if (PseudoRandom.randDouble(0, 1) < cr_ || j == jrand) {
          double value;
          value = xCurrent.getValue(j) + k_ * (xParent2.getValue(j) -
            xCurrent.getValue(j)) +
            f_ * (xParent0.getValue(j) - xParent1.getValue(j));

          if (value < xChild.getLowerBound(j)) {
            value = xChild.getLowerBound(j);
          }
          if (value > xChild.getUpperBound(j)) {
            value = xChild.getUpperBound(j);
          }

          xChild.setValue(j, value);
        } else {
          cr_ = 0.0;
          double value;
          value = xCurrent.getValue(j);
          xChild.setValue(j, value);
        }
      }
    } else {
      Configuration.logger_.severe("DifferentialEvolutionCrossover.execute: " +
        " unknown DE variant (" + differentialEvolutionVariant_ + ")");
      Class<String> cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    } // else
    return child;
  }

  /**
   * Builder class
   */
  public static class Builder {
    private double cr_;
    private double f_;
    private double k_;
    // DE variant (rand/1/bin, rand/1/exp, etc.)
    private String differentialEvolutionVariant_;

    public Builder() {
      cr_ = DEFAULT_CR ;
      f_ = DEFAULT_F ;
      k_ = DEFAULT_K ;
      differentialEvolutionVariant_ = DEFAULT_DE_VARIANT ;
    }

    public Builder cr(double cr) {
      cr_ = cr ;

      return this ;
    }

    public Builder f(double f) {
      f_ = f ;

      return this ;
    }

    public Builder k(double k) {
      k_ = k ;

      return this ;
    }

    public Builder variant(String variant) {
      differentialEvolutionVariant_ = variant ;

      return this ;
    }
    public DifferentialEvolutionCrossover build() {
      return new DifferentialEvolutionCrossover(this) ;
    }
  }
}
