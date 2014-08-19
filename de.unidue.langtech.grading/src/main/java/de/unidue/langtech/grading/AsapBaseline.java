package de.unidue.langtech.grading;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.FeatureValuesReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;
import de.unidue.langtech.grading.io.Asap2Reader;
import de.unidue.langtech.grading.report.KappaReport;
import de.unidue.langtech.grading.report.LearningCurveReport;
import de.unidue.langtech.grading.tc.BatchTaskLearningCurve;

public class AsapBaseline
    extends ExperimentsBase
{

    public static final String TRAIN_DATA_ALL        = "classpath:/asap/train.tsv";
    public static final String TRAIN_DATA_CONSISTENT = "classpath:/asap/train_consistent_items.tsv";
    public static final String TEST_DATA             = "classpath:/asap/test_public.txt";


    public static void main(String[] args)
        throws Exception
    {
        for (int essaySetId : Asap2Reader.essaySetIds) {
	        ParameterSpace pSpace = getParameterSpace(essaySetId, TRAIN_DATA_ALL, TEST_DATA);
//	        ParameterSpace pSpace = getParameterSpace(essaySetId, TRAIN_DATA_CONSISTENT, TEST_DATA);

	        AsapBaseline experiment = new AsapBaseline();
//	        experiment.runCrossValidation(pSpace);
//	        experiment.runTrainTest(pSpace);
	        experiment.runLearningCurve(pSpace);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(int essaySetId, String trainFile, String testFile) 
    		throws IOException
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, Asap2Reader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(
                        Asap2Reader.PARAM_INPUT_FILE, trainFile,
                        Asap2Reader.PARAM_ESSAY_SET_ID, essaySetId));
        dimReaders.put(DIM_READER_TEST, Asap2Reader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(
                        Asap2Reader.PARAM_INPUT_FILE, testFile,
                        Asap2Reader.PARAM_ESSAY_SET_ID, essaySetId));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                getPipelineParameterDim(),
                getFeatureSetsDim(),
                getClassificationArgsDim()
//                Dimension.createBundle("featureSelection", getFeatureSelectionDim())
        );

        return pSpace;
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation("ASAP-CV",
                getPreprocessing(), NUM_FOLDS);
        // adds a report to TestTask which creates a report about average feature values for
        // each outcome label
        batch.addInnerReport(FeatureValuesReport.class);
        // computes and stores the kappa values
        batch.addInnerReport(KappaReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchCrossValidationReport.class);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }

    // ##### TRAIN-TEST #####
    protected void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskTrainTest batch = new BatchTaskTrainTest("ASAP-TrainTest",
                getPreprocessing());
        // adds a report to TestTask which creates a report about average feature values for
        // each outcome label
        batch.addInnerReport(FeatureValuesReport.class);
        // computes and stores the kappa values
        batch.addInnerReport(KappaReport.class);    
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }
    
    // ##### LEARNING-CURVE #####
    protected void runLearningCurve(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskLearningCurve batch = new BatchTaskLearningCurve("ASAP-LearningCurve",
                getPreprocessing());
        // computes and stores the kappa values
        batch.addInnerReport(LearningCurveReport.class);    
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }
}