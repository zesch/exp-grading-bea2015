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

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.clusterers.FarthestFirst;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Clusters the training data, selects an exemplar/centroid and evaluates on the test data.
 */
public class ClusterExemplarTask
    extends ExecutableTaskBase
    implements Constants
{
	
    /**
     * Public name of the output folder for the new training data
     */
    public static final String ADAPTED_TRAINING_DATA = "train.new";
    
    @Discriminator
    private List<String> clusteringArguments;
    @Discriminator
    private String featureMode;
    @Discriminator
    private String learningMode;

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

        Instances trainData = TaskUtils.getInstances(arffFileTrain, multiLabel);
		
        Clusterer abstractClusterer = AbstractClusterer.forName(clusteringArguments.get(0), clusteringArguments
                .subList(1, clusteringArguments.size()).toArray(new String[0]));
        
        // we assume that only this method has been used - breaks modularity, but need results fast ... :/
        SimpleKMeans clusterer = (SimpleKMeans) abstractClusterer;
        
        trainData = WekaUtils.removeOutcomeId(trainData, multiLabel);
        Instances copyTrainData = new Instances(trainData);
        
	    // generate data for clusterer (w/o class)
	    Remove filter = new Remove();
	    filter.setAttributeIndices("" + (trainData.classIndex() + 1));
	    filter.setInputFormat(trainData);
	    Instances clusterTrainData = Filter.useFilter(trainData, filter);
        
        clusterer.buildClusterer(clusterTrainData);
        Instances centroids = clusterer.getClusterCentroids();
        
//        Add addFilter = new Add();
//        addFilter.setAttributeIndex(new Integer(numTestLabels + i + 1).toString());
//        addFilter.setNominalLabels("0,1");
//        addFilter.setAttributeName(trainData.attribute(i).name() + COMPATIBLE_OUTCOME_CLASS);
//        addFilter.setInputFormat(testData);
        
        trainData.clear();
        
       	Enumeration<Instance> centroidInstances = centroids.enumerateInstances();
    	while (centroidInstances.hasMoreElements()) {
    		Instance centroidInstance = centroidInstances.nextElement();
    		
    		// centroidInstance is usually not a real instance, but a virtual centroid
    		// we need to find the closest point in the training data
    		double minDistance = Double.POSITIVE_INFINITY;
    		int offset = 0;
    		int minOffset = 0;
        	Enumeration<Instance> trainInstances = clusterTrainData.enumerateInstances();
            while (trainInstances.hasMoreElements()) {
            	Instance trainInstance = trainInstances.nextElement();

            	double dist = distance(centroidInstance, trainInstance);
            	if (dist < minDistance) {
            		minDistance = dist;
            		minOffset = offset;
            	}
            	offset++;
            }
    		
            // add selected instance to instances
            trainData.add(copyTrainData.get(minOffset));			
    	}


        // write the new training data (that will be used by the test task instead of the original one)                
        DataSink.write(aContext.getStorageLocation(ADAPTED_TRAINING_DATA, AccessMode.READWRITE).getPath()
                + "/" + ARFF_FILENAME, trainData);
    }
    
    private double distance(Instance i1, Instance i2) {
    	double dist = 0.0;
    	for (int i=0; i<i1.numAttributes(); i++) {
    		dist += Math.abs(i1.value(i) - i2.value(i));
    	}
    	return dist / i1.numAttributes();
    }
}