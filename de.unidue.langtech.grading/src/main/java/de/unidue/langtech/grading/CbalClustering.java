package de.unidue.langtech.grading;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.clusterers.SimpleKMeans;
import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;
import de.unidue.langtech.grading.io.CbalReader;
import de.unidue.langtech.grading.report.KappaReport;
import de.unidue.langtech.grading.tc.BatchTaskClusterClassification;
import de.unidue.langtech.grading.tc.BatchTaskClustering;

public class CbalClustering
    extends ExperimentsBase
{
    public static final boolean useSpellChecking = false;

    public static void main(String[] args)
        throws Exception
    {
        File baseDir = new File(new DkproContext().getWorkspace("ETS").getAbsolutePath() + "/CBAL");

        for (String question : CbalReader.cbalQuestions) {
        	System.out.println("Q: " + question);
	        ParameterSpace pSpace = getParameterSpace(baseDir.getAbsolutePath(), question);

	        CbalClustering experiment = new CbalClustering();
//	        experiment.runClustering(pSpace);
	        experiment.runClusterClassification(pSpace);
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

        Dimension<List<String>> dimClusteringArgs = Dimension.create("clusteringArguments",
                Arrays.asList(new String[] { SimpleKMeans.class.getName(), "-N", "20", })
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

    // ##### CLUSTERING #####
    protected void runClustering(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskClustering batch = new BatchTaskClustering("CBAL-Clustering",
                getPreprocessing());    
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }
    
    // ##### CLUSTERING + CLASSIFICATION #####
    protected void runClusterClassification(ParameterSpace pSpace)
        throws Exception
    {
        BatchTaskClusterClassification batch = new BatchTaskClusterClassification("ASAP-ClusterClassification",
                getPreprocessing());    
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addInnerReport(KappaReport.class);
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(BatchOutcomeIDReport.class);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }
}