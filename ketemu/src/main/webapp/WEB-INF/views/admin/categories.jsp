<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Categories" active="admin">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <h1>Categories</h1>
        <p>Every item belongs to one category. The icon is drawn in the photo well of tags that have no photo.</p>
      </div>
    </div>
    <k:adminnav active="categories"/>
    <div class="grid admin">
      <div class="admin__main">
        <div class="table-box">
          <table class="table">
            <caption class="visually-hidden">Categories</caption>
            <thead><tr><th scope="col">Icon</th><th scope="col">Name</th><th scope="col" class="num">Items</th><th scope="col">Added</th><th scope="col"><span class="visually-hidden">Actions</span></th></tr></thead>
            <tbody>
              <c:forEach var="cat" items="${categories}">
                <tr>
                  <td class="icon-cell"><k:icon name="${cat.icon}" size="32"/></td>
                  <td><strong><c:out value="${cat.name}"/></strong></td>
                  <td class="num">${cat.itemCount}</td>
                  <td class="num muted">${cat.added}</td>
                  <td>
                    <div class="actions">
                      <a class="btn btn--link" href="${ctx}/admin/categories/${cat.id}/edit">Edit</a>
                      <form method="post" action="${ctx}/admin/categories/${cat.id}/delete" class="inline" data-confirm="Delete ${fn:escapeXml(cat.name)}?" data-confirm-text="${cat.itemCount > 0 ? 'Items still use it, so the desk will refuse until they move.' : 'No items use it, so it can go.'}">
                        <k:csrf/><button class="btn btn--danger" type="submit">Delete</button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
        <c:if test="${empty categories}">
          <p class="muted admin__empty">No categories yet. Add the first one with the form. Students cannot post items until there is at least one.</p>
        </c:if>
      </div>
      <form class="panel form admin__side" method="post" action="${ctx}/admin/categories" novalidate>
        <k:csrf/>
        <h2>Add a category</h2>
        <k:penfield name="name" label="Name" value="${form.name}" error="${errors.name}" maxlength="40" required="true"/>
        <fieldset class="field${not empty errors.icon ? ' field--error' : ''}">
          <legend class="field__label">Icon</legend>
          <div class="icon-pick">
            <c:forEach var="ic" items="${icons}">
              <input type="radio" name="icon" id="new-icon-${ic}" value="${ic}" ${form.icon == ic or (empty form.icon and ic == 'box') ? 'checked' : ''}>
              <label for="new-icon-${ic}" title="${ic}"><k:icon name="${ic}" size="32"/><span class="visually-hidden">${ic}</span></label>
            </c:forEach>
          </div>
          <c:if test="${not empty errors.icon}"><p class="field__error">${errors.icon}</p></c:if>
        </fieldset>
        <div class="actions"><button class="btn btn--primary" type="submit">Add category</button></div>
      </form>
    </div>
  </div>
</k:layout>
