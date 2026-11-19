<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Edit location" active="admin">
  <div class="wrap auth">
    <div class="auth__text">
      <p><a href="${ctx}/admin/locations">Back to locations</a></p>
      <h1>Edit location</h1>
      <p>Turning off the SKK post option removes it from the pickup list. Items it already holds keep showing it.</p>
    </div>
    <form class="panel form auth__form" method="post" action="${ctx}/admin/locations/${form.id}/edit" novalidate>
      <k:csrf/>
      <k:penfield name="name" label="Name" value="${form.name}" error="${errors.name}" maxlength="80" required="true"/>
      <k:penfield name="zone" label="Zone (optional)" value="${form.zone}" error="${errors.zone}" maxlength="40"/>
      <label class="check"><input type="checkbox" name="skkPost" value="1" ${form.skkPost ? 'checked' : ''}> This is an SKK post</label>
      <div class="actions">
        <button class="btn btn--primary" type="submit">Save location</button>
        <a class="btn btn--quiet" href="${ctx}/admin/locations">Cancel</a>
      </div>
    </form>
  </div>
</k:layout>
