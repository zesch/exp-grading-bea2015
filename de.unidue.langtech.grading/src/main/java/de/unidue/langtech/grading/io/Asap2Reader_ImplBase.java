package de.unidue.langtech.grading.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public abstract class Asap2Reader_ImplBase
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
    
    public static final String PARAM_ESSAY_SET_ID = "EssaySetId";
    @ConfigurationParameter(name = PARAM_ESSAY_SET_ID, mandatory = false)
    protected Integer requestedEssaySetId; 
    
    protected int currentIndex;
    
    protected Map<String, List<Asap2Item>> itemMap;

    protected Queue<Asap2Item> asap2Items;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        
        itemMap = new HashMap<String, List<Asap2Item>>();
        asap2Items = new LinkedList<Asap2Item>();

        if (requestedEssaySetId != null && requestedEssaySetId < 0) {
            getLogger().warn("Invalid essaySetId - using all documents");
            requestedEssaySetId = null;
        }
        
        
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
                if (nextLine.startsWith("Id\t")) {
                    nextLine = reader.readLine();
                }
                
                String[] nextItem = nextLine.split(separator);
                
                int textId       = -1;
                int essaySetId   = -1;
                String goldClass = null;
                String valClass  = null;
                String text      = null;
    
                if (nextItem.length == 5) {
                    textId     = Integer.parseInt(nextItem[0]);
                    essaySetId = Integer.parseInt(nextItem[1]);
                    goldClass  = nextItem[2];
                    valClass   = nextItem[3];
                    text       = nextItem[4];
                }
                else if (nextItem.length == 3) {
                    textId     = Integer.parseInt(nextItem[0]);
                    essaySetId = Integer.parseInt(nextItem[1]);
                    text       = nextItem[2];
                }
                else {
                    throw new IOException("Wrong file format.");
                }     
                
                // if validEssaySetId is set, then skip if not equal with current 
                if (requestedEssaySetId != null && requestedEssaySetId != essaySetId) {
                    continue;
                }
               
                Asap2Item newItem = new Asap2Item(textId, essaySetId, goldClass, valClass, text);
                
                List<Asap2Item> itemList;
                if (itemMap.containsKey(goldClass)) {
                    itemList = itemMap.get(goldClass);
                }
                else {
                    itemList = new ArrayList<Asap2Item>();
                }
                itemList.add(newItem);
                itemMap.put(goldClass, itemList);
                
                asap2Items.add(newItem);
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    
        currentIndex = 0;
    }
    
    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
    }
}
