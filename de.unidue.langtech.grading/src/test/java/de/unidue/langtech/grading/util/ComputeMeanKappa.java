package de.unidue.langtech.grading.util;

import java.util.List;

public class ComputeMeanKappa {

	public static void main(String[] args) {
		Double[] kappas = new Double[] {
				0.793,
				0.584,
				0.613,
				0.640
		};
		
		System.out.println(QuadraticWeightedKappa.getMeanKappa(kappas));

	}
}
