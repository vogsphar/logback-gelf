/*
 * Logback GELF - zero dependencies Logback GELF appender library.
 * Copyright (C) 2016 Oliver Siegmar
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.siegmar.logbackgelf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class GelfTcpAppender extends AbstractGelfAppender {

    private static final int DEFAULT_CONNECT_TIMEOUT = 15_000;
    private static final int DEFAULT_RECONNECT_INTERVAL = 300;
    private static final int DEFAULT_MAX_RETRIES = 2;
    private static final int DEFAULT_RETRY_DELAY = 3_000;
    private static final int SEC_TO_MSEC = 1000;

    private final Object lock = new Object();

    /**
     * Maximum time (in milliseconds) to wait for establishing a connection. A value of 0 disables
     * the connect timeout. Default: 15,000 milliseconds.
     */
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    /**
     * Time interval (in seconds) after an existing connection is closed and re-opened.
     * A value of 0 disables automatic reconnects. Default: 300 seconds.
     */
    private int reconnectInterval = DEFAULT_RECONNECT_INTERVAL;

    /**
     * Number of retries. A value of 0 disables retry attempts. Default: 2.
     */
    private int maxRetries = DEFAULT_MAX_RETRIES;

    /**
     * Time (in milliseconds) between retry attempts. Ignored if maxRetries is 0.
     * Default: 3,000 milliseconds.
     */
    private int retryDelay = DEFAULT_RETRY_DELAY;

    private OutputStream outputStream;

    /**
     * Timestamp scheduled for the next reconnect.
     */
    private long nextReconnect;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(final int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(final int retryDelay) {
        this.retryDelay = retryDelay;
    }

    @Override
    protected void appendMessage(final byte[] messageToSend) throws IOException {
        // GELF via TCP requires 0 termination
        final byte[] tcpMessage = Arrays.copyOf(messageToSend, messageToSend.length + 1);

        int openRetries = maxRetries;
        do {
            if (sendMessage(tcpMessage)) {
                // Message was sent successfully - we're done with it
                break;
            }

            if (retryDelay > 0 && openRetries > 0) {
                try {
                    Thread.sleep(retryDelay);
                } catch (final InterruptedException e) {
                    break;
                }
            }
        } while (openRetries-- > 0 && isStarted());
    }

    /**
     * Send message to socket's output stream.
     *
     * @param messageToSend message to send.
     *
     * @return {@code true} if message was sent successfully, {@code false} otherwise.
     */
    private boolean sendMessage(final byte[] messageToSend) {
        try {
            synchronized (lock) {
                if (System.currentTimeMillis() > nextReconnect) {
                    connect();
                }

                outputStream.write(messageToSend);
            }

            return true;
        } catch (final IOException e) {
            addError(String.format("Error sending message via tcp://%s:%s",
                getGraylogHost(), getGraylogPort()), e);

            // force reconnect int next loop cycle
            nextReconnect = 0;
        }

        return false;
    }

    /**
     * Opens a new connection (and closes the old one - if existent).
     *
     * @throws IOException if the connection failed.
     */
    private void connect() throws IOException {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (final IOException ignored) {
                // ignore
            }
        }
        final Socket socket = new Socket();
        socket.connect(new InetSocketAddress(getGraylogHost(), getGraylogPort()), connectTimeout);
        socket.shutdownInput();
        outputStream = socket.getOutputStream();

        nextReconnect = reconnectInterval < 0
            ? Long.MAX_VALUE
            : System.currentTimeMillis() + (reconnectInterval * SEC_TO_MSEC);
    }

    @Override
    protected void close() throws IOException {
        synchronized (lock) {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

}
