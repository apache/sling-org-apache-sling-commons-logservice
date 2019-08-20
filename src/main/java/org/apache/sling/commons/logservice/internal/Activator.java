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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The <code>Activator</code> class is the <code>BundleActivator</code> for the
 * log service bundle. This activator registers the <code>LogService</code> and
 * <code>LogReaderService</code>.
 */
public class Activator implements BundleActivator {

    private volatile ServiceTracker<LogReaderService, LogReaderService> logReaderTracker;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        final SLF4JSupport listener = new SLF4JSupport();
        this.logReaderTracker = new ServiceTracker<>(context, LogReaderService.class,
                new ServiceTrackerCustomizer<LogReaderService, LogReaderService>() {

                    @Override
                    public LogReaderService addingService(final ServiceReference<LogReaderService> reference) {
                        final LogReaderService srvc = context.getService(reference);
                        if (srvc != null) {
                            srvc.addLogListener(listener);
                            listener.replay(srvc.getLog());
                        }
                        return srvc;
                    }

                    @Override
                    public void modifiedService(final ServiceReference<LogReaderService> reference,
                            final LogReaderService service) {
                        // nothing to do
                    }

                    @Override
                    public void removedService(final ServiceReference<LogReaderService> reference,
                            final LogReaderService service) {
                        service.removeLogListener(listener);

                    }
                });
        this.logReaderTracker.open();
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        if (this.logReaderTracker != null) {
            this.logReaderTracker.close();
            this.logReaderTracker = null;
        }
    }
}
