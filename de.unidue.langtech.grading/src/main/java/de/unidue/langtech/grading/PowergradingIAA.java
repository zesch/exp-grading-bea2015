package de.unidue.langtech.grading;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.unidue.langtech.grading.io.PowerGradingReader;
import de.unidue.langtech.grading.util.QuadraticWeightedKappa;

public class PowergradingIAA
{

	 public static void main(String[] args)
			 throws Exception
	 {        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                PowerGradingReader.class,
                PowerGradingReader.PARAM_INPUT_FILE, "classpath:/powergrading/test_30.txt"
        );

        // should create the serialized grades in the right place
        SimplePipeline.runPipeline(reader);
        
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        List<Integer> grades1 = gson.fromJson(FileUtils.readFileToString(new File("target/grades1.txt")), listType);
        List<Integer> grades2 = gson.fromJson(FileUtils.readFileToString(new File("target/grades2.txt")), listType);
        List<Integer> grades3 = gson.fromJson(FileUtils.readFileToString(new File("target/grades3.txt")), listType);
        
        System.out.println(grades1);
        System.out.println("1-2: " + QuadraticWeightedKappa.getKappa(grades1, grades2, 0, 1));
        System.out.println("1-3: " + QuadraticWeightedKappa.getKappa(grades1, grades3, 0, 1));
        System.out.println("2-3: " + QuadraticWeightedKappa.getKappa(grades2, grades3, 0, 1));
    }
}