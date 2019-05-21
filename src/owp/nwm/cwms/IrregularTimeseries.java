package owp.nwm.cwms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class IrregularTimeseries extends CwmsTimeSeries<Float>
{
    Map<Instant, AbstractMap.SimpleEntry<Float, Float>> timeValueQuality = new TreeMap<>();
    List<Float> qualities = null;

    public IrregularTimeseries(final String o,
                               final String n,
                               final String s,
                               final String u,
                               final String si,
                               final Map<Instant, AbstractMap.SimpleEntry<Float, Float>> ts)
    {
        super(o, n, s, u, si);
        // TODO Auto-generated constructor stub
        timeValueQuality = ts;
    }

    @Override
    public Float getValueAtTime(final Instant k)
    {
        // TODO Auto-generated method stub

        return timeValueQuality.get(k).getKey();
    }

    public Instant getFirstTime()
    {
        return timeValueQuality.keySet().stream().findFirst().get();
    }

    public Instant getLastTime()
    {
        return timeValueQuality.keySet().stream().skip(timeValueQuality.size() - 1).findFirst().get();
    }

    @Override
    public CwmsTimeSeries<Float> getDailyAverage()
    {
        final Map<Instant, AbstractMap.SimpleEntry<Float, Float>> dailytimevaluequality = new TreeMap<>();

        final Instant firstTime = getFirstTime();

        final Instant lastTime = getLastTime().plus(Duration.ofDays(1));

        for(Instant start = firstTime; lastTime.isAfter(start); start = start.plus(Duration.ofDays(1)))
        {

            final Instant currentTime = start;
            final Supplier<Stream<Entry<Instant, AbstractMap.SimpleEntry<Float, Float>>>> onedayentries =
                                                                                                        () -> timeValueQuality.entrySet()
                                                                                                                              .stream()
                                                                                                                              .filter(x -> {
                                                                                                                                  final LocalDate startld =
                                                                                                                                                          LocalDateTime.ofInstant(currentTime,
                                                                                                                                                                                  ZoneOffset.UTC)
                                                                                                                                                                       .toLocalDate();
                                                                                                                                  final LocalDate keyld =
                                                                                                                                                        LocalDateTime.ofInstant(x.getKey(),
                                                                                                                                                                                ZoneOffset.UTC)
                                                                                                                                                                     .toLocalDate();
                                                                                                                                  return keyld.isEqual(startld);
                                                                                                                              });

            //final Entry<Instant, Float> e = onedayentries.findFirst().get();

            //final AbstractMap.SimpleEntry<Instant, AbstractMap.SimpleEntry<Float, Float>> e =
            //                                                                              new AbstractMap.SimpleEntry<Instant, AbstractMap.SimpleEntry<Float, Float>>(start,
            //                                                                                                                                                          new AbstractMap.SimpleEntry<>(0.0f,
            //                                                                                                                                                                                        0.0f));

            //    if(onedayentries.count() > 0)
            //    {
            //        final Entry<Instant, Float> sum =
            //                                        onedayentries.reduce(e,
            //                                                             (e1,
            //                                                              e2) -> new AbstractMap.SimpleEntry<Instant, Float>(e1.getKey(),
//                                                                                                                    // Float.sum(e1.getValue(),
            //       e2.getValue())));

            final Float avg = Float.valueOf((float)(onedayentries.get().mapToDouble(x -> {
                return x.getValue().getKey().doubleValue();
            }).average().orElse(-9999.9)));

            final Float qualAvg = Float.valueOf((float)onedayentries.get().mapToDouble(x -> {
                return x.getValue().getValue();
            }).average().orElse(-9999.9));

            dailytimevaluequality.put(currentTime, new AbstractMap.SimpleEntry<Float, Float>(avg, qualAvg));
            //           }
            //           else
            //           {
            //               dailytimevalue.put(start, new Float(-999.0f));
            //          }
            // System.out.println(start.toString() + " : " + avg + "   " + qualAvg);
        }

        final IrregularTimeseries dailysteptimeseries = new IrregularTimeseries(this.office,
                                                                                this.name_1,
                                                                                this.source,
                                                                                this.unit,
                                                                                this.siteIndex,
                                                                                dailytimevaluequality);

        // TODO Auto-generated method stub
        return dailysteptimeseries;
    }

    @Override
    public void toCMS()
    {

        timeValueQuality.replaceAll((k, v) -> v.getKey() > 0 ? new AbstractMap.SimpleEntry<>(v.getKey() * CFS_TO_CMS,
                                                                                             v.getValue()) : v);
        unit = "CMS";
    }

    @Override
    public void toCSV(final String csvfilename)
    {
        // TODO Auto-generated method stub
        final Path path = Paths.get(csvfilename);

        try
        {
            final BufferedWriter writer = Files.newBufferedWriter(path);
            writer.write("# Time step :");
            writer.newLine();
            writer.write("#Unit: " + unit);
            writer.newLine();
            writer.write("#Query Name: " + office + "_" + name_1);
            writer.newLine();
            writer.write("#Nodata value: " + "-9999.9");
            writer.newLine();

            writer.write("office                USACE_site_index              datetime              value              quality");

            timeValueQuality.forEach((k, v) -> {
                try
                {
                    writer.newLine();
                    writer.write(String.format("%-8s%-30s%s  %10s %5s",
                                               office,
                                               siteIndex,
                                               k.toString(),
                                               v.getKey(),
                                               v.getValue()));
                }
                catch(final IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });

            writer.close();
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }

    }

}
