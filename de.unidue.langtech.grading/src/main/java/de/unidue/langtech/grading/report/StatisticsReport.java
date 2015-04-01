package de.unidue.langtech.grading.report;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import weka.classifiers.Evaluation;
import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.BennettSAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CohenKappaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.PercentageAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.RandolphKappaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.ScottPiAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.distance.OrdinalDistanceFunction;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.unidue.langtech.grading.util.QuadraticWeightedKappa;

public class StatisticsReport extends ReportBase {

	public static final String PERCENTAGEAGREEMENT = "percentageAgreement";
	public static final String STATISTICS_FILE_NAME = "statistics.txt";
	private static final String WEIGHTEDFMESSURE = "weightedFmessure";
	private static final String WEIGHTEDPRECISION = "weightedPrecision";
	private static final String WEIGHTEDRECALL = "weightedRecall";
	private static final String FLEISSKAPPA = "fleissKappa";
	private static final String RANDOLPHSKAPPA = "randolphsKappa";
	private static final String COHENSKAPPA = "cohensKappa";
	private static final String KRIPPENDORFSALPHA = "krippendorfsAlpha";
	private static final String SCOTTSPI = "scottsPi";
	private static final String BENNETS = "bennetsS";
	private static final String ACCURACYOFMAJORCLASS = "accuracyOfMajorClass";
	private static final String MAJORCLASS = "majorClass";
	 public static final String ZESCHSKAPPA = "zeschsKappa";

	Map<String, Double> results = new HashMap<String, Double>();

	@Override
	public void execute() throws Exception {
		File storage = getContext().getStorageLocation(
				TestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);

		Properties props = new Properties();

		File evaluationFile = new File(storage.getAbsolutePath() + "/evaluation.bin");

		weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
				.read(evaluationFile.getAbsolutePath());

		CodingAnnotationStudy study = getStudy(eval);
		PercentageAgreement pa = new PercentageAgreement(study);
		FleissKappaAgreement fleissKappa = new FleissKappaAgreement(study);
		RandolphKappaAgreement randolphKappa = new RandolphKappaAgreement(study);
		CohenKappaAgreement cohenKappa = new CohenKappaAgreement(study);
		KrippendorffAlphaAgreement krippendorfAlpha = new KrippendorffAlphaAgreement(
				study, new OrdinalDistanceFunction());
		ScottPiAgreement scottPi = new ScottPiAgreement(study);
		BennettSAgreement bennetS = new BennettSAgreement(study);

		int majorClass = getMajorClass(eval);
		// TP+TN / TP+TN+FP+FN
		double accuracyOfMajorClass = (eval.numTruePositives(majorClass) + eval
				.numTrueNegatives(majorClass))
				/ (eval.numTruePositives(majorClass)
						+ eval.numTrueNegatives(majorClass)
						+ eval.numFalsePositives(majorClass) + eval
							.numFalseNegatives(majorClass));

		results.put(ZESCHSKAPPA,getQuadraticWeightedKappa(eval));
		results.put(MAJORCLASS, (double) majorClass);
		results.put(ACCURACYOFMAJORCLASS, accuracyOfMajorClass);
		results.put(WEIGHTEDFMESSURE, eval.weightedFMeasure());
		results.put(WEIGHTEDPRECISION, eval.weightedPrecision());
		results.put(WEIGHTEDRECALL, eval.weightedRecall());
		results.put(PERCENTAGEAGREEMENT, pa.calculateAgreement());
		results.put(FLEISSKAPPA, fleissKappa.calculateAgreement());
		results.put(RANDOLPHSKAPPA, randolphKappa.calculateAgreement());
		results.put(COHENSKAPPA, cohenKappa.calculateAgreement());
		results.put(KRIPPENDORFSALPHA, krippendorfAlpha
				.calculateAgreement());
		results.put(SCOTTSPI, scottPi.calculateAgreement());
		results.put(BENNETS, bennetS.calculateAgreement());

		System.out.println(eval.toMatrixString());

		for (String s : results.keySet()) {
			if (s.equals("majorClass")) {
				// translate from index to classname
				String className = TaskUtils.getClassLabels(eval.getHeader(),
						false).get(results.get(s).intValue());
				System.out.println(s + ": " + className);
				props.setProperty(s, className);
			} else {
				System.out.printf(s+": %.2f"+System.getProperty("line.separator"), results.get(s));
//				System.out.println(s + ": " + results.get(s));
				props.setProperty(s, results.get(s).toString());
			}
		}

		// Write out properties
		getContext().storeBinary(TestTask.RESULTS_FILENAME,
				new PropertiesAdapter(props));
	}

	private Double getQuadraticWeightedKappa(Evaluation eval) {
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
		return  QuadraticWeightedKappa.getKappa(goldLabelsList, predictedLabelsList, classLabelsInteger.toArray(new Integer[0]));
	}

	// find the major class
	private int getMajorClass(Evaluation eval) {
		double[][] confusionMatrix = eval.confusionMatrix();
		int tempResult = 0;
		int tempSum = 0;
		for (int i = 0; i < confusionMatrix[0].length; i++) {
			int sum = getSum(confusionMatrix[i]);
			if (sum > tempSum) {
				tempResult = i;
				tempSum = sum;
			}
		}

		return tempResult;
	}

	private int getSum(double[] ds) {
		int result = 0;
		for (double d : ds) {
			result += (int) d;
		}
		return result;
	}

	/**
	 * get a CodingAnnotationStudy from the weka evaluation
	 * 
	 * @param eval
	 * @return study
	 */
	private CodingAnnotationStudy getStudy(Evaluation eval) {
		List<String> classLabels = TaskUtils.getClassLabels(eval.getHeader(),
				false);
		CodingAnnotationStudy study = new CodingAnnotationStudy(2);
		double[][] confusionMatrix = eval.confusionMatrix();

		for (int i = 0; i < confusionMatrix[0].length; i++) {
			for (int j = 0; j < confusionMatrix[0].length; j++) {
				study.addMultipleItems((int) confusionMatrix[i][j],
						classLabels.get(i), classLabels.get(j));
			}
		}
		return study;
	}

}
