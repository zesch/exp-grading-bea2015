package de.unidue.langtech.grading;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.functions.SMO;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpDependencyParser;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.jazzy.SpellChecker;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.DependencyDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneCharacterNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneSkipNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.FeatureValuesReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.unidue.langtech.grading.report.KappaReport;
import de.unidue.langtech.grading.report.LearningCurveReport;
import de.unidue.langtech.grading.tc.BatchTaskClusterClassification;
import de.unidue.langtech.grading.tc.BatchTaskClusterClassificationExemplar;
import de.unidue.langtech.grading.tc.BatchTaskClustering;
import de.unidue.langtech.grading.tc.BatchTaskLearningCurve;

public abstract class ExperimentsBase
	implements Constants
{

    public static final String LANGUAGE_CODE = "en";

    public static final Boolean[] toLowerCase = new Boolean[] { true };
    
    public static final Boolean[] onlyPure = new Boolean[] { false, true};

    public static final String stopwordList = "classpath:/stopwords/english_stopwords.txt";
//    public static final String stopwordList = "classpath:/stopwords/english_empty.txt";
    
    public static final String SPELLING_VOCABULARY = "classpath:/vocabulary/en_US_dict.txt";

    public static final int NUM_FOLDS = 5;
    
    public static final int NR_OF_CLUSTERS = 32;
    
    public static final boolean useTagger = true;
    public static final boolean useParsing = true;
    public static final boolean useSpellChecking = false;

	@SuppressWarnings("unchecked")
	public static Dimension<List<String>> getClassificationArgsDim()
	{
        Dimension<List<String>> dimClassificationArgs = Dimension.create(
        		Constants.DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { SMO.class.getName() })
//                ,
//                Arrays.asList(new String[] { NaiveBayes.class.getName() })
        );
        return dimClassificationArgs;
	}
	
	@SuppressWarnings("unchecked")
	public static Dimension<List<Object>> getPipelineParameterDim()
	{
            Dimension<List<Object>> dimPipelineParameters = Dimension.create(
            DIM_PIPELINE_PARAMS,
            Arrays.asList(new Object[] {
            		LuceneNGramDFE.PARAM_NGRAM_USE_TOP_K, 5000,
                    LuceneNGramDFE.PARAM_NGRAM_STOPWORDS_FILE, stopwordList,
                    LuceneCharacterNGramDFE.PARAM_CHAR_NGRAM_MIN_N, 3,
                    LuceneCharacterNGramDFE.PARAM_CHAR_NGRAM_MAX_N, 5,
                    LuceneSkipNGramDFE.PARAM_NGRAM_USE_TOP_K, 5000
            }));
            return dimPipelineParameters;
        
	}         

	@SuppressWarnings("unchecked")
	public static Dimension<List<String>> getFeatureSetsDim()
	{

        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] {
                    NrOfTokensDFE.class.getName(),
                    LuceneNGramDFE.class.getName(),
                    LuceneSkipNGramDFE.class.getName(),
                    LuceneCharacterNGramDFE.class.getName(),
                    DependencyDFE.class.getName()
                })
        );
        return dimFeatureSets;
	}
	
	public static Map<String, Object> getFeatureSelectionDim()
	{
	    // single-label feature selection (Weka specific options), reduces the feature set to k features
	    Map<String, Object> dimFeatureSelection = new HashMap<String, Object>();
	    dimFeatureSelection.put(DIM_FEATURE_SEARCHER_ARGS,
	            asList(new String[] { Ranker.class.getName(), "-N", "5000" }));
	    dimFeatureSelection.put(DIM_ATTRIBUTE_EVALUATOR_ARGS,
	            asList(new String[] { InfoGainAttributeEval.class.getName() }));
	    dimFeatureSelection.put(DIM_APPLY_FEATURE_SELECTION, true);
	    
	    return dimFeatureSelection;
	}
	

	
    public static AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        AnalysisEngineDescription tagger       = createEngineDescription(NoOpAnnotator.class);
        AnalysisEngineDescription lemmatizer   = createEngineDescription(NoOpAnnotator.class);
        AnalysisEngineDescription chunker      = createEngineDescription(NoOpAnnotator.class);
        AnalysisEngineDescription spellChecker = createEngineDescription(NoOpAnnotator.class);
        AnalysisEngineDescription parser       = createEngineDescription(NoOpAnnotator.class);
        
        if (useTagger) {
        	System.out.println("Running tagger ...");
//                tagger = createEngineDescription(
//                        TreeTaggerPosLemmaTT4J.class,
//                        TreeTaggerPosLemmaTT4J.PARAM_LANGUAGE, LANGUAGE_CODE
//                );
            tagger = createEngineDescription(
                    ClearNlpPosTagger.class,
                    ClearNlpPosTagger.PARAM_LANGUAGE, LANGUAGE_CODE
            );
            lemmatizer = createEngineDescription(
                    ClearNlpLemmatizer.class
            );
        }

        if (useSpellChecking) {
            spellChecker = createEngineDescription(
            		SpellChecker.class,
            		SpellChecker.PARAM_MODEL_LOCATION, SPELLING_VOCABULARY
            );
        }
        
        if (useParsing) {
        	System.out.println("Running parser ...");
//                parser = createEngineDescription(
//                        StanfordParser.class,
//                        StanfordParser.PARAM_VARIANT, "pcfg"
//                );
            parser = createEngineDescription(
                    ClearNlpDependencyParser.class,
                    ClearNlpDependencyParser.PARAM_VARIANT, "ontonotes"
            );
        }
        
        return createEngineDescription(
                createEngineDescription(
                        ClearNlpSegmenter.class
                ),
                spellChecker,
                tagger,
                lemmatizer,
                chunker,
                parser
        );
    }
    
    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace, String name)
        throws Exception
    {
        BatchTaskCrossValidation batch = new BatchTaskCrossValidation(name + "-CV",
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
    protected void runTrainTest(ParameterSpace pSpace, String name)
        throws Exception
    {
        BatchTaskTrainTest batch = new BatchTaskTrainTest(name + "-TrainTest",
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
    protected void runLearningCurve(ParameterSpace pSpace, String name)
        throws Exception
    {
        BatchTaskLearningCurve batch = new BatchTaskLearningCurve(name + "-LearningCurve",
                getPreprocessing());
        // computes and stores the kappa values
        batch.addInnerReport(LearningCurveReport.class);    
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(BatchRuntimeReport.class);

        // Run
        Lab.getInstance().run(batch);
    }
    
    // ##### CLUSTERING #####
    protected void runClustering(ParameterSpace pSpace, String name)
        throws Exception
    {
        BatchTaskClustering batch = new BatchTaskClustering(name + "-Clustering",
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
    protected void runClusterClassification(ParameterSpace pSpace, String name)
        throws Exception
    {
        BatchTaskClusterClassification batch = new BatchTaskClusterClassification(name + "-ClusterClassification",
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
    
    // ##### CLUSTERING + CLASSIFICATION WITH CENTROIDS #####
    protected void runClusterClassificationCentroids(ParameterSpace pSpace, String name)
        throws Exception
    {
        BatchTaskClusterClassificationExemplar batch = new BatchTaskClusterClassificationExemplar(name + "-ClusterClassification",
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