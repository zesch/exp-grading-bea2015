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
import java.util.List;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.lab.task.Discriminator;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.ExecutableTaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.weka.util.WekaUtils;

/**
 * Builds the classifier from the training data and performs classification on the test data.
 * Repeats that for different amounts of training data in order to build a learning curve.
 * 
 */
public class LearningCurveTask
    extends ExecutableTaskBase
    implements Constants
{
	public static Integer[] NUMBER_OF_TRAINING_INSTANCES = new Integer[] {16,32,64,128,256,512,1024};

	public static Integer ITERATIONS = 25;

    @Discriminator
    private List<String> classificationArguments;   
    @Discriminator
    private String featureMode;
    @Discriminator
    private String learningMode;

    @Override
    public void execute(TaskContext aContext)
        throws Exception
    {
        boolean multiLabel = false;

        
        for (Integer numberInstances : NUMBER_OF_TRAINING_INSTANCES) {
        	for (int iteration=0; iteration<ITERATIONS; iteration++) {
                File arffFileTrain = new File(aContext.getStorageLocation(
                        TEST_TASK_INPUT_KEY_TRAINING_DATA,
                        AccessMode.READONLY).getPath()
                        + "/" + TRAINING_DATA_FILENAME);
                File arffFileTest = new File(aContext.getStorageLocation(TEST_TASK_INPUT_KEY_TEST_DATA,
                        AccessMode.READONLY).getPath()
                        + "/" + TRAINING_DATA_FILENAME);
                
                Instances trainData = TaskUtils.getInstances(arffFileTrain, multiLabel);
                Instances testData = TaskUtils.getInstances(arffFileTest, multiLabel);
                
                if (numberInstances > trainData.size()) {
                	continue;
                }

                Classifier cl = AbstractClassifier.forName(classificationArguments.get(0), classificationArguments
                            .subList(1, classificationArguments.size()).toArray(new String[0]));

                Instances copyTestData = new Instances(testData);
                trainData = WekaUtils.removeOutcomeId(trainData, multiLabel);
                testData = WekaUtils.removeOutcomeId(testData, multiLabel);
                
                Random generator = new Random();
                generator.setSeed(System.nanoTime());
                
                trainData.randomize(generator);
                
                // remove fraction of training data that should not be used for training
                for (int i = trainData.size() - 1; i >= numberInstances; i--) {
                	trainData.delete(i);
                }

                // file to hold prediction results
                File evalOutput = new File(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY,
                        AccessMode.READWRITE)
                        .getPath()
                        + "/" + EVALUATION_DATA_FILENAME + "_" + numberInstances + "_" + iteration);

                // train the classifier on the train set split - not necessary in multilabel setup, but
                // in single label setup
                cl.buildClassifier(trainData);
                
                weka.core.SerializationHelper.write(evalOutput.getAbsolutePath(),
                        WekaUtils.getEvaluationSinglelabel(cl, trainData, testData));
                testData = WekaUtils.getPredictionInstancesSingleLabel(testData, cl);
                testData = WekaUtils.addOutcomeId(testData, copyTestData, false);

//                // Write out the predictions
//                DataSink.write(aContext.getStorageLocation(TEST_TASK_OUTPUT_KEY, AccessMode.READWRITE)
//                        .getAbsolutePath() + "/" + PREDICTIONS_FILENAME + "_" + trainPercent, testData); 
        	} 	
        }
    }
}