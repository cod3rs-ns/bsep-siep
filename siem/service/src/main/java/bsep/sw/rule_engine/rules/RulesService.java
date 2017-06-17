package bsep.sw.rule_engine.rules;


import bsep.sw.domain.*;
import bsep.sw.repositories.AlarmedLogsRepository;
import bsep.sw.repositories.LogsRepository;
import bsep.sw.services.AlarmDefinitionService;
import bsep.sw.services.AlarmService;
import bsep.sw.services.ProjectService;
import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RulesService {

    private final SimpMessagingTemplate template;
    private final AlarmService alarmService;
    private final ProjectService projectService;
    private final AlarmDefinitionService alarmDefinitionService;
    private final LogsRepository logsRepository;
    private final AlarmedLogsRepository alarmedLogsRepository;

    @Autowired
    public RulesService(final SimpMessagingTemplate template,
                        final AlarmService alarmService,
                        final ProjectService projectService,
                        final AlarmDefinitionService alarmDefinitionService,
                        final LogsRepository logsRepository,
                        final AlarmedLogsRepository alarmedLogsRepository) {
        this.template = template;
        this.alarmService = alarmService;
        this.projectService = projectService;
        this.alarmDefinitionService = alarmDefinitionService;
        this.logsRepository = logsRepository;
        this.alarmedLogsRepository = alarmedLogsRepository;
    }

    public void evaluateNewLog(final Log log) {
        final RulesEngine rulesEngine = RulesEngineBuilder
                .aNewRulesEngine()
                .named(UUID.randomUUID().toString())
                .withSkipOnFirstAppliedRule(false)
                .withSkipOnFirstNonTriggeredRule(false)
                .withSkipOnFirstFailedRule(false)
                .withSilentMode(true)
                .build();

        final Project project = projectService.findOne(log.getProject());

        // don't risk
        if (project == null) return;

        final List<AlarmDefinition> definitions = alarmDefinitionService.findAllByProject(project);

        for (final AlarmDefinition definition : definitions) {
            if (definition.getDefinitionType() == AlarmDefinitionType.SINGLE) {
                final SingleLogRule logRule = new SingleLogRule(log, definition, projectService, alarmService, alarmDefinitionService, template);
                rulesEngine.registerRule(logRule);
            } else {
                final ArrayList<Log> logs = gatherNonProcessedLogs(definition);
                if (logs.size() > 0) {
                    final MultiLogRule logRule = new MultiLogRule(logs, definition, projectService, alarmService, alarmDefinitionService, template, alarmedLogsRepository);
                    rulesEngine.registerRule(logRule);
                }
            }
        }
        rulesEngine.fireRules();
    }

    private ArrayList<Log> gatherNonProcessedLogs(final AlarmDefinition definition) {
        // retrieve all logs in interval
        final DateTime forwardLookup = DateTime.now().minus(definition.getMultiRule().getInterval() * 1000); // convert to millis
        final List<Log> allLogsInInterval = logsRepository.findAllByProjectAndTimeAfter(definition.getProject().getId(), forwardLookup);

        // retrieve all logs that triggered Alarm within same definition
        final List<AlarmedLogs> alreadyApplied = alarmedLogsRepository.findAllByAlarmDefinitionId(definition.getId());
        final ArrayList<String> logIds = new ArrayList<>();
        alreadyApplied.forEach(al -> logIds.add(al.getLogId()));

        // remove all logs that have triggered alarms
        final ArrayList<Log> toRemove = new ArrayList<>();
        allLogsInInterval.forEach(log -> {
            for (String logId : logIds) {
                if (logId.equals(log.getId())) {
                    toRemove.add(log);
                    break;
                }
            }
        });
        allLogsInInterval.removeAll(toRemove);
        return new ArrayList<>(allLogsInInterval);
    }
}