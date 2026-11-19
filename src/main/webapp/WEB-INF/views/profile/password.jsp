<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Change password" active="profile">
  <div class="wrap auth">
    <div class="auth__text">
      <p><a href="${ctx}/profile">Back to your profile</a></p>
      <h1>Change password</h1>
      <p>Passwords are stored as bcrypt hashes, so nobody at the desk can read yours.</p>
    </div>
    <form class="panel form auth__form" method="post" action="${ctx}/profile/password" novalidate>
      <k:csrf/>
      <k:penfield name="current" label="Current password" type="password" error="${errors.current}" autocomplete="current-password" required="true"/>
      <k:penfield name="password" label="New password" type="password" error="${errors.password}" hint="At least 8 characters." autocomplete="new-password" required="true"/>
      <k:penfield name="confirm" label="New password again" type="password" error="${errors.confirm}" autocomplete="new-password" required="true"/>
      <div class="actions">
        <button class="btn btn--primary" type="submit">Change password</button>
        <a class="btn btn--quiet" href="${ctx}/profile">Cancel</a>
      </div>
    </form>
  </div>
</k:layout>
