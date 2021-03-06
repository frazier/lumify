package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityObjectDetectionUpdate extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final EntityHelper entityHelper;
    private final AuditRepository auditRepository;

    @Inject
    public EntityObjectDetectionUpdate(
            final GraphRepository graphRepository,
            final EntityHelper entityHelper,
            final AuditRepository auditRepository) {
        this.graphRepository = graphRepository;
        this.entityHelper = entityHelper;
        this.auditRepository = auditRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        final String artifactId = getRequiredParameter(request, "artifactId");
        final String sign = getRequiredParameter(request, "sign");
        final String conceptId = getRequiredParameter(request, "conceptId");
        String resolvedGraphVertexId = getOptionalParameter(request, "graphVertexId");
        String existing = getOptionalParameter(request, "existing");
        String x1 = getRequiredParameter(request, "x1"), x2 = getRequiredParameter(request, "x2"),
                y1 = getRequiredParameter(request, "y1"), y2 = getRequiredParameter(request, "y2");
        final String boundingBox = "x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2;

        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
        GraphVertex resolvedVertex;
        if (resolvedGraphVertexId != null) {
            resolvedVertex = graphRepository.findVertex(resolvedGraphVertexId, user);
        } else {
            // TODO: replace second "" when we implement commenting on ui
            resolvedVertex = entityHelper.createGraphVertex(conceptVertex, sign, existing,"", "", artifactId, user);
            resolvedGraphVertexId = resolvedVertex.getId();
        }
        GraphVertex artifactVertex = graphRepository.findVertex(artifactId, user);

        // update graph vertex
        // TODO: replace second "" when we implement commenting on ui
        entityHelper.updateGraphVertex(resolvedVertex, conceptId, sign, "", "", user);

        // update the detected object property on the artifact
        JSONArray detectedObjects = new JSONArray(artifactVertex.getProperty(PropertyName.DETECTED_OBJECTS).toString());
        for (int i = 0; i < detectedObjects.length(); i++) {
            JSONObject detectedObject = detectedObjects.getJSONObject(i);
            String oldCoordinates = "x1: " + detectedObject.get("x1") + ", y1: " + detectedObject.get("y1") +
                    ", x2: " + detectedObject.get("x2") + ", y2: " + detectedObject.get("y2");
            if (detectedObject.has("graphVertexId") && detectedObject.get("graphVertexId").equals(resolvedGraphVertexId) ||
                    (oldCoordinates.equals(boundingBox))) {
                ArtifactDetectedObject tag = entityHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);
                JSONObject result = new JSONObject();

                JSONObject entityTag = tag.getJson();
                entityTag.put("artifactId", artifactId);
                detectedObjects.put(i, entityTag);

                artifactVertex.setProperty(PropertyName.DETECTED_OBJECTS, detectedObjects.toString());
                result.put("entityVertex", entityTag);
                graphRepository.save(artifactVertex, user);

                // TODO: replace "" when we implement commenting on ui
                auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), artifactVertex, PropertyName.DETECTED_OBJECTS.toString(), "", "", user);

                JSONObject updatedArtifactVertex = entityHelper.formatUpdatedArtifactVertexProperty(artifactId, PropertyName.DETECTED_OBJECTS.toString(), artifactVertex.getProperty(PropertyName.DETECTED_OBJECTS));
                result.put("updatedArtifactVertex", updatedArtifactVertex);

                respondWithJson(response, result);
                break;
            }
        }
    }
}