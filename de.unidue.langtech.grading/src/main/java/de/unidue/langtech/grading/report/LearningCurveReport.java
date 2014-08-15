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

import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.CORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.FMEASURE;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.INCORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PCT_CORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PCT_INCORRECT;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PCT_UNCLASSIFIED;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.PRECISION;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.RECALL;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.WGT_FMEASURE;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.WGT_PRECISION;
import static de.tudarmstadt.ukp.dkpro.tc.core.util.ReportConstants.WGT_RECALL;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.FlexTable;
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

        for (Integer trainPercent : LearningCurveTask.TRAIN_PERCENTAGES) {
	        Properties props = new Properties();
	
	        File evaluationFile = new File(storage.getAbsolutePath() + "/"
	                + TestTask.EVALUATION_DATA_FILENAME + "_" + trainPercent);
	        
	        // this might happen as we skip some percentages if the number of training instances is too low
	        if (!evaluationFile.exists()) {
	        	continue;
	        }
	
	        weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
	                .read(evaluationFile.getAbsolutePath());
	        
//	        System.out.println(eval.toMatrixString());
	        
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
	        
	        results.put(KappaReport.KAPPA, QuadraticWeightedKappa.getKappa(goldLabelsList, predictedLabelsList, classLabelsInteger.toArray(new Integer[0])));
	
	        for (String s : results.keySet()) {
	            System.out.println(trainPercent + "\t" + results.get(s));
	            props.setProperty(s, results.get(s).toString());
	        }
	        
	        // Write out properties
	        getContext().storeBinary(TestTask.RESULTS_FILENAME + "_" + trainPercent, new PropertiesAdapter(props));
        }
    }
}