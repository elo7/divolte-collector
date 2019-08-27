package io.divolte.server.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class StatsdConfiguration {

    public final boolean enabled;
    public final String host;
    public final int port;
    public final String prefix;
    public final int bufferSize;

    @JsonCreator
    public StatsdConfiguration(
        boolean enabled,
        String host,
        int port,
        String prefix,
        @JsonProperty("buffer_size") int bufferSize) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.prefix = prefix;
        this.bufferSize = bufferSize;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("enabled", enabled)
            .add("host", host)
            .add("port", port)
            .add("prefix", prefix)
            .add("bufferSize", bufferSize)
            .toString();
    }
}
