package org.simondean.vertx.async.unit.fakes;

import org.vertx.java.core.Context;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.datagram.DatagramSocket;
import org.vertx.java.core.datagram.InternetProtocolFamily;
import org.vertx.java.core.dns.DnsClient;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.shareddata.SharedData;
import org.vertx.java.core.sockjs.SockJSServer;

import java.net.InetSocketAddress;

public class FakeVertx implements Vertx {
  @Override
  public NetServer createNetServer() {
    return null;
  }

  @Override
  public NetClient createNetClient() {
    return null;
  }

  @Override
  public HttpServer createHttpServer() {
    return null;
  }

  @Override
  public HttpClient createHttpClient() {
    return null;
  }

  @Override
  public DatagramSocket createDatagramSocket(InternetProtocolFamily internetProtocolFamily) {
    return null;
  }

  @Override
  public SockJSServer createSockJSServer(HttpServer httpServer) {
    return null;
  }

  @Override
  public FileSystem fileSystem() {
    return null;
  }

  @Override
  public EventBus eventBus() {
    return null;
  }

  @Override
  public DnsClient createDnsClient(InetSocketAddress... inetSocketAddresses) {
    return null;
  }

  @Override
  public SharedData sharedData() {
    return null;
  }

  @Override
  public long setTimer(long l, Handler<Long> handler) {
    return 0;
  }

  @Override
  public long setPeriodic(long l, Handler<Long> handler) {
    return 0;
  }

  @Override
  public boolean cancelTimer(long l) {
    return false;
  }

  @Override
  public Context currentContext() {
    return null;
  }

  @Override
  public void runOnContext(Handler<Void> handler) {
    handler.handle(null);
  }

  @Override
  public boolean isEventLoop() {
    return false;
  }

  @Override
  public boolean isWorker() {
    return false;
  }

  @Override
  public void stop() {

  }
}
