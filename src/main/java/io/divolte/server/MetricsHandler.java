package io.divolte.server;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.codahale.metrics.MetricRegistry.name;

@ParametersAreNonnullByDefault
public class MetricsHandler implements HttpHandler {

    private static final Pattern IGNORE_PATHS = Pattern.compile("^/(ping|static|.*\\.js|.*\\.css).*");
    private final Timer timer2xx;
    private final Timer timer3xx;
    private final Timer timer4xx;
    private final Timer timer5xx;
    private final HttpHandler next;

    MetricsHandler(HttpHandler next, MetricRegistry registry) {
        timer2xx = registry.timer(name(this.getClass(), "requests.2xx"));
        timer3xx = registry.timer(name(this.getClass(), "requests.3xx"));
        timer4xx = registry.timer(name(this.getClass(), "requests.4xx"));
        timer5xx = registry.timer(name(this.getClass(), "requests.5xx"));
        this.next = next;

        JmxReporter
            .forRegistry(registry)
            .inDomain("io.divolte.server")
            .build()
            .start();
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

        switch (innerExchange.getStatusCode() / 100) {
            case 2:
                timer2xx.update(time, TimeUnit.MILLISECONDS);
                break;
            case 3:
                timer3xx.update(time, TimeUnit.MILLISECONDS);
                break;
            case 4:
                timer4xx.update(time, TimeUnit.MILLISECONDS);
                break;
            default:
                timer5xx.update(time, TimeUnit.MILLISECONDS);
                break;
        }

        nextListener.proceed();
    }

    public static MetricsHandler create(HttpHandler next) {
        MetricRegistry registry = SharedMetricRegistries.getOrCreate("divolte");
        return new MetricsHandler(next, registry);
    }
}
