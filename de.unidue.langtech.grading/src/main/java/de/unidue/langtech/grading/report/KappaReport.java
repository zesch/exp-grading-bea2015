package de.unidue.langtech.grading.report;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import weka.core.SerializationHelper;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.unidue.langtech.grading.util.QuadraticWeightedKappa;

public class KappaReport
    extends ReportBase
{
    
    public static final String KAPPA = "kappa";
    public static final String KAPPA_FILE_NAME = "kappa.txt";

    // holds overall CV results
    Map<String, Double> results = new HashMap<String, Double>();

    @Override
    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(TestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);

        Properties props = new Properties();

        File evaluationFile = new File(storage.getAbsolutePath() + "/"
                + TestTask.EVALUATION_DATA_FILENAME);

        weka.classifiers.Evaluation eval = (weka.classifiers.Evaluation) SerializationHelper
                .read(evaluationFile.getAbsolutePath());
        
        System.out.println(eval.toMatrixString());
        
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
        
        results.put(KAPPA, QuadraticWeightedKappa.getKappa(goldLabelsList, predictedLabelsList, classLabelsInteger.toArray(new Integer[0])));

        for (String s : results.keySet()) {
            System.out.println(s + ": " + results.get(s));
            props.setProperty(s, results.get(s).toString());
        }
        
        // Write out properties
        getContext().storeBinary(KAPPA_FILE_NAME, new PropertiesAdapter(props));
    }
}