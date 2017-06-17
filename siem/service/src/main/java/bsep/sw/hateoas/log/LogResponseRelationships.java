package bsep.sw.hateoas.log;

import bsep.sw.domain.Log;
import bsep.sw.hateoas.relationships.RelationshipData;
import bsep.sw.hateoas.relationships.RelationshipLinks;
import bsep.sw.hateoas.relationships.ResponseRelationship;

import static bsep.sw.hateoas.ResourceTypes.PROJECTS_TYPE;

public class LogResponseRelationships {

    public ResponseRelationship project;

    public static LogResponseRelationships fromDomain(final Log log) {
        final RelationshipData data = new RelationshipData(PROJECTS_TYPE, log.getProject().toString());
        // FIXME
        final RelationshipLinks links = new RelationshipLinks( "related-link");

        return new LogResponseRelationships()
                .project(new ResponseRelationship(links, data));
    }

    public LogResponseRelationships project(final ResponseRelationship project) {
        this.project = project;
        return this;
    }

}