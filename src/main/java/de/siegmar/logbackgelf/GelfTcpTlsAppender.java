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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class GelfTcpTlsAppender extends GelfTcpAppender {

    /**
     * If {@code true}, trust all TLS certificates (even self signed certificates).
     */
    private boolean trustAllCertificates;

    /**
     * Socket factory used for creating new sockets.
     */
    private SSLSocketFactory socketFactory;

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(final boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    @Override
    protected void startAppender() throws IOException {
        super.startAppender();

        try {
            socketFactory = initSocketFactory();
        } catch (final KeyManagementException | NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    private SSLSocketFactory initSocketFactory()
        throws KeyManagementException, NoSuchAlgorithmException {
        if (trustAllCertificates) {
            addWarn("Enable trustAllCertificates - don't use this in production!");
            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, buildNoopTrustManagers(), new SecureRandom());
            return context.getSocketFactory();
        }

        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    private static TrustManager[] buildNoopTrustManagers() {
        return new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(final X509Certificate[] chain,
                                               final String authType) {
                }

                public void checkServerTrusted(final X509Certificate[] chain,
                                               final String authType) {
                }
            },
        };
    }

    @Override
    protected Socket getSocket() throws IOException {
        final SSLSocket socket = (SSLSocket) socketFactory.createSocket();
        socket.connect(new InetSocketAddress(getGraylogHost(), getGraylogPort()),
            getConnectTimeout());
        return socket;
    }

}
