package de.unidue.langtech.grading.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class Asap2ReaderTest
{

    @Test
    public void asap2ReaderTest() throws Exception {
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                Asap2Reader.class,
                Asap2Reader.PARAM_INPUT_FILE, "classpath:/asap/train.tsv"
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertTrue(jcas.getDocumentText().length() < 1820);
            i++;
        }
        assertEquals(17043, i);
    }
    
    @Test
    public void essaySetIdTest() throws Exception {
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                Asap2Reader.class,
                Asap2Reader.PARAM_INPUT_FILE, "classpath:/asap/train.tsv",
                Asap2Reader.PARAM_ESSAY_SET_ID, 1
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertTrue(jcas.getDocumentText().length() < 1820);
            i++;
        }
        assertEquals(1672, i);
    }
}
