/*
 * Logback GELF - zero dependencies Logback GELF appender library.
 * Copyright (C) 2018 Oliver Siegmar
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

class AddressResolver {

    private final String hostname;
    private final AtomicInteger cnt;

    AddressResolver(final String hostname) {
        this(hostname, new AtomicInteger());
    }

    AddressResolver(final String hostname, final AtomicInteger cnt) {
        this.hostname = hostname;
        this.cnt = cnt;
    }

    public InetAddress resolve() throws UnknownHostException {
        final InetAddress[] ips = lookup();
        return ips[modulo(cnt.getAndIncrement(), ips.length)];
    }

    protected InetAddress[] lookup() throws UnknownHostException {
        return InetAddress.getAllByName(hostname);
    }

    private static int modulo(final int value, final int modulo) {
        return ((value % modulo) + modulo) % modulo;
    }

}
