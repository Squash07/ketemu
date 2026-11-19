<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Create account" active="register">
  <div class="wrap auth">
    <div class="auth__text">
      <h1>Create an account</h1>
      <p>Your NRP and name are shown to a finder when you claim their item, so they know who is coming to the post. Your email and phone are shared only after a claim is approved.</p>
      <p>Already registered? <a href="${ctx}/login">Sign in</a>.</p>
    </div>
    <form class="panel form auth__form" method="post" action="${ctx}/register" novalidate>
      <k:csrf/>
      <div class="form__row">
        <k:penfield name="nrp" label="NRP" value="${form.nrp}" error="${errors.nrp}" maxlength="12" autocomplete="username" required="true"/>
        <k:penfield name="phone" label="Phone (optional)" type="tel" value="${form.phone}" error="${errors.phone}" maxlength="20" autocomplete="tel"/>
      </div>
      <k:penfield name="fullName" label="Full name" value="${form.fullName}" error="${errors.fullName}" maxlength="100" autocomplete="name" required="true"/>
      <k:penfield name="email" label="Email" type="email" value="${form.email}" error="${errors.email}" maxlength="120" autocomplete="email" required="true"/>
      <div class="form__row">
        <k:penfield name="password" label="Password" type="password" error="${errors.password}" hint="At least 8 characters." autocomplete="new-password" required="true"/>
        <k:penfield name="confirm" label="Password again" type="password" error="${errors.confirm}" autocomplete="new-password" required="true"/>
      </div>
      <div class="actions">
        <button class="btn btn--primary" type="submit">Create account</button>
      </div>
    </form>
  </div>
</k:layout>
