<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="error" required="false" %>
<%@ attribute name="hint" required="false" %>
<%@ attribute name="rows" required="false" %>
<%@ attribute name="maxlength" required="false" %>
<%@ attribute name="placeholder" required="false" %>
<%@ attribute name="autocomplete" required="false" %>
<%@ attribute name="required" required="false" type="java.lang.Boolean" %>
<%@ attribute name="id" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="fid" value="${empty id ? 'f-'.concat(name) : id}"/>
<div class="field${not empty error ? ' field--error' : ''}">
  <label class="field__label" for="${fid}">${label}</label>
  <c:choose>
    <c:when test="${type == 'textarea'}">
      <textarea class="pen" id="${fid}" name="${name}" rows="${empty rows ? 4 : rows}"<c:if test="${not empty maxlength}"> maxlength="${maxlength}"</c:if><c:if test="${not empty placeholder}"> placeholder="${placeholder}"</c:if><c:if test="${required}"> required</c:if><c:if test="${not empty error}"> aria-invalid="true" aria-describedby="${fid}-err"</c:if><c:if test="${empty error and not empty hint}"> aria-describedby="${fid}-hint"</c:if>><c:out value="${value}"/></textarea>
    </c:when>
    <c:otherwise>
      <input class="pen" id="${fid}" name="${name}" type="${empty type ? 'text' : type}" value="<c:out value='${value}'/>"<c:if test="${not empty maxlength}"> maxlength="${maxlength}"</c:if><c:if test="${not empty placeholder}"> placeholder="${placeholder}"</c:if><c:if test="${not empty autocomplete}"> autocomplete="${autocomplete}"</c:if><c:if test="${required}"> required</c:if><c:if test="${not empty error}"> aria-invalid="true" aria-describedby="${fid}-err"</c:if><c:if test="${empty error and not empty hint}"> aria-describedby="${fid}-hint"</c:if>>
    </c:otherwise>
  </c:choose>
  <c:if test="${not empty error}"><p class="field__error" id="${fid}-err"><c:out value="${error}"/></p></c:if>
  <c:if test="${empty error and not empty hint}"><p class="field__hint" id="${fid}-hint">${hint}</p></c:if>
</div>
