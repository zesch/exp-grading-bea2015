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
import de.unidue.langtech.grading.io.PowerGradingReader;
import de.unidue.langtech.grading.report.KappaReport;
import de.unidue.langtech.grading.report.LearningCurveReport;
import de.unidue.langtech.grading.tc.BatchTaskLearningCurve;

public class PowergradingBaseline
    extends ExperimentsBase
{

    public static final String TRAIN_DATA_ALL = "classpath:/powergrading/train_70.txt";
    public static final String TEST_DATA_ALL = "classpath:/powergrading/test_30.txt";

    public static void main(String[] args)
        throws Exception
    {
        for (int questionId : PowerGradingReader.questionIds) {
	        ParameterSpace pSpace = getParameterSpace(questionId, TRAIN_DATA_ALL, TEST_DATA_ALL);

	        PowergradingBaseline experiment = new PowergradingBaseline();
//	        experiment.runCrossValidation(pSpace, "PG");
//	        experiment.runTrainTest(pSpace, "PG");
	        experiment.runLearningCurve(pSpace, "PG");
        }
    }
    
    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(int questionId, String trainFile, String testFile) 
    		throws IOException
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, PowerGradingReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(
                		PowerGradingReader.PARAM_INPUT_FILE, trainFile,
                		PowerGradingReader.PARAM_QUESTION_ID, questionId));
        dimReaders.put(DIM_READER_TEST, PowerGradingReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(
                		PowerGradingReader.PARAM_INPUT_FILE, testFile,
                		PowerGradingReader.PARAM_QUESTION_ID, questionId));
 
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                getPipelineParameterDim(),
                getFeatureSetsDim(),
                getClassificationArgsDim()
//                Dimension.createBundle("featureSelection", dimFeatureSelection)
        );

        return pSpace;
    }
}