/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal;

import org.junit.Test;

public class ReportTest
{
    @Test
    public void testReportHandleResolutionStatistics()
    {                               
        String args[] = {"-s", "20"}; 
        Report.main(args);
    }
}