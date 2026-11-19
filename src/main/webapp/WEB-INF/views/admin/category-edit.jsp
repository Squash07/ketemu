<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Edit category" active="admin">
  <div class="wrap auth">
    <div class="auth__text">
      <p><a href="${ctx}/admin/categories">Back to categories</a></p>
      <h1>Edit category</h1>
      <p>Renaming changes the label on every item in this category straight away.</p>
    </div>
    <form class="panel form auth__form" method="post" action="${ctx}/admin/categories/${form.id}/edit" novalidate>
      <k:csrf/>
      <k:penfield name="name" label="Name" value="${form.name}" error="${errors.name}" maxlength="40" required="true"/>
      <fieldset class="field${not empty errors.icon ? ' field--error' : ''}">
        <legend class="field__label">Icon</legend>
        <div class="icon-pick">
          <c:forEach var="ic" items="${icons}">
            <input type="radio" name="icon" id="icon-${ic}" value="${ic}" ${form.icon == ic ? 'checked' : ''}>
            <label for="icon-${ic}" title="${ic}"><k:icon name="${ic}" size="32"/><span class="visually-hidden">${ic}</span></label>
          </c:forEach>
        </div>
        <c:if test="${not empty errors.icon}"><p class="field__error">${errors.icon}</p></c:if>
      </fieldset>
      <div class="actions">
        <button class="btn btn--primary" type="submit">Save category</button>
        <a class="btn btn--quiet" href="${ctx}/admin/categories">Cancel</a>
      </div>
    </form>
  </div>
</k:layout>
