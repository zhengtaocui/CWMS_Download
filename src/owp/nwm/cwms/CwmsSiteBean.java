package owp.nwm.cwms;

import com.opencsv.bean.CsvBindByName;

public class CwmsSiteBean
{
    @CsvBindByName
    private String office;
    @CsvBindByName
    private String CWMS_ID;

    @CsvBindByName
    private String Data_type;

    @CsvBindByName
    private Float latitude;

    @CsvBindByName
    private Float longitude;

    @CsvBindByName
    private String data_Freq;

    @CsvBindByName
    private String name3;

    @CsvBindByName
    private String public_nam;

    @CsvBindByName
    private String long_name;

    @CsvBindByName
    private String descriptio;

    @CsvBindByName
    private String horizontal;

    @CsvBindByName
    private String estimate;

    @CsvBindByName
    private String vertical_d;

    @CsvBindByName
    private String timezone;

    @CsvBindByName
    private String county;

    @CsvBindByName
    private String state;

    @CsvBindByName
    private String nation;

    @CsvBindByName
    private String nearst_ci;

    @CsvBindByName
    private String bounding_o;

    @CsvBindByName
    private String location_k;

    @CsvBindByName
    private String location_t;

    @CsvBindByName
    private String name_1;

    @CsvBindByName
    private String Alternate;

    @CsvBindByName
    private Float Mins;

    @CsvBindByName
    private String Obs_type;

    @CsvBindByName
    private Float Elev_ft;

    public String getOffice()
    {
        return office;
    }

    public String getCWMS_ID()
    {
        return CWMS_ID;
    }

    public String getName_1()
    {
        return name_1;
    }
}
