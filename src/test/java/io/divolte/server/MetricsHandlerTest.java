package io.divolte.server;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpServerExchange.class)
public class MetricsHandlerTest {

    @Mock
    private HttpHandler handler;
    @Mock
    private MetricRegistry registry;
    private HttpServerExchange exchange = PowerMockito.mock(HttpServerExchange.class);
    @Mock
    private ExchangeCompletionListener.NextListener nextListener;
    @Mock
    private Timer timer2xx;
    @Mock
    private Timer timer3xx;
    @Mock
    private Timer timer4xx;
    @Mock
    private Timer timer5xx;

    private MetricsHandler subject;

    @Before
    public void setup() {
        when(registry.timer(name(MetricsHandler.class, "requests.time.2xx"))).thenReturn(timer2xx);
        when(registry.timer(name(MetricsHandler.class, "requests.time.3xx"))).thenReturn(timer3xx);
        when(registry.timer(name(MetricsHandler.class, "requests.time.4xx"))).thenReturn(timer4xx);
        when(registry.timer(name(MetricsHandler.class, "requests.time.5xx"))).thenReturn(timer5xx);
        when(exchange.getStatusCode()).thenReturn(200);

        subject = new MetricsHandler(handler, registry);
    }

    @Test
    public void shouldNotSendMetricsForHealthCheckPath() throws Exception {
        when(exchange.getRequestPath()).thenReturn("/ping");

        subject.handleRequest(exchange);

        verify(exchange, never()).addExchangeCompleteListener(any());
    }

    @Test
    public void shouldNotSendMetricsForStaticPath() throws Exception {
        when(exchange.getRequestPath()).thenReturn("/static/res.png");

        subject.handleRequest(exchange);

        verify(exchange, never()).addExchangeCompleteListener(any());
    }

    @Test
    public void shouldNotSendMetricsForStaticJSPath() throws Exception {
        when(exchange.getRequestPath()).thenReturn("/res.js");

        subject.handleRequest(exchange);

        verify(exchange, never()).addExchangeCompleteListener(any());
    }

    @Test
    public void shouldNotSendMetricsForStaticCSSPath() throws Exception {
        when(exchange.getRequestPath()).thenReturn("/res.css");

        subject.handleRequest(exchange);

        verify(exchange, never()).addExchangeCompleteListener(any());
    }

    @Test
    public void shouldNotSendMetricsWhenExchangeIsComplete() throws Exception {
        when(exchange.getRequestPath()).thenReturn("/path");
        when(exchange.isComplete()).thenReturn(true);

        subject.handleRequest(exchange);

        verify(exchange, never()).addExchangeCompleteListener(any());
    }

    @Test
    public void shouldSendMetricsWhenPathIsAllowedAndExchangeIsNotComplete() throws Exception {
        when(exchange.getRequestPath()).thenReturn("/path");
        when(exchange.isComplete()).thenReturn(false);

        subject.handleRequest(exchange);

        verify(exchange).addExchangeCompleteListener(any());

    }

    @Test
    public void shouldSendMetricsFor2xx() {
        when(exchange.getStatusCode()).thenReturn(201);

        subject.exchangeEvent(exchange, nextListener, 0L);

        verify(timer2xx).update(anyLong(), any(TimeUnit.class));
        verify(timer3xx, never()).update(Mockito.anyLong(), any(TimeUnit.class));
        verify(timer4xx, never()).update(anyLong(), any(TimeUnit.class));
        verify(timer5xx, never()).update(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldSendMetricsFor3xx() {
        when(exchange.getStatusCode()).thenReturn(302);

        subject.exchangeEvent(exchange, nextListener, 0L);

        verify(timer2xx, never()).update(anyLong(), any(TimeUnit.class));
        verify(timer3xx).update(anyLong(), any(TimeUnit.class));
        verify(timer4xx, never()).update(anyLong(), any(TimeUnit.class));
        verify(timer5xx, never()).update(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldSendMetricsFor4xx() {
        when(exchange.getStatusCode()).thenReturn(404);

        subject.exchangeEvent(exchange, nextListener, 0L);

        verify(timer2xx, never()).update(anyLong(), any(TimeUnit.class));
        verify(timer3xx, never()).update(anyLong(), any(TimeUnit.class));
        verify(timer4xx).update(anyLong(), any(TimeUnit.class));
        verify(timer5xx, never()).update(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldSendMetricsFor5xx() {
        when(exchange.getStatusCode()).thenReturn(500);

        subject.exchangeEvent(exchange, nextListener, 0L);

        verify(timer2xx, never()).update(anyLong(), any(TimeUnit.class));
        verify(timer3xx, never()).update(anyLong(), any(TimeUnit.class));
        verify(timer4xx, never()).update(anyLong(), any(TimeUnit.class));
        verify(timer5xx).update(anyLong(), any(TimeUnit.class));
    }
}
