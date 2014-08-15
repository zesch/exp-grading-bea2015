package de.unidue.langtech.grading.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class DatasetStatisticsCollector
	extends JCasAnnotator_ImplBase
{
	public static final String RESPONSES_KEY = "responses";
	public static final String TOKENS_KEY = "tokens/response";
	public static final String TTR_KEY = "TTR";
	
    public static final String PARAM_DATASET_NAME = "datasetName";
    @ConfigurationParameter(name = PARAM_DATASET_NAME, mandatory = true)
    protected String dataset;

	private FrequencyDistribution<String> fd;
	
	private int nrofResponses;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException 
	{
		super.initialize(context);
		
		fd = new FrequencyDistribution<>();
		nrofResponses = 0;
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		nrofResponses++;
		
		for (Token token : JCasUtil.select(jcas, Token.class)) {
			fd.inc(token.getCoveredText().toLowerCase());
		}
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException 
	{
		super.collectionProcessComplete();
		
//		for (String sample : fd.getMostFrequentSamples(50)) {
//			System.out.println(sample + " - " + fd.getCount(sample));
//		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(RESPONSES_KEY + ":" + nrofResponses + "\n");
		sb.append(TOKENS_KEY + ":" + (double) fd.getN() / nrofResponses + "\n");
		sb.append(TTR_KEY + ":" + (double) fd.getB() / fd.getN() + "\n");
		System.out.println(sb.toString());
		
		try {
			FileUtils.writeStringToFile(new File("target/" + dataset + "_" + UUID.randomUUID() + ".txt"), sb.toString());
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}
