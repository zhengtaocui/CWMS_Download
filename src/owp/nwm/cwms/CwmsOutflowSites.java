package owp.nwm.cwms;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

public class CwmsOutflowSites
{
    private final List<CwmsSiteBean> sites = new ArrayList<>();

    public CwmsOutflowSites(final List<CwmsSiteBean> s)
    {
        for(final CwmsSiteBean e: s)
        {
            sites.add(e);
        }
    }

    public CwmsOutflowSites(final String sitefile) throws IOException
    {
        try (Reader reader = Files.newBufferedReader(Paths.get(sitefile));)
        {
            final CsvToBean<CwmsSiteBean> csvToBean =
                                                    new CsvToBeanBuilder<CwmsSiteBean>(reader).withType(CwmsSiteBean.class)
                                                                                              .withIgnoreLeadingWhiteSpace(true)
                                                                                              .build();

            final Iterator<CwmsSiteBean> cwmsSiteBeanIter = csvToBean.iterator();
            while(cwmsSiteBeanIter.hasNext())
            {
                final CwmsSiteBean cwmsSiteBean = cwmsSiteBeanIter.next();
                sites.add(cwmsSiteBean);
                System.out.println("office : " + cwmsSiteBean.getOffice());
                System.out.println("CWMS_ID : " + cwmsSiteBean.getCWMS_ID());
                System.out.println("name_1 : " + cwmsSiteBean.getName_1());
            }

        }

    }

    public List<CwmsSiteBean> getSites()
    {
        return sites;
    }

}
