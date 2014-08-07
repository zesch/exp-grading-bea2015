package de.unidue.langtech.grading.report;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.storage.filesystem.FileSystemStorageService;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.PropertiesAdapter;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.TestTask;

public class KappaReportTest
{

    @Before
    public void setup() {
        File resultsFile = new File("src/test/resources/report_repo/kappa/results.prop");
        resultsFile.delete();        
    }
    
    @Test
    public void kappaReportTest()
            throws Exception
    {
        
        // DKPRO_HOME needs to be set, but repo can point elsewhere
        System.setProperty("DKPRO_HOME", new File("target").getAbsolutePath());
        File repo = new File("src/test/resources/report_repo/");

        Lab framework = Lab.getInstance();
        ((FileSystemStorageService) framework.getStorageService()).setStorageRoot(repo);

        KappaReport report = new KappaReport();
        report.setContext(framework.getTaskContextFactory().getContext("kappa"));
        report.execute();
        
        Map<String, String> discriminatorsMap = report.getContext().getStorageService().retrieveBinary(report.getContext().getId(), TestTask.RESULTS_FILENAME, new PropertiesAdapter()).getMap();
        assertEquals(0.3017996, Double.parseDouble(discriminatorsMap.get(KappaReport.KAPPA)), 0.000001);    
    }
}
