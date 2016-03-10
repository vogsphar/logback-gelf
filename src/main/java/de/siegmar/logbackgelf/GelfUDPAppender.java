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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.zip.DeflaterOutputStream;

public class GelfUDPAppender extends AbstractGelfAppender {

    private DatagramChannel channel;
    private Integer maxChunkSize;
    private GelfUDPChunker chunker;
    private boolean useCompression = true;

    public Integer getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(final Integer maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public boolean isUseCompression() {
        return useCompression;
    }

    public void setUseCompression(final boolean useCompression) {
        this.useCompression = useCompression;
    }


    @Override
    protected void startAppender() throws IOException {
        channel = DatagramChannel.open();
        chunker = new GelfUDPChunker(maxChunkSize);
    }

    @Override
    protected void appendMessage(final byte[] binMessage) throws IOException {
        final byte[] messageToSend = useCompression ? compress(binMessage) : binMessage;

        final InetSocketAddress remote = new InetSocketAddress(getGraylogHost(), getGraylogPort());

        for (final ByteBuffer chunk : chunker.chunks(messageToSend)) {
            while (chunk.hasRemaining()) {
                channel.send(chunk, remote);
            }
        }
    }

    private static byte[] compress(final byte[] binMessage) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(binMessage.length);
        try {
            try (final OutputStream deflaterOut = new DeflaterOutputStream(bos)) {
                deflaterOut.write(binMessage);
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return bos.toByteArray();
    }

    @Override
    protected void close() throws IOException {
        channel.close();
    }

}
