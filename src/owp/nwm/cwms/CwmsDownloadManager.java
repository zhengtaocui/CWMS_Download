package owp.nwm.cwms;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CwmsDownloadManager extends DownloadManager
{
    private final static String CWMS_OUTFLOW_SITES = "./CWMS_outflow_sites_263_index.csv";

    private final static String urlformat =
                                          "http://cwms-data.usace.army.mil/cwms-data/timeseries?office=%s&name=%s&format=%s&begin=%s&end=%s&timezone=UTC";

    private CwmsOutflowSites sites = null;

    public CwmsDownloadManager() throws IOException
    {
        try
        {
            sites = new CwmsOutflowSites(CWMS_OUTFLOW_SITES);
        }
        catch(final IOException e)
        {
            throw e;
        }
    }

    public CwmsOutflowSites getSites()
    {
        return sites;
    }

    public List<String> getURLs(final Instant begin, final Instant end, final String format)
    {
        final List<String> urls = new ArrayList<String>();

        for(final CwmsSiteBean site: sites.getSites())
        {
            urls.add(this.getURL(site.getOffice(), site.getName_1(), begin, end, format));
        }
        return urls;
    }

    public String getURL(final String office,
                         final String name_1,
                         final Instant begin,
                         final Instant end,
                         final String format)
    {
        return (begin == null ? (end == null ? String.format(urlformat,
                                                             office,
                                                             name_1,
                                                             format,
                                                             "1900-01-01T00:00:00",
                                                             "") : String.format(urlformat,
                                                                                 office,
                                                                                 name_1,
                                                                                 format,
                                                                                 "1900-01-01T00:00:00Z",
                                                                                 end.toString())) : (end == null ? String.format(urlformat,
                                                                                                                                 office,
                                                                                                                                 name_1,
                                                                                                                                 format,
                                                                                                                                 begin.toString(),
                                                                                                                                 "") : String.format(urlformat,
                                                                                                                                                     office,
                                                                                                                                                     name_1,
                                                                                                                                                     format,
                                                                                                                                                     begin.toString(),
                                                                                                                                                     end.toString()))).replaceAll("\\s",
                                                                                                                                                                                  "%20");

    }

    public void toCsvFile(final InputStream in, final String outfilename)
    {
        final Path path = Paths.get(outfilename);
        BufferedWriter writer = null;

        try
        {
            writer = Files.newBufferedWriter(path);
            boolean first = true;

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final org.w3c.dom.Document document = builder.parse(in);
            document.getDocumentElement().normalize();
            System.out.println("Root element: " + document.getDocumentElement().getNodeName());

            //final NodeList nList = document.getDocumentElement().getChildNodes();

            final NodeList nList = document.getDocumentElement().getElementsByTagName("time-series");

            //final NodeList nList = document.getElementsByTagName("time-series");

            // final String office = nList.item(0).getTextContent();
            // System.out.println("office =" + office);

            System.out.println("length = " + nList.getLength());
            for(int temp = 0; temp < nList.getLength(); temp++)
            {

                final Node nNode = nList.item(temp);
                if(nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    final Element e = (Element)nNode;
                    final String office = e.getElementsByTagName("office").item(0).getTextContent();
                    final String CWMS_ID = e.getElementsByTagName("name").item(0).getTextContent().split("\\.")[0];

                    System.out.println("\nCurrent Element :" + e.getTagName());
                    System.out.println("office = " + e.getElementsByTagName("office").item(0).getTextContent());
                    System.out.println("name = " + e.getElementsByTagName("name").item(0).getTextContent());

                    final NodeList tsList = e.getElementsByTagName("irregular-interval-values");
                    if(tsList.getLength() > 0)
                    {
                        for(int ts = 0; ts < tsList.getLength(); ts++)
                        {
                            final Node tsNode = tsList.item(ts);
                            final String unit = tsNode.getAttributes().getNamedItem("unit").getNodeValue();

                            if(first)
                            {
                                writer.write("# Irregular-interval-values: time step is not constant!");
                                writer.newLine();
                                writer.write("#Unit: " + unit);
                                writer.newLine();
                                writer.write("#Query Name: " + e.getElementsByTagName("name").item(0).getTextContent());
                                writer.newLine();

                                writer.write("office                CWMS_ID              datetime              value              quality");
                                first = false;
                            }

                            //System.out.println(tsNode.getTextContent());
                            final List<String> timeValueQuality = Collections
                                                                             .list(new StringTokenizer(tsNode.getTextContent(),
                                                                                                       "\n"))
                                                                             .stream()
                                                                             .map(x -> (String)x)
                                                                             .collect(Collectors.toList());

                            for(final String s: timeValueQuality)
                            {
                                final List<String> tokens =
                                                          Collections.list(new StringTokenizer(s))
                                                                     .stream()
                                                                     .map(x -> (String)x)
                                                                     .collect(Collectors.toList());
                                writer.newLine();
                                writer.write(String.format("%-8s%-30s%20s  %10s %5s",
                                                           office,
                                                           CWMS_ID,
                                                           tokens.get(0),
                                                           tokens.get(1),
                                                           tokens.get(2)));

                                //System.out.println(tokens.get(0));
                            }
                        }
                    }
                    final NodeList rtsList = e.getElementsByTagName("regular-interval-values");

                    if(rtsList.getLength() > 0)
                    {

                        for(int ts = 0; ts < rtsList.getLength(); ts++)
                        {
                            final Node tsNode = rtsList.item(ts);
                            System.out.println(tsNode.getNodeType());
                            final String intervalString =
                                                        tsNode.getAttributes().getNamedItem("interval").getNodeValue();
                            final int segmentCount = Integer.parseInt(tsNode.getAttributes()
                                                                            .getNamedItem("segment-count")
                                                                            .getNodeValue());
                            final String unit = tsNode.getAttributes().getNamedItem("unit").getNodeValue();
                            final Duration interval = Duration.parse(intervalString);

                            if(first)
                            {
                                writer.write("#Regular-interval-values: time step is constant!");
                                writer.newLine();
                                if(segmentCount > 1)
                                {
                                    writer.write("#Contains multiple segments, missing time steps possible!!");
                                    writer.newLine();
                                }
                                writer.write("#Unit: " + unit);
                                writer.newLine();
                                writer.write("#Query Name: " + e.getElementsByTagName("name").item(0).getTextContent());
                                writer.newLine();

                                writer.write("office                CWMS_ID              datetime              value              quality");
                                first = false;
                            }

                            System.out.println("Interval: " + interval);
                            System.out.println("segmentCount = " + segmentCount);

                            final NodeList segments = ((Element)tsNode).getElementsByTagName("segment");
                            System.out.println("Child nodes = " + segments.getLength());
                            for(int seg = 0; seg < segments.getLength(); seg++)
                            {

                                final String firstTimeString = segments.item(seg)
                                                                       .getAttributes()
                                                                       .getNamedItem("first-time")
                                                                       .getNodeValue();
                                final Instant firstTime = Instant.parse(firstTimeString);

                                final String order = segments.item(seg)
                                                             .getAttributes()
                                                             .getNamedItem("position")
                                                             .getNodeValue();
                                System.out.println("position = " + order + " : " + firstTime.toString());

                                final List<String> valueQuality = Collections
                                                                             .list(new StringTokenizer(segments.item(seg)
                                                                                                               .getTextContent(),
                                                                                                       "\n"))
                                                                             .stream()
                                                                             .map(x -> (String)x)
                                                                             .collect(Collectors.toList());
                                Instant currentTime = firstTime;
                                for(final String s: valueQuality)
                                {
                                    final List<String> tokens = Collections.list(new StringTokenizer(s))
                                                                           .stream()
                                                                           .map(x -> (String)x)
                                                                           .collect(Collectors.toList());
                                    writer.newLine();
                                    writer.write(String.format("%-8s%-30s%s  %10s %5s",
                                                               office,
                                                               CWMS_ID,
                                                               currentTime.toString(),
                                                               tokens.get(0),
                                                               tokens.get(1)));
                                    currentTime = currentTime.plus(interval);
//System.out.println(tokens.get(0));
                                }

                            }
//                        System.out.println(tsNode.getTextContent());
                        }
                    }
                    /*
                     * final NodeList childs = nNode.getChildNodes(); System.out.println("length = " +
                     * childs.getLength()); for(int c = 0; c < childs.getLength(); c++) {
                     * System.out.println("\nChild Element :" + nList.item(c)); }
                     */
                }
            }

        }
        catch(final ParserConfigurationException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch(final SAXException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(writer != null)
            {
                try
                {
                    writer.close();

                }
                catch(final IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public CwmsTimeSeries<Float> parseXML(final InputStream in, final String siteIndex)
    {
        CwmsTimeSeries<Float> ts = null;
        try
        {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document;
            document = builder.parse(in);
            document.getDocumentElement().normalize();
            System.out.println("Root element: " + document.getDocumentElement().getNodeName());

            final NodeList nList = document.getDocumentElement().getElementsByTagName("time-series");

            System.out.println("length = " + nList.getLength());

            final Node nNode = nList.item(0);
            final Element e = (Element)nNode;
            final String office = e.getElementsByTagName("office").item(0).getTextContent();
            final String CWMS_ID = e.getElementsByTagName("name").item(0).getTextContent().split("\\.")[0];

            NodeList tsList = e.getElementsByTagName("irregular-interval-values");
            if(tsList.getLength() > 0)
            {
                final Map<Instant, AbstractMap.SimpleEntry<Float, Float>> timeValuequality = new TreeMap<>();

                final Node tsNode = tsList.item(0);
                final String unit = tsNode.getAttributes().getNamedItem("unit").getNodeValue();
                final List<String> timeValueQuality =
                                                    Collections.list(new StringTokenizer(tsNode.getTextContent(), "\n"))
                                                               .stream()
                                                               .map(x -> (String)x)
                                                               .collect(Collectors.toList());
                for(final String s: timeValueQuality)
                {
                    final List<String> tokens = Collections.list(new StringTokenizer(s))
                                                           .stream()
                                                           .map(x -> (String)x)

                                                           .collect(Collectors.toList());
                    System.out.println(tokens.toString());
                    try
                    {
                        timeValuequality.put(Instant.parse(tokens.get(0)),
                                             new AbstractMap.SimpleEntry<Float, Float>(Float.parseFloat(tokens.get(1)),
                                                                                       Float.parseFloat(tokens.get(2))));
                    }
                    catch(final NumberFormatException e1)
                    {
                        e1.printStackTrace();
                    }
                }
                ts = new IrregularTimeseries(office, CWMS_ID, null, unit, siteIndex, timeValuequality);
            }
            else
            {
                tsList = e.getElementsByTagName("regular-interval-values");

                if(tsList.getLength() > 0)
                {
                    final Node tsNode = tsList.item(0);
                    final String intervalString = tsNode.getAttributes().getNamedItem("interval").getNodeValue();
                    final int segmentCount = Integer.parseInt(tsNode.getAttributes()
                                                                    .getNamedItem("segment-count")
                                                                    .getNodeValue());
                    final String unit = tsNode.getAttributes().getNamedItem("unit").getNodeValue();
                    final Duration interval = Duration.parse(intervalString);

                    final NodeList segments = ((Element)tsNode).getElementsByTagName("segment");
                    final List<List<AbstractMap.SimpleEntry<Float, Float>>> segs =
                                                                                 new ArrayList<List<AbstractMap.SimpleEntry<Float, Float>>>();
                    final List<AbstractMap.SimpleEntry<Instant, Instant>> startAndEnd =
                                                                                      new ArrayList<AbstractMap.SimpleEntry<Instant, Instant>>();

                    for(int seg = 0; seg < segments.getLength(); seg++)
                    {
                        final String firstTimeString =
                                                     segments.item(seg)
                                                             .getAttributes()
                                                             .getNamedItem("first-time")
                                                             .getNodeValue();
                        final Instant firstTime = Instant.parse(firstTimeString);

                        final String order = segments.item(seg).getAttributes().getNamedItem("position").getNodeValue();

                        final List<String> valueQuality = Collections
                                                                     .list(new StringTokenizer(segments.item(seg)
                                                                                                       .getTextContent(),
                                                                                               "\n"))
                                                                     .stream()
                                                                     .map(x -> (String)x)
                                                                     .collect(Collectors.toList());
                        Instant currentTime = firstTime;

                        final List<AbstractMap.SimpleEntry<Float, Float>> seg1 = new ArrayList<>();
                        for(final String s: valueQuality)
                        {
                            final List<String> tokens = Collections.list(new StringTokenizer(s))
                                                                   .stream()
                                                                   .map(x -> (String)x)
                                                                   .collect(Collectors.toList());

                            seg1.add(new AbstractMap.SimpleEntry<Float, Float>(Float.parseFloat(tokens.get(0)),
                                                                               Float.parseFloat(tokens.get(1))));

                            currentTime = currentTime.plus(interval);
                        }
                        segs.add(seg1);

                        startAndEnd.add(new SimpleEntry<Instant, Instant>(firstTime, currentTime));
                    }
                    ts = new RegularTimeseries(office, CWMS_ID, null, unit, siteIndex, segs, interval, startAndEnd);
                }

            }

        }
        catch(ParserConfigurationException | SAXException | IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            ts = null;
        }
        return ts;

    }

    public void downloadSites(final Instant begin, final Instant end, final String format)
    {
        URL cwmsWebSite = null;
        final ReadableByteChannel rbc = null;
        final FileOutputStream fos = null;

        for(final CwmsSiteBean site: sites.getSites())
        {
            try
            {
                cwmsWebSite = new URL(this.getURL(site.getOffice(), site.getName_1(), begin, end, format));

                System.out.println(this.getURL(site.getOffice(), site.getName_1(), begin, end, format));

                toCsvFile(cwmsWebSite.openStream(), site.getOffice() + "_" + site.getName_1() + ".csv");
                //            System.exit(0);
                //            rbc = Channels.newChannel(cwmsWebSite.openStream());
                //            fos = new FileOutputStream(site.getOffice() + "_" + site.getName_1() + "." + format);
                //            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                //            rbc.close();
                //            fos.close();
            }
            catch(final MalformedURLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch(final IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
//                System.out.println("Try again: " + this.getURL(site.getOffice(), site.getName_1(), begin, end, format));
                try
                {
                    Thread.sleep(60 * 1000);
                }
                catch(final InterruptedException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                //               try
//                {
//                    rbc = Channels.newChannel(cwmsWebSite.openStream());
//                    fos = new FileOutputStream(site.getOffice() + "_" + site.getName_1() + "." + format);
//                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//                    rbc.close();
//                    fos.close();
//                }
//                catch(final IOException ioe)
//                {
                //                   ioe.printStackTrace();
//                    System.out.println("Re-try failed again: "
///                        + this.getURL(site.getOffice(), site.getName_1(), begin, end, format));
//
//                }
            }
            //         finally
            //         {
            //             try
            //              {
            //       if(rbc.isOpen())
            //       {
            //                  rbc.close();
            //              }
            //               if(fos != null)
            //              {
            //                  fos.close();
            //              }
            //          }
            //            catch(final IOException e)
            //            {
            //               e.printStackTrace();
            //          }
            //      }
//
        }
    }

    public static void main(final String[] args)
    {
        try
        {

            final CwmsDownloadManager manager = new CwmsDownloadManager();
            manager.downloadSites(null, null, "xml");

        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }

    }
}
