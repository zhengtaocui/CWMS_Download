package owp.nwm.cwms;

import java.time.Instant;

public abstract class timeseries<T extends Number>
{
    public abstract T getValueAtTime(final Instant k);
}
