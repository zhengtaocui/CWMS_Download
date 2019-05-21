package owp.nwm.cwms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.List;

import org.junit.Test;

public class RegularTimeseriesTest
{

    @Test
    public void test()
    {
        try
        {
            final InputStream in =
                                 new FileInputStream("./xmldownloaded/SWL_OZGA4-Tailwater.Flow.Inst.1Hour.0.CCP-Comp.xml");
            final CwmsDownloadManager manager = new CwmsDownloadManager();
            final RegularTimeseries ts = (RegularTimeseries)manager.parseXML(in, "122");
            final List<AbstractMap.SimpleEntry<Instant, Instant>> startAndEnd = ts.getStartAndEnd();
            startAndEnd.forEach(x -> {
                System.out.println(x.getKey().toString() + " : " + x.getValue().toString());
            });
            System.out.println("Office: " + ts.getOffice());
            System.out.println("2017-02-06T22:00:00Z : " + ts.getValueAtTime(Instant.parse("2017-02-06T22:00:00Z")));
            System.out.println("2017-07-20T22:00:00Z : " + ts.getValueAtTime(Instant.parse("2017-07-20T22:00:00Z")));
            System.out.println("2017-07-17T06:00:00Z : " + ts.getValueAtTime(Instant.parse("2017-07-17T06:00:00Z")));
            final CwmsTimeSeries dailyAvg = ts.getDailyAverage();
            System.out.println(dailyAvg.getOffice());

        }
        catch(final FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch(final IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // fail("Not yet implemented");
    }

    @Test
    public void test1()
    {
        try
        {
            final InputStream in =
                                 new FileInputStream("/fs/pda/users/zcui/eclipse_workspace/CWMS_Download/xmldownloaded/SWL_OK10307.Flow-Res Out.Ave.1Hour.1Hour.Rev-Regi-Flowgroup.xml");
            final CwmsDownloadManager manager = new CwmsDownloadManager();
            final CwmsTimeSeries<Float> ts = manager.parseXML(in, "122");
            assertNull(ts);

        }
        catch(final FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch(final IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void test2()
    {
        try
        {
            final InputStream in =
                                 new FileInputStream("./xmldownloaded/SWL_AR00157-Tailwater.Flow.Inst.1Hour.0.CCP-Comp.xml");
            final CwmsDownloadManager manager = new CwmsDownloadManager();
            final RegularTimeseries ts = (RegularTimeseries)manager.parseXML(in, "122");
            final List<AbstractMap.SimpleEntry<Instant, Instant>> startAndEnd = ts.getStartAndEnd();
            startAndEnd.forEach(x -> {
                System.out.println(x.getKey().toString() + " : " + x.getValue().toString());
            });
            System.out.println("Office: " + ts.getOffice());

            final CwmsTimeSeries<Float> dailyAvg = ts.getDailyAverage();
            dailyAvg.toCMS();
            //dailyAvg.toCSV("./xmldownloaded/SWL_AR00157-Tailwater.Flow.Inst.1Hour.0.CCP-Comp_test.csv");
            System.out.println(dailyAvg.getOffice());
            System.out.println(dailyAvg.getValueAtTime(Instant.parse("2019-01-02T00:00:00Z")).floatValue());
            assertEquals(76.8997f, dailyAvg.getValueAtTime(Instant.parse("2019-01-02T00:00:00Z")).floatValue(), 0.001);

        }
        catch(final FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch(final IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
