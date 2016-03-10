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
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ContextBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GelfLayoutTest {

    private static final String LOGGER_NAME = GelfLayoutTest.class.getCanonicalName();

    private GelfLayout layout = new GelfLayout();

    @Before
    public void before() {
        layout.setContext(new ContextBase());
        layout.setOriginHost("localhost");
    }

    @Test
    public void simple() throws IOException {
        layout.start();

        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger logger = lc.getLogger(LOGGER_NAME);

        final String logMsg = layout.doLayout(new LoggingEvent(
            LOGGER_NAME,
            logger,
            Level.DEBUG,
            "message {}",
            null,
            new Object[]{1})
        );

        final ObjectMapper om = new ObjectMapper();
        final JsonNode jsonNode = om.readTree(logMsg);
        basicValidation(jsonNode);
        assertNull(jsonNode.get("full_message"));
    }

    private void basicValidation(final JsonNode jsonNode) {
        assertEquals("1.1", jsonNode.get("version").textValue());
        assertEquals("localhost", jsonNode.get("host").textValue());
        assertEquals("message 1", jsonNode.get("short_message").textValue());
        assertEquals(7, jsonNode.get("level").intValue());
        assertEquals("main", jsonNode.get("_thread_name").textValue());
        assertEquals(LOGGER_NAME, jsonNode.get("_logger_name").textValue());
    }

    @Test
    public void exception() throws IOException {
        layout.start();

        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger logger = lc.getLogger(LOGGER_NAME);

        final String logMsg;
        try {
            throw new IllegalArgumentException("Example Exception");
        } catch (final IllegalArgumentException e) {
            logMsg = layout.doLayout(new LoggingEvent(
                LOGGER_NAME,
                logger,
                Level.DEBUG,
                "message {}",
                e,
                new Object[]{1})
            );
        }

        final ObjectMapper om = new ObjectMapper();
        final JsonNode jsonNode = om.readTree(logMsg);
        basicValidation(jsonNode);
        final String fullMessage = jsonNode.get("full_message").textValue();
        final String[] fullMessageLines = fullMessage.split("\n");

        assertTrue(fullMessageLines.length > 1);

        assertEquals("java.lang.IllegalArgumentException: Example Exception", fullMessageLines[0]);
        assertTrue(fullMessageLines[1].matches(
            "^\tat de.siegmar.logbackgelf.GelfLayoutTest.exception\\(GelfLayoutTest.java:\\d+\\) "
                + "~\\[test-classes/:na\\]$"));
    }

    @Test
    public void complex() throws IOException {
        layout.setIncludeRawMessage(true);
        layout.setIncludeLevelName(true);
        layout.addStaticField("foo:bar");
        layout.setIncludeCallerData(true);
        layout.start();

        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger logger = lc.getLogger(LOGGER_NAME);

        final LoggingEvent event = new LoggingEvent(
            LOGGER_NAME,
            logger,
            Level.DEBUG,
            "message {}",
            null,
            new Object[]{1});

        final Map<String, String> mdc = new HashMap<>();
        mdc.put("mdc_key", "mdc_value");
        event.setMDCPropertyMap(mdc);

        final String logMsg = layout.doLayout(event);

        final ObjectMapper om = new ObjectMapper();
        final JsonNode jsonNode = om.readTree(logMsg);
        basicValidation(jsonNode);
        assertEquals("DEBUG", jsonNode.get("_level_name").textValue());
        assertEquals("bar", jsonNode.get("_foo").textValue());
        assertEquals("mdc_value", jsonNode.get("_mdc_key").textValue());
        assertEquals("message {}", jsonNode.get("_raw_message").textValue());
    }

}
