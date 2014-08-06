package de.unidue.langtech.grading.io;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

public class Asap2Reader
    extends Asap2Reader_ImplBase
{
 
    @Override
    public boolean hasNext()
        throws IOException 
    {
        return !asap2Items.isEmpty();
    }

    @Override
    public void getNext(JCas jcas)
    	throws IOException, CollectionException
    {
        Asap2Item asap2Item = asap2Items.poll();
        getLogger().debug(asap2Item);
                
        try
        {
	        if (language != null) {
	            jcas.setDocumentLanguage(language);
	        }
	        else {
	            jcas.setDocumentLanguage(DEFAULT_LANGUAGE);
	        }
	        
	        jcas.setDocumentText(asap2Item.getText());
	        	
            DocumentMetaData dmd = DocumentMetaData.create(jcas);
            dmd.setDocumentId(String.valueOf(asap2Item.getTextId())); // + "-" + asap2Item.getEssaySetId());
            dmd.setDocumentTitle(Integer.toString(asap2Item.getTextId()));
            dmd.setDocumentUri(inputFileURL.toURI().toString());
            dmd.setCollectionId(Integer.toString(asap2Item.getEssaySetId()));
	        
        } 
		catch (URISyntaxException e) {
			throw new CollectionException(e);
		}
        
        // The gold score is always assigned to the container CAS
        TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
        outcome.setOutcome(asap2Item.getGoldClass());
        outcome.addToIndexes();
        
        currentIndex++;
    }
}