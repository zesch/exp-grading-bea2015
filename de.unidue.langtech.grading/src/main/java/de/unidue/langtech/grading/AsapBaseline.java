package de.unidue.langtech.grading;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;
import de.unidue.langtech.grading.io.Asap2Reader;

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
//	        experiment.runCrossValidation(pSpace, "ASAP");
//	        experiment.runTrainTest(pSpace, "ASAP");
	        experiment.runLearningCurve(pSpace, "ASAP");
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
}