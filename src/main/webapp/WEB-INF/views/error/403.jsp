<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="${empty errorTitle ? 'Not your tag' : errorTitle}">
  <div class="wrap error-page">
    <div class="error-page__tag" aria-hidden="true"><span class="tag__eyelet"></span><span class="error-page__code">403</span></div>
    <div class="error-page__text">
      <h1><c:out value="${empty errorTitle ? 'That is behind the desk.' : errorTitle}"/></h1>
      <p><c:out value="${empty errorText ? 'You are signed in, but this page or action belongs to someone else.' : errorText}"/></p>
      <div class="actions">
        <a class="btn btn--primary" href="${ctx}/">Go to the board</a>
        <c:if test="${empty me}"><a class="btn btn--outline" href="${ctx}/login">Sign in</a></c:if>
      </div>
    </div>
  </div>
</k:layout>
