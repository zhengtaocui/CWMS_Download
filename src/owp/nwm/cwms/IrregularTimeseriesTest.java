package owp.nwm.cwms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import org.junit.Test;

public class IrregularTimeseriesTest
{

    @Test
    public void test()
    {
        InputStream in;
        try
        {
            in =
               new FileInputStream("./xmldownloaded/SWF_TX08012-Gated_Total.Flow-Out.Ave.~1Day.1Day.Rev-SWF-REGI.xml");
            final CwmsDownloadManager manager = new CwmsDownloadManager();
            final IrregularTimeseries ts = (IrregularTimeseries)manager.parseXML(in, "123");

            System.out.println("Office: " + ts.getOffice());
            System.out.println("2016-10-03T05:00:00Z : " + ts.getValueAtTime(Instant.parse("2016-10-03T05:00:00Z")));
            final CwmsTimeSeries<Float> cts = ts.getDailyAverage();

            System.out.println(cts.getOffice());

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

        //     fail("Not yet implemented");
    }

}
