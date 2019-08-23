package io.divolte.server;

import com.timgroup.statsd.StatsDClient;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpServerExchange.class)
public class MetricsHandlerTest {

    @Mock
    private HttpHandler handler;
    @Mock
    private StatsDClient statsd;
    @Mock
    private ExchangeCompletionListener.NextListener nextListener;
    @Captor
    private ArgumentCaptor<String> captor;

    private HttpServerExchange exchange = PowerMockito.mock(HttpServerExchange.class);
    private MetricsHandler subject;

    @Before
    public void setup() {
        when(exchange.getStatusCode()).thenReturn(200);

        subject = new MetricsHandler(handler, statsd);
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

        verify(statsd).increment("requests.2xx.count");
        verify(statsd).recordExecutionTime(captor.capture(), anyLong());
        verify(statsd, never()).increment("requests.3xx.count");
        verify(statsd, never()).increment("requests.4xx.count");
        verify(statsd, never()).increment("requests.5xx.count");
        Assert.assertEquals("requests.2xx.times", captor.getValue());
    }

    @Test
    public void shouldSendMetricsFor3xx() {
        when(exchange.getStatusCode()).thenReturn(302);

        subject.exchangeEvent(exchange, nextListener, 0L);

        verify(statsd, never()).increment("requests.2xx.count");
        verify(statsd).recordExecutionTime(captor.capture(), anyLong());
        verify(statsd).increment("requests.3xx.count");
        verify(statsd, never()).increment("requests.4xx.count");
        verify(statsd, never()).increment("requests.5xx.count");
        Assert.assertEquals("requests.3xx.times", captor.getValue());
    }

    @Test
    public void shouldSendMetricsFor4xx() {
        when(exchange.getStatusCode()).thenReturn(404);

        subject.exchangeEvent(exchange, nextListener, 0L);

        verify(statsd, never()).increment("requests.2xx.count");
        verify(statsd).recordExecutionTime(captor.capture(), anyLong());
        verify(statsd, never()).increment("requests.3xx.count");
        verify(statsd).increment("requests.4xx.count");
        verify(statsd, never()).increment("requests.5xx.count");
        Assert.assertEquals("requests.4xx.times", captor.getValue());
    }

    @Test
    public void shouldSendMetricsFor5xx() {
        when(exchange.getStatusCode()).thenReturn(500);

        subject.exchangeEvent(exchange, nextListener, 0L);

        verify(statsd, never()).increment("requests.2xx.count");
        verify(statsd).recordExecutionTime(captor.capture(), anyLong());
        verify(statsd, never()).increment("requests.3xx.count");
        verify(statsd, never()).increment("requests.4xx.count");
        verify(statsd).increment("requests.5xx.count");
        Assert.assertEquals("requests.5xx.times", captor.getValue());
    }
}
