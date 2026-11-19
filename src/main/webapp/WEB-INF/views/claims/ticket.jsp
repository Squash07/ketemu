<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="post" value="${not empty claim.meetLocationName ? claim.meetLocationName : claim.heldAtName}"/>
<k:layout title="Claim ${claim.number}" active="claims">
  <div class="wrap">
    <p class="crumb"><a href="${ctx}/claims">All my claims</a></p>
    <div class="grid ticket-page">
      <div class="ticket-page__main">
        <k:ticket claim="${claim}">
          <dl class="ticket__facts">
            <dt>Item</dt><dd><a href="${ctx}/items/${claim.itemId}">${claim.itemCode}</a> <c:out value="${claim.itemTitle}"/></dd>
            <dt>Claimant</dt><dd><c:out value="${claim.claimantName}"/></dd>
            <dt>Finder</dt><dd><c:out value="${claim.finderName}"/></dd>
            <dt>Pickup post</dt><dd><c:out value="${empty post ? 'Agree on a post with the finder' : post}"/></dd>
            <dt>Filed</dt><dd class="num">${claim.filedDate}</dd>
            <c:if test="${not empty claim.decidedAt}"><dt>Decided</dt><dd>${claim.decided}</dd></c:if>
            <c:if test="${claim.approved}">
              <c:choose>
                <c:when test="${isClaimant}">
                  <dt>Contact finder</dt><dd><c:out value="${claim.finderEmail}"/><c:if test="${not empty claim.finderPhone}">, <span class="num"><c:out value="${claim.finderPhone}"/></span></c:if></dd>
                </c:when>
                <c:otherwise>
                  <dt>Contact claimant</dt><dd><c:out value="${claim.claimantEmail}"/><c:if test="${not empty claim.claimantPhone}">, <span class="num"><c:out value="${claim.claimantPhone}"/></span></c:if></dd>
                </c:otherwise>
              </c:choose>
            </c:if>
          </dl>
          <div class="ticket__proof">
            <p class="field__label">Proof of ownership</p>
            <blockquote class="pen-note pen-note--small"><c:out value="${claim.proof}"/></blockquote>
          </div>
          <div class="actions">
            <c:if test="${isClaimant and claim.pending}">
              <a class="btn btn--outline" href="${ctx}/claims/${claim.id}/edit">Edit proof</a>
            </c:if>
            <c:if test="${isFinder and claim.pending and item.open}">
              <form method="post" action="${ctx}/claims/${claim.id}/approve" class="inline"><k:csrf/><button class="btn btn--primary" type="submit">Approve claim</button></form>
              <form method="post" action="${ctx}/claims/${claim.id}/reject" class="inline" data-confirm="Reject this claim?" data-confirm-text="${fn:escapeXml(claim.claimantName)} will be told the claim was not approved.">
                <k:csrf/><button class="btn btn--danger" type="submit">Reject claim</button>
              </form>
            </c:if>
            <c:if test="${isFinder and claim.approved and item.status == 'claimed'}">
              <form method="post" action="${ctx}/items/${claim.itemId}/returned" class="inline"><k:csrf/><button class="btn btn--primary" type="submit">Mark returned</button></form>
            </c:if>
            <c:if test="${isClaimant and claim.active and item.status != 'returned'}">
              <form method="post" action="${ctx}/claims/${claim.id}/cancel" class="inline" data-confirm="Cancel claim ${claim.number}?" data-confirm-text="${claim.approved ? 'The item goes back on the board for others to claim.' : 'The finder will see that you withdrew it.'}">
                <k:csrf/><button class="btn btn--danger" type="submit">Cancel claim</button>
              </form>
            </c:if>
          </div>
        </k:ticket>
      </div>
      <aside class="ticket-page__aside" aria-label="What happens next">
        <h2>What happens next</h2>
        <c:choose>
          <c:when test="${claim.pending}">
            <p>The finder reads your proof and approves or rejects the claim. You get a notification either way. You can still edit the proof until then.</p>
          </c:when>
          <c:when test="${claim.approved and item.status == 'returned'}">
            <p>This item has been handed over and stamped returned. Nothing else to do.</p>
          </c:when>
          <c:when test="${claim.approved}">
            <p>Go to <strong><c:out value="${empty post ? 'the post you agreed on' : post}"/></strong> and show the stub number <strong class="num">${claim.number}</strong>. Bring your KTM. After the handover the finder marks the item returned.</p>
          </c:when>
          <c:when test="${claim.status == 'rejected'}">
            <p>The finder was not convinced, or matched the item to someone else. If you are sure it is yours, add a comment on the item with more detail.</p>
          </c:when>
          <c:otherwise>
            <p>You withdrew this claim. If the item is still open, you can file a new claim from its page.</p>
          </c:otherwise>
        </c:choose>
      </aside>
    </div>
  </div>
</k:layout>
