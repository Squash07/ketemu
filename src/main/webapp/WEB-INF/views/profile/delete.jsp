<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Delete account" active="profile">
  <div class="wrap auth">
    <div class="auth__text">
      <p><a href="${ctx}/profile">Back to your profile</a></p>
      <h1>Delete your account</h1>
      <p>This removes your account together with every item you posted, your claims, comments, watchlist and notifications. It cannot be undone.</p>
      <p>If you only want to take an item off the board, close that report instead.</p>
    </div>
    <form class="panel form auth__form auth__form--danger" method="post" action="${ctx}/profile/delete" novalidate>
      <k:csrf/>
      <k:penfield name="password" label="Your password" type="password" error="${errors.password}" hint="We ask again so nobody can delete your account from an unlocked laptop." autocomplete="current-password" required="true"/>
      <div class="actions">
        <button class="btn btn--danger" type="submit">Delete my account</button>
        <a class="btn btn--outline" href="${ctx}/profile">Keep my account</a>
      </div>
    </form>
  </div>
</k:layout>
