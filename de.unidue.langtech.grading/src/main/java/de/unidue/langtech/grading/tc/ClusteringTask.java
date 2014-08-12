/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.unidue.langtech.grading.tc;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.task.PreprocessTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Clusters the training data and evaluates on the test data.
 */
public class ClusteringTask
    extends ExecutableTaskBase
    implements Constants
{
    @Discriminator
    private List<String> classificationArguments;
    @Discriminator
    private String featureMode;
    @Discriminator
    private String learningMode;
    @Discriminator
    String threshold;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        if (learningMode.equals(Constants.LM_MULTI_LABEL)) {
        	throw new IllegalArgumentException("Cannot use multi-label setup in clustering.");
        }
        boolean multiLabel = false;

        File arffFileTrain = new File(aContext.getStorageLocation(
                TEST_TASK_INPUT_KEY_TRAINING_DATA,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_FILENAME);

        File arffFileTest = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA,
                AccessMode.READONLY).getPath()
                + "/" + TRAINING_DATA_FILENAME);

        Instances trainData = TaskUtils.getInstances(arffFileTrain, multiLabel);
        Instances testData = TaskUtils.getInstances(arffFileTest, multiLabel);

        testData = WekaUtils.makeOutcomeClassesCompatible(trainData, testData, multiLabel);
        
        // get number of outcomes
		List<String> trainOutcomeValues = TaskUtils.getClassLabels(trainData, multiLabel);
        int outcomeSize = trainOutcomeValues.size();

        Clusterer clusterer = AbstractClusterer.forName(classificationArguments.get(0), classificationArguments
                .subList(1, classificationArguments.size()).toArray(new String[0]));

        Instances copyTrainData = new Instances(trainData);
        trainData = WekaUtils.removeOutcomeId(trainData, multiLabel);
        testData = WekaUtils.removeOutcomeId(testData, multiLabel);
        
	    // generate data for clusterer (w/o class)
	    Remove filter = new Remove();
	    filter.setAttributeIndices("" + (trainData.classIndex() + 1));
	    filter.setInputFormat(trainData);
	    Instances clusterTrainData = Filter.useFilter(trainData, filter);
        
        clusterer.buildClusterer(clusterTrainData);
         
        // create a map of instance assignments to clusters
        Map<Integer, Set<Integer>> clusterMap = new HashMap<Integer, Set<Integer>>();
        @SuppressWarnings("rawtypes")
        Enumeration instanceEnumeration = clusterTrainData.enumerateInstances();
        int instanceOffset = 0;
        while (instanceEnumeration.hasMoreElements()) {
        	Instance instance = (Instance) instanceEnumeration.nextElement();
        	double[] distribution = clusterer.distributionForInstance(instance);
        	int clusterId = 0;
        	for (double value : distribution) {
        		if (new Double(value).intValue() == 1) {
        			Set<Integer> clusterInstances;
        			if (!clusterMap.containsKey(clusterId)) {
        				clusterInstances = new HashSet<Integer>();
        				clusterMap.put(clusterId, clusterInstances);
        			}
        			clusterInstances = clusterMap.get(clusterId);
        			clusterInstances.add(instanceOffset);
        			clusterMap.put(clusterId, clusterInstances);
        		}
        		clusterId++;
        	}
        	instanceOffset++;
        }
        
        Map<String, String> instanceId2TextMap = getInstanceId2TextMap(aContext);
        
        ConditionalFrequencyDistribution<Integer,String> clusterAssignments = new ConditionalFrequencyDistribution<Integer,String>();
        for (Integer clusterId : clusterMap.keySet()) {
        	System.out.println("CLUSTER: " + clusterId);
        	for (Integer offset : clusterMap.get(clusterId)) {
        		
        		// get instance ID from instance
        		Instance instance = copyTrainData.get(offset);
        		
        		Double classOffset = new Double(instance.value(copyTrainData.classAttribute()));
                String label = (String) trainOutcomeValues.get(classOffset.intValue());
                
        		clusterAssignments.addSample(clusterId, label);
       
        		String instanceId = instance.stringValue(copyTrainData.attribute(AddIdFeatureExtractor.ID_FEATURE_NAME).index());
        		System.out.println(label + "\t" + instanceId2TextMap.get(instanceId));
        	}
        	System.out.println();
        }
        
        System.out.println("ID\tSIZE\tPURITY");
        for (Integer clusterId : clusterMap.keySet()) {
        	FrequencyDistribution<String> fd = clusterAssignments.getFrequencyDistribution(clusterId);
        	double purity = (double) fd.getCount(fd.getSampleWithMaxFreq()) / fd.getN();
        	System.out.println(clusterId + "\t" + clusterMap.get(clusterId).size() + "\t" + purity);
        }
        System.out.println();
        
	    // evaluate clusterer
	    ClusterEvaluation eval = new ClusterEvaluation();
	    eval.setClusterer(clusterer);
	    eval.evaluateClusterer(trainData);

	    // print results
//	    System.out.println(eval.clusterResultsToString());
        
//        // file to hold prediction results
//        File evalOutput = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
//                AccessMode.READWRITE)
//                .getPath()
//                + "/" + EVALUATION_DATA_FILENAME);
//        
//        // Write out the predictions
//        DataSink.write(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
//                .getAbsolutePath() + "/" + PREDICTIONS_FILENAME, testData);
    }
    
    private Map<String, String> getInstanceId2TextMap(TaskContext aContext)
    		throws ResourceInitializationException
    {	
        Map<String, String> instanceId2TextMap = new HashMap<String,String>();

        // TrainTest setup: input files are set as imports
        File root = aContext.getStorageLocation(PreprocessTask.OUTPUT_KEY_TRAIN, AccessMode.READONLY);
        Collection<File> files = FileUtils.listFiles(root, new String[] { "bin" }, true);
        CollectionReaderDescription reader = createReaderDescription(BinaryCasReader.class, BinaryCasReader.PARAM_PATTERNS,
                files);
        
        for (JCas jcas : new JCasIterable(reader)) {
        	DocumentMetaData dmd = DocumentMetaData.get(jcas);
        	instanceId2TextMap.put(dmd.getDocumentId(), jcas.getDocumentText());
        }
        
        return instanceId2TextMap;
    }
}