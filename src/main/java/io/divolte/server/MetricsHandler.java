package io.divolte.server;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import io.divolte.server.config.StatsdConfiguration;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
public class MetricsHandler implements HttpHandler {

    private static final Pattern IGNORE_PATHS = Pattern.compile("^/(ping|static|.*\\.js|.*\\.css).*");
    private final StatsDClient statsd;
    private final HttpHandler next;

    MetricsHandler(HttpHandler next, StatsDClient statsd) {
        this.next = next;
        this.statsd = statsd;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        boolean shouldReportMetric =
            !(IGNORE_PATHS.matcher(exchange.getRequestPath()).matches() || exchange.isComplete());

        if (shouldReportMetric) {
            final long start = System.currentTimeMillis();
            exchange.addExchangeCompleteListener((innerExchange, next) -> exchangeEvent(innerExchange, next, start));
        }

        next.handleRequest(exchange);
    }

    void exchangeEvent(HttpServerExchange innerExchange, ExchangeCompletionListener.NextListener
        nextListener, long start) {
        long time = System.currentTimeMillis() - start;
        String handler;

        switch (innerExchange.getStatusCode() / 100) {
            case 2:
                handler = "2xx";
                break;
            case 3:
                handler = "3xx";
                break;
            case 4:
                handler = "4xx";
                break;
            default:
                handler = "5xx";
                break;
        }

        statsd.increment(String.format("requests.%s.count", handler));
        statsd.recordExecutionTime(String.format("requests.%s.times", handler), time);
        nextListener.proceed();
    }

    public static MetricsHandler create(HttpHandler next, StatsdConfiguration configuration) {
        StatsDClient client;

        if (configuration.enabled) {
            client = new NonBlockingStatsDClient(
                configuration.prefix,
                configuration.host,
                configuration.port,
                null,
                configuration.bufferSize);
        } else {
            client = new NoOpStatsDClient();
        }

        return new MetricsHandler(next, client);
    }
}
