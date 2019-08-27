package org.gooru.nucleus.handlers.courses.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.courses.constants.MessageConstants;

/**
 * Created by ashish on 7/1/16.
 */
public class ProcessorContext {

  private final String userId;
  private final JsonObject session;
  private final JsonObject request;
  private final MultiMap requestHeaders;
  private final String courseId;
  private final String unitId;
  private final String lessonId;
  private final String collectionId;
  private final String lessonPlanId;
  private final TenantContext tenantContext;
  private final String milestoneId;
  private final String fwCode;

  public ProcessorContext(String userId, JsonObject session, JsonObject request, String courseId,
      String unitId, String lessonId, String collectionId, MultiMap headers) {
    if (session == null || userId == null || session.isEmpty() || headers == null || headers
        .isEmpty()) {
      throw new IllegalStateException(
          "Processor Context creation failed because of invalid values");
    }
    this.userId = userId;
    this.session = session.copy();
    this.request = request != null ? request.copy() : null;
    this.requestHeaders = headers;
    this.courseId = courseId;
    this.unitId = unitId;
    this.lessonId = lessonId;
    this.collectionId = collectionId;
    this.tenantContext = new TenantContext(session);
    this.milestoneId = headers.get(MessageConstants.MILESTONE_ID);
    this.fwCode = headers.get(MessageConstants.FW_CODE);
    this.lessonPlanId = headers.get(MessageConstants.LESSON_PLAN_ID);

  }

  public String userId() {
    return this.userId;
  }

  public JsonObject session() {
    return this.session.copy();
  }

  public JsonObject request() {
    return this.request;
  }

  public String courseId() {
    return this.courseId;
  }

  public String unitId() {
    return this.unitId;
  }

  public MultiMap requestHeaders() {
    return requestHeaders;
  }

  public String lessonId() {
    return this.lessonId;
  }

  public String collectionId() {
    return this.collectionId;
  }

  public String tenant() {
    return this.tenantContext.tenant();
  }

  public String tenantRoot() {
    return this.tenantContext.tenantRoot();
  }

  public String milestoneId() {
    return milestoneId;
  }
  
  public String lessonPlanId() {
    return this.lessonPlanId;
  }

  public String frameworkCode() {
    return fwCode;
  }

  private static class TenantContext {

    private static final String TENANT = "tenant";
    private static final String TENANT_ID = "tenant_id";
    private static final String TENANT_ROOT = "tenant_root";

    private final String tenantId;
    private final String tenantRoot;

    TenantContext(JsonObject session) {
      JsonObject tenantJson = session.getJsonObject(TENANT);
      if (tenantJson == null || tenantJson.isEmpty()) {
        throw new IllegalStateException("Tenant Context invalid");
      }
      this.tenantId = tenantJson.getString(TENANT_ID);
      if (tenantId == null || tenantId.isEmpty()) {
        throw new IllegalStateException("Tenant Context with invalid tenant");
      }
      this.tenantRoot = tenantJson.getString(TENANT_ROOT);
    }

    public String tenant() {
      return this.tenantId;
    }

    public String tenantRoot() {
      return this.tenantRoot;
    }
  }

}
