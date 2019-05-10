package owp.nwm.cwms;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

public abstract class timeseries<T>
{
    Map<Instant, T> timevalue = new TreeMap<>();

    public T getValueAtTime(final Instant k)
    {
        return timevalue.get(k);
    }
}
