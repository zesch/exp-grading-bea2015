package de.unidue.langtech.grading;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.unidue.langtech.grading.io.Asap2Reader;
import de.unidue.langtech.grading.io.PowerGradingReader;
import de.unidue.langtech.grading.util.DatasetStatisticsCollector;

public class DatasetStatistics
    implements Constants
{
	
    public static final String LANGUAGE_CODE = "en";

    public static final String TRAIN_DATA_ALL_ASAP = "classpath:/asap/train.tsv";
    public static final String TRAIN_DATA_ALL_PG = "classpath:/powergrading/train_70.txt";

	public static final Integer[] essaySetIds = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    public static final boolean useTagger = true;
    public static final boolean useChunker = false;
    public static final boolean useParsing = true;
    public static final boolean useSpellChecking = false;

    public static void main(String[] args)
        throws Exception
    {	
    	System.out.println("ASAP");
        runAsapProcessing();
        getStatistics("ASAP");
        
    	System.out.println("Powergrading");
        runPowergradingProcessing();
        getStatistics("PG");
    }
    
    private static void getStatistics(String dataset)
    		throws NumberFormatException, IOException
    {
        List<Double> nrOfResponses = new ArrayList<>();
        List<Double> tokensPerResponse = new ArrayList<>();
        List<Double> ttr = new ArrayList<>();
        
        File[] files = new File("target").listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().startsWith(dataset)) {
         		     for (String line : FileUtils.readLines(file)) {
         		    	 String[] parts = line.split(":");
         		    	 if (parts[0].equals(DatasetStatisticsCollector.TOKENS_KEY)) {
         		    		 tokensPerResponse.add(Double.parseDouble(parts[1]));
         		    	 }
         		    	 else if (parts[0].equals(DatasetStatisticsCollector.RESPONSES_KEY)) {
         		    		 nrOfResponses.add(Double.parseDouble(parts[1]));
         		    	 }
         		    	 else if (parts[0].equals(DatasetStatisticsCollector.TTR_KEY)) {
         		    		 ttr.add(Double.parseDouble(parts[1]));
         		    	 }
        		     }
        	    }
            }
        }
        
        // responses
        double[] dataResponses = ArrayUtils.toPrimitive(nrOfResponses.toArray(new Double[0]));
        double[] dataTokens = ArrayUtils.toPrimitive(tokensPerResponse.toArray(new Double[0]));
        double[] dataTTR = ArrayUtils.toPrimitive(ttr.toArray(new Double[0]));
        
        Mean mean = new Mean();
        StandardDeviation stdDev = new StandardDeviation();

        System.out.println("avg # responses: " + mean.evaluate(dataResponses));
        System.out.println("+/- " + stdDev.evaluate(dataResponses));
        
        System.out.println("avg tokens per response: " + mean.evaluate(dataTokens));
        System.out.println("+/- " + stdDev.evaluate(dataTokens));
        
        System.out.println("TTR: " + mean.evaluate(dataTTR));
        System.out.println("+/- " + stdDev.evaluate(dataTTR));
        System.out.println();
        System.out.println();
    }
    
    private static void runAsapProcessing()
    		throws ResourceInitializationException, UIMAException, IOException
    {
        for (int essaySetId : essaySetIds) {
        	SimplePipeline.runPipeline(
        			CollectionReaderFactory.createReader(
	        			Asap2Reader.class,
	                    Asap2Reader.PARAM_INPUT_FILE, TRAIN_DATA_ALL_ASAP, 
	                    Asap2Reader.PARAM_ESSAY_SET_ID, essaySetId
        			),
		            createEngineDescription(
		                createEngineDescription(ClearNlpSegmenter.class),
		                createEngineDescription(
		                		DatasetStatisticsCollector.class,
		                		DatasetStatisticsCollector.PARAM_DATASET_NAME, "ASAP")
		            )
            );
        }
    }
    
    
    private static void runPowergradingProcessing()
    		throws ResourceInitializationException, UIMAException, IOException
    {
        for (int questionId : PowerGradingReader.questionIds) {
        	SimplePipeline.runPipeline(
        			CollectionReaderFactory.createReader(
	        			PowerGradingReader.class,
                		PowerGradingReader.PARAM_INPUT_FILE, TRAIN_DATA_ALL_PG,
                		PowerGradingReader.PARAM_QUESTION_ID, questionId
        			),
		            createEngineDescription(
		                createEngineDescription(ClearNlpSegmenter.class),
		                createEngineDescription(
		                		DatasetStatisticsCollector.class,
		                		DatasetStatisticsCollector.PARAM_DATASET_NAME, "PG")
		            )

            );
        }
    }
}