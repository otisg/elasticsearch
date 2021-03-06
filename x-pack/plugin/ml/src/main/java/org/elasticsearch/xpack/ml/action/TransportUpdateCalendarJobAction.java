/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.action;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.ml.action.PutCalendarAction;
import org.elasticsearch.xpack.core.ml.action.UpdateCalendarJobAction;
import org.elasticsearch.xpack.ml.job.JobManager;
import org.elasticsearch.xpack.ml.job.persistence.JobProvider;

import java.util.Set;

public class TransportUpdateCalendarJobAction extends HandledTransportAction<UpdateCalendarJobAction.Request, PutCalendarAction.Response> {

    private final JobProvider jobProvider;
    private final JobManager jobManager;

    @Inject
    public TransportUpdateCalendarJobAction(Settings settings, TransportService transportService,
                                            ActionFilters actionFilters, JobProvider jobProvider, JobManager jobManager) {
        super(settings, UpdateCalendarJobAction.NAME, transportService, actionFilters, UpdateCalendarJobAction.Request::new);
        this.jobProvider = jobProvider;
        this.jobManager = jobManager;
    }

    @Override
    protected void doExecute(Task task, UpdateCalendarJobAction.Request request, ActionListener<PutCalendarAction.Response> listener) {
        Set<String> jobIdsToAdd = Strings.tokenizeByCommaToSet(request.getJobIdsToAddExpression());
        Set<String> jobIdsToRemove = Strings.tokenizeByCommaToSet(request.getJobIdsToRemoveExpression());

        jobProvider.updateCalendar(request.getCalendarId(), jobIdsToAdd, jobIdsToRemove,
                c -> {
                    jobManager.updateProcessOnCalendarChanged(c.getJobIds());
                    listener.onResponse(new PutCalendarAction.Response(c));
                }, listener::onFailure);
    }
}
