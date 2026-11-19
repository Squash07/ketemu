<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Notifications" active="notifications">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <h1>Notifications</h1>
        <p>Claims on your items, decisions on your claims, and changes to tags in your watchlist.</p>
      </div>
      <c:if test="${not empty notes}">
        <div class="actions">
          <c:if test="${unread > 0}">
            <form method="post" action="${ctx}/notifications/read-all" class="inline"><k:csrf/><button class="btn btn--outline" type="submit">Mark all as read</button></form>
          </c:if>
          <form method="post" action="${ctx}/notifications/clear" class="inline" data-confirm="Clear all notifications?" data-confirm-text="Every notification is deleted. Your items and claims stay as they are.">
            <k:csrf/><button class="btn btn--danger" type="submit">Clear all</button>
          </form>
        </div>
      </c:if>
    </div>
    <c:choose>
      <c:when test="${empty notes}">
        <div class="empty">
          <div class="empty__tag" aria-hidden="true"></div>
          <h2>All quiet.</h2>
          <p>When someone claims your item, decides on your claim or comments on a tag you saved, it shows up here.</p>
          <div class="actions"><a class="btn btn--primary" href="${ctx}/#board">Go to the board</a></div>
        </div>
      </c:when>
      <c:otherwise>
        <ol class="notes">
          <c:forEach var="n" items="${notes}">
            <li class="note${n.read ? '' : ' note--unread'}">
              <span class="note__dot" aria-hidden="true"></span>
              <div class="note__body">
                <p class="note__msg"><c:if test="${not n.read}"><span class="visually-hidden">Unread: </span></c:if><c:out value="${n.message}"/></p>
                <p class="note__when muted num">${n.when}</p>
              </div>
              <div class="actions note__actions">
                <c:if test="${not empty n.itemId}">
                  <form method="post" action="${ctx}/notifications/${n.id}/read" class="inline">
                    <k:csrf/><input type="hidden" name="go" value="/items/${n.itemId}">
                    <button class="btn btn--link" type="submit">Open ${n.itemCode}</button>
                  </form>
                </c:if>
                <c:if test="${not n.read}">
                  <form method="post" action="${ctx}/notifications/${n.id}/read" class="inline"><k:csrf/><button class="btn btn--link" type="submit">Mark read</button></form>
                </c:if>
                <form method="post" action="${ctx}/notifications/${n.id}/delete" class="inline"><k:csrf/><button class="btn btn--danger" type="submit">Delete</button></form>
              </div>
            </li>
          </c:forEach>
        </ol>
        <k:pager paging="${paging}" base="${ctx}/notifications"/>
      </c:otherwise>
    </c:choose>
  </div>
</k:layout>
