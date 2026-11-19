<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Your profile" active="profile">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <h1><c:out value="${me.fullName}"/></h1>
        <p>Member since ${me.joined}. You posted ${stats[0]} item${stats[0] == 1 ? '' : 's'}, and ${stats[1]} of them ${stats[1] == 1 ? 'is' : 'are'} back with ${stats[1] == 1 ? 'its' : 'their'} owner.</p>
      </div>
      <div class="actions">
        <a class="btn btn--outline" href="${ctx}/profile/edit">Edit profile</a>
        <a class="btn btn--quiet" href="${ctx}/profile/password">Change password</a>
      </div>
    </div>

    <section class="section section--first">
      <dl class="facts">
        <dt>NRP</dt><dd class="num"><c:out value="${me.nrp}"/></dd>
        <dt>Email</dt><dd><c:out value="${me.email}"/></dd>
        <dt>Phone</dt><dd class="num"><c:out value="${empty me.phone ? 'Not added' : me.phone}"/></dd>
        <dt>Role</dt><dd>${me.admin ? 'SKK desk admin' : 'Student'}</dd>
      </dl>
    </section>

    <section class="section" aria-labelledby="posts-title">
      <div class="section-head">
        <h2 id="posts-title">Your posts</h2>
        <a class="btn btn--primary" href="${ctx}/items/new">Report an item</a>
      </div>
      <c:choose>
        <c:when test="${empty myItems}">
          <div class="empty">
            <div class="empty__tag" aria-hidden="true"></div>
            <h3>You have not posted anything yet.</h3>
            <p>Lost something, or picked something up? Post it and it goes on the board straight away.</p>
          </div>
        </c:when>
        <c:otherwise>
          <ul class="board">
            <c:forEach var="it" items="${myItems}">
              <li class="board__slot"><k:tag item="${it}"/></li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </section>

    <section class="section" aria-labelledby="claims-title">
      <div class="section-head">
        <h2 id="claims-title">Your claims</h2>
        <a class="btn btn--outline" href="${ctx}/claims">All claims</a>
      </div>
      <c:choose>
        <c:when test="${empty myClaims}">
          <p class="muted">No claims yet. When you spot your item on the board, open it and choose File claim.</p>
        </c:when>
        <c:otherwise>
          <div class="ticket-list">
            <c:forEach var="cl" items="${myClaims}" end="3">
              <k:ticket claim="${cl}" compact="true"/>
            </c:forEach>
          </div>
        </c:otherwise>
      </c:choose>
    </section>

    <section class="section" aria-labelledby="danger-title">
      <h2 id="danger-title">Delete account</h2>
      <p class="muted">Removes your account and everything you posted. You will be asked for your password.</p>
      <p><a class="btn btn--danger" href="${ctx}/profile/delete">Delete my account</a></p>
    </section>
  </div>
</k:layout>
