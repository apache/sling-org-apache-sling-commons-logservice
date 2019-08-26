/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.commons.logservice.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code SLF4JSupport} uses slf4j API to output all log entries.
 */
public class SLF4JSupport implements LogListener {

    /**
     * The service property name of the component name (value is "component.name").
     * Note: We use a private constant here to not create a unneeded dependency on
     * the org.osgi.service.component package.
     */
    private static final String COMPONENT_NAME = ComponentConstants.COMPONENT_NAME; // "component.name";

    private static final String LOGGER_EVENT_FRAMEWORK = "Events.Framework";
    private static final String LOGGER_EVENT_BUNDLE = "Events.Bundle";
    private static final String LOGGER_EVENT_SERVICE = "Events.Service";
    private static final String LOGGER_EVENT_LOG_SERVICE = "LogService";

    private static final String SL_MARKER = "STARTLEVEL CHANGED";

    /** Framework start level. */
    private final FrameworkStartLevel startLevel;

    private final List<String> logServiceLoggers = Arrays.asList(LOGGER_EVENT_FRAMEWORK, LOGGER_EVENT_BUNDLE,
            LOGGER_EVENT_SERVICE, LOGGER_EVENT_LOG_SERVICE);

    public SLF4JSupport(final FrameworkStartLevel startLevel) {
        this.startLevel = startLevel;
    }

    @Override
    public void logged(final LogEntry logEntry) {
        doLog(logEntry);
    }

    private void doLog(final LogEntry logEntry) {
        // get the logger for the bundle
        final boolean isLogService = logServiceLoggers.contains(logEntry.getLoggerName());
        final Logger logger = LoggerFactory
                .getLogger(isLogService ? getLoggerName(logEntry.getBundle()) : logEntry.getLoggerName());
        if (!isEnabled(logger, logEntry)) {
            // early exit, this message will not be logged, don't do any work...
            return;
        }
        logOut(logger, logEntry);
    }

    private String getLoggerName(final Bundle bundle) {
        String name;
        if (bundle == null) {

            // if we have no bundle, use the system bundle's name
            name = Constants.SYSTEM_BUNDLE_SYMBOLICNAME;

        } else {

            // otherwise use the bundle symbolic name
            name = bundle.getSymbolicName();

            // if the bundle has no symbolic name, use the location
            if (name == null) {
                name = bundle.getLocation();
            }

            // if the bundle also has no location, use the bundle Id
            if (name == null) {
                name = String.valueOf(bundle.getBundleId());
            }
        }

        return name;
    }

    /**
     * Actually logs the given log entry to the logger
     */
    private synchronized void logOut(final Logger logger, final LogEntry logEntry) {

        final StringBuilder msg = new StringBuilder();

        ServiceReference<?> sr = logEntry.getServiceReference();
        if (sr != null) {
            msg.append("Service [");
            if (sr.getProperty(Constants.SERVICE_PID) != null) {
                msg.append(sr.getProperty(Constants.SERVICE_PID)).append(',');
            } else if (sr.getProperty(COMPONENT_NAME) != null) {
                msg.append(sr.getProperty(COMPONENT_NAME)).append(',');
            } else if (sr.getProperty(Constants.SERVICE_DESCRIPTION) != null) {
                msg.append(sr.getProperty(Constants.SERVICE_DESCRIPTION)).append(
                    ',');
            }
            msg.append(sr.getProperty(Constants.SERVICE_ID))
                .append(", ")
                .append(Arrays.toString((String[]) sr.getProperty(Constants.OBJECTCLASS)))
                .append("] ");
        }

        if (logEntry.getMessage() != null) {
            msg.append(logEntry.getMessage());
            if (LOGGER_EVENT_FRAMEWORK.equals(logEntry.getLoggerName()) && logEntry.getMessage().contains(SL_MARKER)) {
                msg.append(" to ");
                msg.append(String.valueOf(this.startLevel.getStartLevel()));
            }
        }

        Throwable exception = logEntry.getException();
        if (exception != null) {
            msg.append(" (").append(exception).append(')');
        }

        String message = msg.toString();
        switch (logEntry.getLogLevel()) {
        case DEBUG:
            logger.debug(message, exception);
                break;
        case INFO:
            logger.info(message, exception);
                break;
        case WARN:
            logger.warn(message, exception);
                break;
        case ERROR:
            logger.error(message, exception);
                break;
        case TRACE:
                logger.trace(message, exception);
                break;
        case AUDIT: // we treat audit as trace
            logger.trace(message, exception);
            break;
        }
    }

    static boolean isEnabled(final Logger logger, final LogEntry logEntry) {
        switch (logEntry.getLogLevel()) {
        case DEBUG:
            return logger.isDebugEnabled();
        case INFO:
            return logger.isInfoEnabled();
        case WARN:
            return logger.isWarnEnabled();
        case ERROR:
            return logger.isErrorEnabled();
        case TRACE:
            return logger.isTraceEnabled();
        case AUDIT: // we treat audit as trace
            return logger.isTraceEnabled();
        }
        return false;
    }

    public synchronized void replay(final Enumeration<LogEntry> log) {
        // the enumeration contains the most recent entries first
        final List<LogEntry> entries = new ArrayList<>(Collections.list(log));
        Collections.reverse(entries);
        for (final LogEntry entry : entries) {
            doLog(entry);
        }
    }
}
