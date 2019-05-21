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
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class RegularTimeseries extends CwmsTimeSeries<Float>
{

    List<List<AbstractMap.SimpleEntry<Float, Float>>> segments;
    Duration interval;
    List<AbstractMap.SimpleEntry<Instant, Instant>> startEnd;

    public RegularTimeseries(final String o,
                             final String n,
                             final String s,
                             final String u,
                             final String si,
                             final List<List<AbstractMap.SimpleEntry<Float, Float>>> seg,
                             final Duration d,
                             final List<AbstractMap.SimpleEntry<Instant, Instant>> segStartEnd)
    {
        super(o, n, s, u, si);
        // TODO Auto-generated constructor stub
        segments = seg;
        interval = d;
        startEnd = segStartEnd;

    }

    public Integer getSegmentIndexByTime(final Instant t)
    {
        Integer index = null;
        for(int i = 0; i < startEnd.size(); i++)
        {
            if((startEnd.get(i).getKey().equals(t) || startEnd.get(i).getKey().isBefore(t))
                && startEnd.get(i).getValue().equals(t) || startEnd.get(i).getValue().isAfter(t))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public Float getValueAtTime(final Instant k)
    {
        // TODO Auto-generated method stub
        final Integer index = this.getSegmentIndexByTime(k);

        final int valueInd = (int)(Duration.between(this.startEnd.get(index).getKey(), k).getSeconds()
            / interval.getSeconds());

        return segments.get(index).get(valueInd).getKey();
    }

    public List<AbstractMap.SimpleEntry<Instant, Instant>> getStartAndEnd()
    {
        return startEnd;
    }

    @Override
    public CwmsTimeSeries<Float> getDailyAverage()
    {

        final Instant firstTime = startEnd.get(0).getKey();
        final Instant lastTime = startEnd.get(startEnd.size() - 1).getValue();

        final List<List<AbstractMap.SimpleEntry<Float, Float>>> segs = new ArrayList<>();
        final List<AbstractMap.SimpleEntry<Float, Float>> seg = new ArrayList<>();

        for(Instant start =
                          firstTime; start.isBefore(lastTime.plus(Duration.ofDays(1))); start =
                                                                                              start.plus(Duration.ofDays(1)))
        {
            final List<AbstractMap.SimpleEntry<Instant, Instant>> se = new ArrayList<>();

            final LocalDate ld = LocalDateTime.ofInstant(start, ZoneOffset.UTC).toLocalDate();

            final Deque<Integer> indexes = new LinkedList<Integer>();
            int count = 0;
            for(final AbstractMap.SimpleEntry<Instant, Instant> s: startEnd)
            {
                final LocalDate lstart = LocalDateTime.ofInstant(s.getKey(), ZoneOffset.UTC).toLocalDate();
                final LocalDate lend = LocalDateTime.ofInstant(s.getValue().minus(interval), ZoneOffset.UTC)
                                                    .toLocalDate();
                if((lstart.isEqual(ld) || lstart.isBefore(ld)) && (lend.isAfter(ld) || lend.isEqual(ld)))
                {
                    indexes.add(count);
                    se.add(s);
                }
                count++;
            }

            if(indexes.isEmpty())
            {
                seg.add(new AbstractMap.SimpleEntry<Float, Float>(-9999f, -9999f));
                // System.out.println(start.truncatedTo(ChronoUnit.DAYS).toString() + ": -9999.0   -9999.0");
            }
            else
            {
                float sum = 0;
                float qualsum = 0;
                count = 0;
                for(final Integer i: indexes)
                {
                    Instant t = startEnd.get(i).getKey();
                    for(int j = 0; j < segments.get(i).size(); j++)
                    {
                        if(LocalDateTime.ofInstant(t, ZoneOffset.UTC)
                                        .toLocalDate()
                                        .isEqual(LocalDateTime.ofInstant(start, ZoneOffset.UTC).toLocalDate()))
                        {
                            sum += segments.get(i).get(j).getKey();
                            qualsum += segments.get(i).get(j).getValue();
                            count++;
                        }
                        t = t.plus(interval);
                    }
                }
                seg.add(new AbstractMap.SimpleEntry<Float, Float>(sum / count, qualsum / count));
                // System.out.println(start.truncatedTo(ChronoUnit.DAYS).toString() + ": " + sum / count + "  "
                //    + qualsum / count);
            }
        }
        segs.add(seg);

        return new RegularTimeseries(this.office,
                                     this.name_1,
                                     this.source,
                                     this.unit,
                                     this.siteIndex,
                                     segs,
                                     Duration.ofDays(1),
                                     new ArrayList<AbstractMap.SimpleEntry<Instant, Instant>>(Arrays.asList(new AbstractMap.SimpleEntry<Instant, Instant>(firstTime.truncatedTo(ChronoUnit.DAYS),
                                                                                                                                                          lastTime.truncatedTo(ChronoUnit.DAYS)))));

    }

    @Override
    public void toCMS()
    {
        segments.forEach(x -> {
            if(unit.equals("cfs"))
                x.replaceAll(kq -> kq.getKey() > 0 ? new AbstractMap.SimpleEntry<Float, Float>(kq.getKey() * CFS_TO_CMS,
                                                                                               kq.getValue()) : kq);
        });
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
            writer.write("# Time step :" + interval.toString());
            writer.newLine();
            writer.write("#Unit: " + unit);
            writer.newLine();
            writer.write("#Query Name: " + office + "_" + name_1);
            writer.newLine();
            writer.write("#Nodata value: " + "-9999.9");
            writer.newLine();

            writer.write("office                USACE_site_index              datetime              value              quality");

            for(int i = 0; i < segments.size(); i++)
            {
                Instant t = startEnd.get(i).getKey();
                for(int j = 0; j < segments.get(i).size(); j++)
                {
                    writer.newLine();
                    writer.write(String.format("%-8s%-30s%s  %10s %5s",
                                               office,
                                               siteIndex,
                                               t.toString(),
                                               segments.get(i).get(j).getKey(),
                                               segments.get(i).get(j).getValue()));
                    t = t.plus(interval);

                }
            }
            writer.close();
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }

    }

}
