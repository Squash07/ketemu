<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="No tag with that code">
  <div class="wrap error-page">
    <div class="error-page__tag" aria-hidden="true"><span class="tag__eyelet"></span><span class="error-page__code">404</span></div>
    <div class="error-page__text">
      <h1>No tag with that code.</h1>
      <p>It may have been returned or removed, or the link has a typo. The board has everything that is still waiting.</p>
      <div class="actions">
        <a class="btn btn--primary" href="${ctx}/">Go to the board</a>
        <a class="btn btn--outline" href="${ctx}/items/new?kind=lost">Post a lost item</a>
      </div>
    </div>
  </div>
</k:layout>
