<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Users" active="admin">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <h1>Users</h1>
        <p>Give SKK staff the admin role so they can manage categories and locations. Removing an account also removes its items, claims and comments.</p>
      </div>
      <form class="user-search" method="get" action="${ctx}/admin/users" role="search">
        <label class="field__label" for="uq">Find a user</label>
        <div class="user-search__row">
          <input class="pen" id="uq" name="q" type="search" value="<c:out value='${q}'/>" placeholder="Name, email or NRP" maxlength="80">
          <button class="btn btn--outline" type="submit">Search</button>
        </div>
      </form>
    </div>
    <k:adminnav active="users"/>
    <c:choose>
      <c:when test="${empty users}">
        <div class="empty">
          <div class="empty__tag" aria-hidden="true"></div>
          <h2>Nobody matches &lsquo;<c:out value="${q}"/>&rsquo;.</h2>
          <p>Check the spelling, or search by NRP instead.</p>
          <div class="actions"><a class="btn btn--outline" href="${ctx}/admin/users">Show everyone</a></div>
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-box">
          <table class="table">
            <caption class="visually-hidden">Users</caption>
            <thead><tr><th scope="col">Name</th><th scope="col">NRP</th><th scope="col">Email</th><th scope="col">Joined</th><th scope="col">Role</th><th scope="col"><span class="visually-hidden">Actions</span></th></tr></thead>
            <tbody>
              <c:forEach var="u" items="${users}">
                <tr>
                  <td><strong><c:out value="${u.fullName}"/></strong><c:if test="${u.id == me.id}"> <span class="muted">(you)</span></c:if></td>
                  <td class="num"><c:out value="${u.nrp}"/></td>
                  <td><c:out value="${u.email}"/></td>
                  <td class="num">${u.joined}</td>
                  <td>
                    <form method="post" action="${ctx}/admin/users/${u.id}/role" class="role-form">
                      <k:csrf/>
                      <label class="visually-hidden" for="role-${u.id}">Role for <c:out value="${u.fullName}"/></label>
                      <select class="select select--small" id="role-${u.id}" name="role" ${u.id == me.id ? 'disabled' : ''}>
                        <option value="student" ${u.role == 'student' ? 'selected' : ''}>Student</option>
                        <option value="admin" ${u.role == 'admin' ? 'selected' : ''}>Admin</option>
                      </select>
                      <c:if test="${u.id != me.id}"><button class="btn btn--link" type="submit">Change role</button></c:if>
                    </form>
                  </td>
                  <td>
                    <c:if test="${u.id != me.id}">
                      <form method="post" action="${ctx}/admin/users/${u.id}/delete" class="inline" data-confirm="Remove ${fn:escapeXml(u.fullName)}?" data-confirm-text="Their account, items, claims and comments are deleted for good.">
                        <k:csrf/><button class="btn btn--danger" type="submit">Remove</button>
                      </form>
                    </c:if>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
        <k:pager paging="${paging}" base="${ctx}/admin/users${pageBase}"/>
      </c:otherwise>
    </c:choose>
  </div>
</k:layout>
