<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Watchlist" active="watchlist">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <h1>Watchlist</h1>
        <p>Tags you are keeping an eye on. When one is claimed, returned, closed or gets a comment, you get a notification. Notes here are private to you.</p>
      </div>
    </div>
    <c:choose>
      <c:when test="${empty entries}">
        <div class="empty">
          <div class="empty__tag" aria-hidden="true"></div>
          <h2>Nothing saved yet.</h2>
          <p>Open any tag on the board and choose Save to watchlist. It is handy for a friend&rsquo;s lost item, or one that might be yours.</p>
          <div class="actions"><a class="btn btn--primary" href="${ctx}/#board">Browse the board</a></div>
        </div>
      </c:when>
      <c:otherwise>
        <ul class="watch">
          <c:forEach var="w" items="${entries}">
            <li class="watch__row" id="w-${w.itemId}">
              <div class="watch__tag tag-frame"><k:tag item="${w.item}"/></div>
              <div class="watch__body">
                <h2 class="watch__title"><a href="${ctx}/items/${w.itemId}"><c:out value="${w.item.title}"/></a></h2>
                <p class="muted">${w.item.code}, ${w.item.status == 'open' ? 'still open' : w.item.status}. Saved ${w.saved}.</p>
                <div class="watch__note">
                  <p class="field__label">Your note</p>
                  <c:choose>
                    <c:when test="${empty w.note}"><p class="muted">No note yet.</p></c:when>
                    <c:otherwise><p class="pen-note pen-note--small"><c:out value="${w.note}"/></p></c:otherwise>
                  </c:choose>
                </div>
                <div class="actions">
                  <button class="btn btn--outline" type="button" data-toggle="note-${w.itemId}">${empty w.note ? 'Add note' : 'Edit note'}</button>
                  <form method="post" action="${ctx}/watchlist/remove" class="inline" data-confirm="Remove ${w.item.code} from your watchlist?" data-confirm-text="You will stop getting notifications about it.">
                    <k:csrf/><input type="hidden" name="itemId" value="${w.itemId}">
                    <button class="btn btn--danger" type="submit">Remove</button>
                  </form>
                </div>
                <form class="form watch__noteform" id="note-${w.itemId}" method="post" action="${ctx}/watchlist/note" hidden>
                  <k:csrf/><input type="hidden" name="itemId" value="${w.itemId}">
                  <k:penfield name="note" id="note-field-${w.itemId}" label="Private note" value="${w.note}" maxlength="140" hint="Up to 140 characters. Leave it empty to clear the note."/>
                  <div class="actions"><button class="btn btn--dark" type="submit">Save note</button></div>
                </form>
              </div>
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>
  </div>
</k:layout>
