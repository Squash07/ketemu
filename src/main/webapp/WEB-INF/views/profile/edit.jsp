<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Edit profile" active="profile">
  <div class="wrap auth">
    <div class="auth__text">
      <p><a href="${ctx}/profile">Back to your profile</a></p>
      <h1>Edit profile</h1>
      <p>Finders see your name and NRP on claims. Your email and phone appear on a claim ticket only after a claim is approved.</p>
    </div>
    <form class="panel form auth__form" method="post" action="${ctx}/profile/edit" novalidate>
      <k:csrf/>
      <div class="form__row">
        <k:penfield name="nrp" label="NRP" value="${form.nrp}" error="${errors.nrp}" maxlength="12" required="true"/>
        <k:penfield name="phone" label="Phone (optional)" type="tel" value="${form.phone}" error="${errors.phone}" maxlength="20" autocomplete="tel"/>
      </div>
      <k:penfield name="fullName" label="Full name" value="${form.fullName}" error="${errors.fullName}" maxlength="100" autocomplete="name" required="true"/>
      <k:penfield name="email" label="Email" type="email" value="${form.email}" error="${errors.email}" maxlength="120" autocomplete="email" required="true"/>
      <div class="actions">
        <button class="btn btn--primary" type="submit">Save profile</button>
        <a class="btn btn--quiet" href="${ctx}/profile">Cancel</a>
      </div>
    </form>
  </div>
</k:layout>
