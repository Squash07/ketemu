<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Campus locations" active="admin">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <h1>Campus locations</h1>
        <p>Places students can pick when they report an item. SKK posts also appear as pickup points for claims.</p>
      </div>
    </div>
    <k:adminnav active="locations"/>
    <div class="grid admin">
      <div class="admin__main">
        <div class="table-box">
          <table class="table">
            <caption class="visually-hidden">Campus locations</caption>
            <thead><tr><th scope="col">Name</th><th scope="col">Zone</th><th scope="col">SKK post</th><th scope="col" class="num">Items</th><th scope="col"><span class="visually-hidden">Actions</span></th></tr></thead>
            <tbody>
              <c:forEach var="loc" items="${locations}">
                <tr>
                  <td><strong><c:out value="${loc.name}"/></strong></td>
                  <td><c:out value="${empty loc.zone ? '' : loc.zone}"/></td>
                  <td>${loc.skkPost ? 'Yes, pickup point' : 'No'}</td>
                  <td class="num">${loc.itemCount}</td>
                  <td>
                    <div class="actions">
                      <a class="btn btn--link" href="${ctx}/admin/locations/${loc.id}/edit">Edit</a>
                      <form method="post" action="${ctx}/admin/locations/${loc.id}/delete" class="inline" data-confirm="Delete ${fn:escapeXml(loc.name)}?" data-confirm-text="${loc.itemCount > 0 ? 'Items were reported here, so the desk will refuse until they move.' : 'No items were reported here, so it can go.'}">
                        <k:csrf/><button class="btn btn--danger" type="submit">Delete</button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
        <c:if test="${empty locations}">
          <p class="muted admin__empty">No locations yet. Add the SKK posts first, then the buildings students use most.</p>
        </c:if>
      </div>
      <form class="panel form admin__side" method="post" action="${ctx}/admin/locations" novalidate>
        <k:csrf/>
        <h2>Add a location</h2>
        <k:penfield name="name" label="Name" value="${form.name}" error="${errors.name}" maxlength="80" required="true"/>
        <k:penfield name="zone" label="Zone (optional)" value="${form.zone}" error="${errors.zone}" maxlength="40" placeholder="East campus"/>
        <label class="check"><input type="checkbox" name="skkPost" value="1" ${form.skkPost ? 'checked' : ''}> This is an SKK post</label>
        <div class="actions"><button class="btn btn--primary" type="submit">Add location</button></div>
      </form>
    </div>
  </div>
</k:layout>
