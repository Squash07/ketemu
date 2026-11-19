<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Sign in" active="login">
  <div class="wrap auth">
    <div class="auth__text">
      <h1>Sign in</h1>
      <p>Use the email you registered with. You need an account to post items, file claims and save tags to your watchlist.</p>
      <p>New here? <a href="${ctx}/register">Create an account</a>. It takes a minute.</p>
    </div>
    <form class="panel form auth__form" method="post" action="${ctx}/login" novalidate>
      <k:csrf/>
      <input type="hidden" name="next" value="<c:out value='${next}'/>">
      <k:penfield name="email" label="Email" type="email" value="${email}" error="${errors.email}" autocomplete="email" required="true"/>
      <k:penfield name="password" label="Password" type="password" error="${errors.password}" autocomplete="current-password" required="true"/>
      <div class="actions">
        <button class="btn btn--primary" type="submit">Sign in</button>
        <a class="btn btn--quiet" href="${ctx}/register">Create account</a>
      </div>
    </form>
  </div>
</k:layout>
