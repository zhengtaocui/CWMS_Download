package owp.nwm.cwms;

public abstract class CwmsTimeSeries<T extends Number> extends timeseries<T>
{
    final static float CFS_TO_CMS = 0.028316846592f;
    String office;
    String name_1;
    String source;
    String unit;
    String siteIndex;

    public CwmsTimeSeries(final String o, final String n, final String s, final String u, final String si)
    {
        office = o;
        name_1 = n;
        source = s;
        unit = u;
        siteIndex = si;
    }

    public String getOffice()
    {
        return office;
    }

    public void setSource(final String s)
    {
        source = s;
    }

    public abstract void toCMS();

    public abstract CwmsTimeSeries<T> getDailyAverage();

    public abstract void toCSV(String csvfilename);
}
