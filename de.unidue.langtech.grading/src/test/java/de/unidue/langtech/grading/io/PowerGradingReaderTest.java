package de.unidue.langtech.grading.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

public class PowerGradingReaderTest
{

    @Test
    public void asap2ReaderTest() throws Exception {
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                PowerGradingReader.class,
                PowerGradingReader.PARAM_INPUT_FILE, "classpath:/powergrading/studentanswers_grades_100.tsv"
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertTrue(jcas.getDocumentText().length() < 453);
            i++;
        }
        assertEquals(2000, i);
    }
    
    @Test
    public void questionIdTest() throws Exception {
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                PowerGradingReader.class,
                PowerGradingReader.PARAM_INPUT_FILE, "classpath:/powergrading/studentanswers_grades_100.tsv",
                PowerGradingReader.PARAM_QUESTION_ID, 1
       );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertTrue(jcas.getDocumentText().length() < 432);
            i++;
        }
        assertEquals(100, i);
    }
}
