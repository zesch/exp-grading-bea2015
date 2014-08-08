package de.unidue.langtech.grading.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

public class RfuReader
    extends JCasCollectionReader_ImplBase
{
    protected static final String DEFAULT_LANGUAGE = "en";
    
    public static final String PARAM_INPUT_FILE = "InputFile";
    @ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
    protected String inputFileString;
    protected URL inputFileURL;
    
    public static final String PARAM_LANGUAGE = "Language";
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = DEFAULT_LANGUAGE)
    protected String language;
    
    public static final String PARAM_ENCODING = "Encoding";
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
    private String encoding;
    
    public static final String PARAM_SEPARATOR = "Separator";
    @ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
    private String separator;
    
    protected int currentIndex;    

    protected Queue<GenericItem> items;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        items = new LinkedList<GenericItem>();
        
        try {
            inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            inputFileURL.openStream(),
                            encoding
                    )
            );
            
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                // skip the header
                if (nextLine.startsWith("id")) {
                    nextLine = reader.readLine();
                }

                String[] nextItem = nextLine.split(separator, 3);
                                
                String id   = null;
                String text = null;
                int grade   = -1;
    
                if (nextItem.length == 3) {
                    id    = nextItem[0];
                    text  = nextItem[1];
                    grade = Integer.parseInt(nextItem[2]);
                }
                else {
                    throw new IOException("Wrong file format.");
                }
               
                GenericItem newItem = new GenericItem(id, text, grade);

                items.add(newItem);
            }   
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

        currentIndex = 0;
    }
    
    @Override
    public boolean hasNext()
        throws IOException 
    {
        return !items.isEmpty();
    }

    @Override
    public void getNext(JCas jcas)
    	throws IOException, CollectionException
    {
        GenericItem item = items.poll();
        getLogger().debug(item);
                
        try
        {
	        if (language != null) {
	            jcas.setDocumentLanguage(language);
	        }
	        else {
	            jcas.setDocumentLanguage(DEFAULT_LANGUAGE);
	        }
	        
	        jcas.setDocumentText(item.getText());
	        	        	
            DocumentMetaData dmd = DocumentMetaData.create(jcas);
            dmd.setDocumentId(String.valueOf(item.getStudentId())); 
            dmd.setDocumentTitle(item.getStudentId());
            dmd.setDocumentUri(inputFileURL.toURI().toString());
            dmd.setCollectionId(inputFileString);
	        
        } 
		catch (URISyntaxException e) {
			throw new CollectionException(e);
		}
        
        TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
        outcome.setOutcome(Integer.toString(item.getGrade()));
        outcome.addToIndexes();

        currentIndex++;
    }
    
    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
    }
}
