package bsep.sw.services;

import bsep.sw.domain.AlarmDefinition;
import bsep.sw.domain.Project;
import bsep.sw.repositories.AlarmDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlarmDefinitionService {

    private final AlarmDefinitionRepository repository;

    @Autowired
    public AlarmDefinitionService(final AlarmDefinitionRepository repository) {
        this.repository = repository;
    }

    public AlarmDefinition save(final AlarmDefinition alarmDefinition) {
        return repository.save(alarmDefinition);
    }

    @Transactional(readOnly = true)
    public Page<AlarmDefinition> findAllByProject(final Project project, final Pageable pageable) {
        return repository.findAlarmDefinitionsByProject(project, pageable);
    }

    @Transactional(readOnly = true)
    public List<AlarmDefinition> findAllByProject(final Project project) {
        return repository.findAlarmDefinitionsByProject(project);
    }

    @Transactional(readOnly = true)
    public AlarmDefinition findByProjectAndId(final Project project, final Long definitionId) {
        return repository.findAlarmDefinitionByIdAndProject(definitionId, project);
    }

    @Transactional(readOnly = true)
    public AlarmDefinition findOne(final Long id) {
        return repository.findOne(id);
    }

    public void delete(final Long id) {
        repository.delete(id);
    }

}
