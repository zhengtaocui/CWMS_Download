package owp.nwm.cwms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.junit.Test;

public class CwmsDownloadManagerTest
{

    @Test
    public void test()
    {
        final Path xmldir = Paths.get("./xmldownloaded");

        try
        {
            final Consumer<Path> xmlfile = new Consumer<Path>()
            {

                @Override
                public void accept(final Path t)
                {
                    // TODO Auto-generated method stub
                    System.out.println(t.toString());
                    InputStream in = null;
                    try
                    {
                        in = new FileInputStream(t.toFile());
                    }
                    catch(final FileNotFoundException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    CwmsDownloadManager manager;
                    try
                    {
                        manager = new CwmsDownloadManager();
                        final CwmsTimeSeries<Float> ts = manager.parseXML(in, "122");
                        if(ts == null)
                        {
                            System.out.println(t.toString() + " Parse ERROR !!!");
                        }
                        else
                        {
                            ts.getDailyAverage().toCSV(t.toString().substring(0, t.toString().length() - 4) + ".csv");
                        }
                    }
                    catch(final IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            };
            Files.newDirectoryStream(xmldir, p -> p.toString().endsWith(".xml")).forEach(xmlfile);
        }
        catch(final IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void Test1()
    {
        try
        {
            final CwmsDownloadManager manager = new CwmsDownloadManager();

            final Consumer<CwmsSiteBean> bean = new Consumer<CwmsSiteBean>()
            {

                @Override
                public void accept(final CwmsSiteBean t)
                {
                    // TODO Auto-generated method stub
                    InputStream in = null;
                    try
                    {
                        in = new FileInputStream("./xmldownloaded/" + t.getOffice() + "_" + t.getName_1() + ".xml");

                        System.out.println("./xmldownloaded/" + t.getOffice() + "_" + t.getName_1() + ".xml");
                    }
                    catch(final FileNotFoundException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return;
                    }
                    final CwmsTimeSeries<Float> ts = manager.parseXML(in, t.getSiteIndex());
                    if(ts == null)
                    {
                        System.out.println(t.getOffice() + "_" + t.getName_1() + " Parse ERROR !!!");
                    }
                    else
                    {
                        final CwmsTimeSeries<Float> dailyts = ts.getDailyAverage();
                        dailyts.toCMS();
                        dailyts.toCSV("./xmldownloaded/" + t.getOffice() + "_" + t.getName_1() + ".csv");
                    }
                }

            };

            manager.getSites().getSites().forEach(bean);
        }
        catch(final IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
