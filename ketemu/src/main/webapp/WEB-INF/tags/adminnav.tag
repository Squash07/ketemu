<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="active" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<nav class="subnav" aria-label="Desk">
  <a href="${ctx}/admin" ${active == 'ledger' ? 'aria-current="page"' : ''}>Ledger</a>
  <a href="${ctx}/admin/categories" ${active == 'categories' ? 'aria-current="page"' : ''}>Categories</a>
  <a href="${ctx}/admin/locations" ${active == 'locations' ? 'aria-current="page"' : ''}>Locations</a>
  <a href="${ctx}/admin/users" ${active == 'users' ? 'aria-current="page"' : ''}>Users</a>
</nav>
