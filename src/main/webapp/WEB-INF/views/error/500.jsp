<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Something broke">
  <div class="wrap error-page">
    <div class="error-page__tag" aria-hidden="true"><span class="tag__eyelet"></span><span class="error-page__code">500</span></div>
    <div class="error-page__text">
      <h1>The desk dropped that one.</h1>
      <p>Something went wrong on our side while handling your request. Nothing you typed was saved. Wait a moment, then try again from the board.</p>
      <div class="actions">
        <a class="btn btn--primary" href="${ctx}/">Go to the board</a>
      </div>
    </div>
  </div>
</k:layout>
