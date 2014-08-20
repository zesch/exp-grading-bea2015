package de.unidue.langtech.grading;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.clusterers.SimpleKMeans;
import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;
import de.unidue.langtech.grading.io.RfuReader;

public class RfuClustering
    extends ExperimentsBase
{

    public static void main(String[] args)
        throws Exception
    {
        File baseDir = new File(new DkproContext().getWorkspace("ETS").getAbsolutePath() + "/RFU");

        for (String question : RfuReader.rfuQuestions) {
        	System.out.println("Q: " + question);
	        ParameterSpace pSpace = getParameterSpace(baseDir.getAbsolutePath(), question);

	        RfuClustering experiment = new RfuClustering();
//	        experiment.runClustering(pSpace, "RFU");
	        experiment.runClusterClassification(pSpace, "RFU");

        }
    }
    
    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(String basedir, String question) 
    		throws IOException
    {
        // configure training and test data reader dimension
        // train/test will use both, while cross-validation will only use the train part
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, RfuReader.class);
        dimReaders.put(
                DIM_READER_TRAIN_PARAMS,
                Arrays.asList(
                		RfuReader.PARAM_INPUT_FILE, basedir + "/" + question + "_train.txt"
        ));
        dimReaders.put(DIM_READER_TEST, RfuReader.class);
        dimReaders.put(
                DIM_READER_TEST_PARAMS,
                Arrays.asList(
                		RfuReader.PARAM_INPUT_FILE, basedir + "/" + question + "_test.txt"
        ));

        Dimension<List<String>> dimClusteringArgs = Dimension.create("clusteringArguments",
                Arrays.asList(new String[] { SimpleKMeans.class.getName(), "-N", "50", })
        );  
   
        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_DATA_WRITER, WekaDataWriter.class.getName()),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT),
                getPipelineParameterDim(),
                getFeatureSetsDim(),
                getClassificationArgsDim(),
                dimClusteringArgs,
                Dimension.create("onlyPureClusters", onlyPure)
//                Dimension.createBundle("featureSelection", dimFeatureSelection)
        );

        return pSpace;
    }
}