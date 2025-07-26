package org.maia.graphics2d.function;

public interface ProbabilityDensityFunction2D extends Function2D {

	double sample();

	Function2D getCumulativeDistributionFunction();

}