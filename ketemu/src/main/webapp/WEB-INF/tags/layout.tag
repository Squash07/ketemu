<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="active" required="false" %>
<%@ attribute name="wide" required="false" type="java.lang.Boolean" %>
<%@ attribute name="description" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${title}"/> | Ketemu</title>
  <meta name="description" content="${empty description ? 'Ketemu is the lost-property board for ITS. Post what you lost or found, prove an item is yours, and pick it up at an SKK post.' : description}">
  <meta name="theme-color" content="#EEF1EC">
  <link rel="icon" href="${ctx}/assets/img/favicon.svg" type="image/svg+xml">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Archivo:wdth,wght@62..125,100..900&family=Shantell+Sans:wght@400;500;600;700&display=swap">
  <link rel="stylesheet" href="${ctx}/assets/css/tokens.css">
  <link rel="stylesheet" href="${ctx}/assets/css/base.css">
  <link rel="stylesheet" href="${ctx}/assets/css/components.css">
  <link rel="stylesheet" href="${ctx}/assets/css/pages.css">
  <script src="${ctx}/assets/js/app.js" defer></script>
</head>
<body>
  <a class="skip" href="#main">Skip to content</a>
  <header class="site-header">
    <nav class="nav wrap" aria-label="Main">
      <a class="brand" href="${ctx}/">
        <span class="brand__tag" aria-hidden="true"><span></span></span>
        <span class="brand__name">Ketemu</span>
      </a>
      <ul class="nav__links">
        <li><a href="${ctx}/" ${active == 'board' ? 'aria-current="page"' : ''}>Board</a></li>
        <li><a href="${ctx}/items/new" ${active == 'report' ? 'aria-current="page"' : ''}>Report an item</a></li>
        <c:if test="${not empty me}">
          <li><a href="${ctx}/claims" ${active == 'claims' ? 'aria-current="page"' : ''}>My claims</a></li>
          <li><a href="${ctx}/watchlist" ${active == 'watchlist' ? 'aria-current="page"' : ''}>Watchlist</a></li>
          <c:if test="${me.admin}">
            <li><a href="${ctx}/admin" ${active == 'admin' ? 'aria-current="page"' : ''}>Desk</a></li>
          </c:if>
        </c:if>
      </ul>
      <div class="nav__account">
        <c:choose>
          <c:when test="${not empty me}">
            <a class="bell" href="${ctx}/notifications" ${active == 'notifications' ? 'aria-current="page"' : ''}>
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 16.5 h12 l-1.6 -2.2 v-3.8 a4.4 4.4 0 0 0 -8.8 0 v3.8 z M10 19.2 a2 2 0 0 0 4 0"/></svg>
              <span class="visually-hidden">Notifications</span>
              <c:if test="${unread > 0}"><span class="bell__count">${unread}<span class="visually-hidden"> unread</span></span></c:if>
            </a>
            <a class="nav__me" href="${ctx}/profile" ${active == 'profile' ? 'aria-current="page"' : ''}><c:out value="${me.firstName}"/></a>
            <form method="post" action="${ctx}/logout" class="inline">
              <k:csrf/>
              <button type="submit" class="btn btn--quiet">Sign out</button>
            </form>
          </c:when>
          <c:otherwise>
            <a class="nav__signin" href="${ctx}/login" ${active == 'login' ? 'aria-current="page"' : ''}>Sign in</a>
            <a class="btn btn--dark" href="${ctx}/register">Create account</a>
          </c:otherwise>
        </c:choose>
      </div>
    </nav>
  </header>
  <div class="toasts wrap"><k:flash/></div>
  <main id="main" tabindex="-1">
    <jsp:doBody/>
  </main>
  <footer class="site-footer">
    <div class="wrap site-footer__inner">
      <p>Ketemu is a student project for Web Programming at ITS. It is not an official ITS service.</p>
      <p>Found something valuable? You can also hand it straight to the nearest SKK post.</p>
    </div>
  </footer>
</body>
</html>
