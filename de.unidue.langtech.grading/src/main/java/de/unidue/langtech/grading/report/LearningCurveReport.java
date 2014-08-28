/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.unidue.langtech.grading.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.unidue.langtech.grading.tc.LearningCurveTask;
import de.unidue.langtech.grading.util.QuadraticWeightedKappa;

/**
 * Report that computes evaluation results given the classification results.
 * Collects results for each of the learning curve runs.
 */
public class LearningCurveReport
    extends ReportBase
    implements Constants
{

    List<String> actualLabelsList = new ArrayList<String>();
    List<String> predictedLabelsList = new ArrayList<String>();
    // in ML mode, holds a map for building the Label Power Set over all label actuals/predictions
    HashMap<String, Map<String, Integer>> tempM = new HashMap<String, Map<String, Integer>>();
    // holds overall CV results
    Map<String, Double> results = new HashMap<String, Double>();
    // holds PR curve data
    List<double[][]> prcData = new ArrayList<double[][]>();

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(TestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);

        for (Integer numberOfInstances : LearningCurveTask.NUMBER_OF_TRAINING_INSTANCES) {
	        Properties props = new Properties();
	        List<Double> kappas = new ArrayList<Double>();
        	for (int iteration=0; iteration<LearningCurveTask.ITERATIONS; iteration++) {
		        File evaluationFile = new File(storage.getAbsolutePath() + "/"
		                + TestTask.EVALUATION_DATA_FILENAME + "_" + numberOfInstances + "_" + iteration);
		        
		        // we need to check non-existing files as we might skip some training sizes
		        if (!evaluationFile.exists()) {
		        	continue;
		        }
    	
    	        weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
    	                .read(evaluationFile.getAbsolutePath());
    	        
//    	        System.out.println(eval.toMatrixString());
    	        
    	        List<String> classLabels = TaskUtils.getClassLabels(eval.getHeader(), false);
    	        List<Integer> classLabelsInteger = new ArrayList<Integer>();
    	        for (String classLabel : classLabels) {
    	            classLabelsInteger.add(Integer.parseInt(classLabel));
    	        }
    	        
    	        double[][] confusionMatrix = eval.confusionMatrix();
    	
    	        List<Integer> goldLabelsList = new ArrayList<Integer>();
    	        List<Integer> predictedLabelsList = new ArrayList<Integer>();
    	        
    	        // fill rating lists from weka confusion matrix
    	        for (int c = 0; c < confusionMatrix.length; c++) {
    	            for (int r = 0; r < confusionMatrix.length; r++) {
    	                for (int i=0; i < (int) confusionMatrix[c][r]; i++) {
    	                    goldLabelsList.add(classLabelsInteger.get(c));
    	                    predictedLabelsList.add(classLabelsInteger.get(r));
    	                }
    	            }
    	        }
    	        
    	        double kappa = QuadraticWeightedKappa.getKappa(goldLabelsList, predictedLabelsList, classLabelsInteger.toArray(new Integer[0]));
    	        kappas.add(kappa);
        	}

        	double min = -1.0;
        	double max = -1.0;
        	if (kappas.size() > 0) {
        		min = Collections.min(kappas);
        		max = Collections.max(kappas);
        	}
	        double meanKappa = QuadraticWeightedKappa.getMeanKappa(kappas);
	        results.put(KappaReport.KAPPA, meanKappa);
	        System.out.println(numberOfInstances + "\t" + meanKappa + "\t" + min + "\t" + max);
	    	
	        for (String s : results.keySet()) {
	            props.setProperty(s, results.get(s).toString());
	        }
	        
	        // Write out properties
	        getContext().storeBinary(TestTask.RESULTS_FILENAME + "_" + numberOfInstances, new PropertiesAdapter(props));
        }
    }
}