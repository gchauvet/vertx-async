/*
 * The MIT License
 *
 * Copyright 2016 Guillaume Chauvet.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.zatarox.vertx.async.fakes;

import io.netty.channel.EventLoopGroup;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.TimeoutStream;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.dns.DnsClient;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.shareddata.SharedData;
import io.vertx.core.spi.VerticleFactory;
import java.util.Set;

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
    public FileSystem fileSystem() {
        return null;
    }

    @Override
    public EventBus eventBus() {
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
    public void runOnContext(Handler<Void> handler) {
        handler.handle(null);
    }

    @Override
    public Context getOrCreateContext() {
        return null;
    }

    @Override
    public NetServer createNetServer(NetServerOptions options) {
        return null;
    }

    @Override
    public NetClient createNetClient(NetClientOptions options) {
        return null;
    }

    @Override
    public HttpServer createHttpServer(HttpServerOptions options) {
        return null;
    }

    @Override
    public HttpClient createHttpClient(HttpClientOptions options) {
        return null;
    }

    @Override
    public DatagramSocket createDatagramSocket(DatagramSocketOptions options) {
        return null;
    }

    @Override
    public DatagramSocket createDatagramSocket() {
        return null;
    }

    @Override
    public DnsClient createDnsClient(int port, String host) {
        return null;
    }

    @Override
    public TimeoutStream timerStream(long delay) {
        return null;
    }

    @Override
    public TimeoutStream periodicStream(long delay) {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
    }

    @Override
    public void deployVerticle(Verticle verticle) {
    }

    @Override
    public void deployVerticle(Verticle verticle, Handler<AsyncResult<String>> handler) {
        handler.handle(null);
    }

    @Override
    public void deployVerticle(Verticle verticle, DeploymentOptions options) {
    }

    @Override
    public void deployVerticle(Verticle verticle, DeploymentOptions options, Handler<AsyncResult<String>> handler) {
        handler.handle(null);
    }

    @Override
    public void deployVerticle(String name) {
    }

    @Override
    public void deployVerticle(String name, Handler<AsyncResult<String>> handler) {
        handler.handle(null);
    }

    @Override
    public void deployVerticle(String name, DeploymentOptions options) {
    }

    @Override
    public void deployVerticle(String name, DeploymentOptions options, Handler<AsyncResult<String>> handler) {
        handler.handle(null);
    }

    @Override
    public void undeploy(String deploymentID) {
    }

    @Override
    public void undeploy(String deploymentID, Handler<AsyncResult<Void>> handler) {
        handler.handle(null);
    }

    @Override
    public Set<String> deploymentIDs() {
        return null;
    }

    @Override
    public void registerVerticleFactory(VerticleFactory factory) {
    }

    @Override
    public void unregisterVerticleFactory(VerticleFactory factory) {
    }

    @Override
    public Set<VerticleFactory> verticleFactories() {
        return null;
    }

    @Override
    public boolean isClustered() {
        return false;
    }

    @Override
    public <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler, boolean ordered, Handler<AsyncResult<T>> handler) {
        handler.handle(null);
    }

    @Override
    public <T> void executeBlocking(Handler<Future<T>> blockingCodeHandler, Handler<AsyncResult<T>> handler) {
        handler.handle(null);
    }

    @Override
    public EventLoopGroup nettyEventLoopGroup() {
        return null;
    }

    @Override
    public boolean isMetricsEnabled() {
        return false;
    }

}
