<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="My claims" active="claims">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <h1>My claims</h1>
        <p>Each claim is a ticket. Keep the stub number handy when you go to the SKK post.</p>
      </div>
    </div>
    <c:choose>
      <c:when test="${empty claims}">
        <div class="empty">
          <div class="empty__tag" aria-hidden="true"></div>
          <h2>No claims yet.</h2>
          <p>When you spot your item on the board, open it and choose Claim this item. Your ticket will appear here.</p>
          <div class="actions"><a class="btn btn--primary" href="${ctx}/?kind=found#board">Browse found items</a></div>
        </div>
      </c:when>
      <c:otherwise>
        <div class="ticket-list">
          <c:forEach var="cl" items="${claims}">
            <k:ticket claim="${cl}" compact="true">
              <p class="ticket__line muted">Filed ${cl.filed}<c:if test="${not empty cl.meetLocationName}">. Pickup at <c:out value="${cl.meetLocationName}"/></c:if>.</p>
            </k:ticket>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</k:layout>
