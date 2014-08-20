package de.unidue.langtech.grading;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;
import de.unidue.langtech.grading.io.CbalReader;

public class CbalBaseline
    extends ExperimentsBase
{

    public static void main(String[] args)
        throws Exception
    {
        File baseDir = new File(new DkproContext().getWorkspace("ETS").getAbsolutePath() + "/CBAL");

        for (String question : CbalReader.cbalQuestions) {
	        ParameterSpace pSpace = getParameterSpace(baseDir.getAbsolutePath(), question);

	        CbalBaseline experiment = new CbalBaseline();
//	        experiment.runCrossValidation(pSpace, "CBAL");
//	        experiment.runTrainTest(pSpace, "CBAL");
	        experiment.runLearningCurve(pSpace, "CBAL");
        }
    }
    
    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(String basedir, String question) 
    		throws IOException
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, CbalReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(
                		CbalReader.PARAM_INPUT_FILE, basedir + "/" + question + ".train.csv"
        ));
        dimReaders.put(DIM_READER_TEST, CbalReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(
                		CbalReader.PARAM_INPUT_FILE, basedir + "/" + question + ".test.csv"
        ));

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