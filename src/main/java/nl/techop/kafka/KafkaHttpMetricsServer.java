
/*
 * *
 *  * Copyright 2014, arnobroekhof@gmail.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package nl.techop.kafka;


import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Class KafkaHttpMetricsServer
 * Author: arnobroekhof
 * Purpose: Class for starting a Embedded Jetty server with the codahale metrics servlets loaded
 * Interfaces: None
 */
public class KafkaHttpMetricsServer {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaHttpMetricsServer.class);
  private Server server;
  private int port;
  private String bindAddress;

  /**
   * Method: KafkaHttpMetricsServer
   * Purpose: Method for constructing the the metrics server.
   *
   * @param bindAddress the name or address to bind on ( defaults to localhost )
   * @param port            the port to bind on ( defaults to 8080 )
   */
  public KafkaHttpMetricsServer(String bindAddress, int port) {

    this.port = port;
    this.bindAddress = bindAddress;

    // call init
    init();
  }

  private static class IndexServlet extends HttpServlet {
    private static final String CONTENT = "For metrics please click <a href = \"./metrics?pretty=true\"> here</a>. " +
            "for threads please click <a href = \"./threads\"> here</a>.";

    public IndexServlet() {
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      resp.setStatus(200);
      resp.setContentType("text/html");
      resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

      try (ServletOutputStream output = resp.getOutputStream()) {
        output.write(CONTENT.getBytes(StandardCharsets.UTF_8));
      }
    }
  }

  /**
   * Method: init
   * Purpose: Initializes the embedded Jetty Server with including the metrics servlets.
   */
  private void init() {
    LOG.info("Initializing Kafka Http Metrics Reporter");

    // creating the socket address for binding to the specified address and port
    InetSocketAddress inetSocketAddress = new InetSocketAddress(bindAddress, port);

    // create new Jetty server
    server = new Server(inetSocketAddress);

    // creating the servlet context handler
    ServletContextHandler servletContextHandler = new ServletContextHandler();

    // setting the context path
    servletContextHandler.setContextPath("/");

    // adding the codahale metrics servlet to the servlet context
    servletContextHandler.addServlet(new ServletHolder(new IndexServlet()), "/");
    servletContextHandler.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
    servletContextHandler.addServlet(new ServletHolder(new ThreadDumpServlet()), "/threads");

    // adding the configured servlet context handler to the Jetty Server
    server.setHandler(servletContextHandler);
    LOG.info("Finished initializing Kafka Http Metrics Reporter");
  }

  /**
   * Method: start
   * Purpose: starting the metrics server
   */
  public void start() {
    try {
      LOG.info("Starting Kafka Http Metrics Reporter");

      // starting the Jetty Server
      server.start();
      LOG.info("Started Kafka Http Metrics Reporter on: {}:{}", bindAddress, port);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Method: stop
   * Purpose: Stopping the metrics server
   */
  public void stop() {
    try {
      LOG.info("Stopping Kafka Http Metrics Reporter");

      // stopping the Jetty Server
      server.stop();
      LOG.info("Kafka Http Metrics Reporter stopped");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
