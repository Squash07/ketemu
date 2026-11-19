<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Desk ledger" active="admin">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <h1>Desk ledger</h1>
        <p>Where every tag stands today. ${openByKind.found} found item${openByKind.found == 1 ? ' is' : 's are'} waiting for an owner, ${openByKind.lost} lost report${openByKind.lost == 1 ? ' is' : 's are'} still open, and ${pendingClaims} claim${pendingClaims == 1 ? ' is' : 's are'} waiting for a finder&rsquo;s answer.</p>
      </div>
    </div>
    <k:adminnav active="ledger"/>

    <section aria-labelledby="status-title" class="ledger-block">
      <h2 id="status-title">Items by status</h2>
      <div class="table-box table-box--narrow">
        <table class="table table--compact">
          <caption class="visually-hidden">Items by status</caption>
          <thead><tr><th scope="col">Status</th><th scope="col">Meaning</th><th scope="col" class="num">Items</th></tr></thead>
          <tbody>
            <tr><td class="nowrap"><k:stamp status="lost" tilt="-3" size="small"/> <k:stamp status="found" tilt="2" size="small"/></td><td>Open, waiting on the board</td><td class="num">${byStatus.open}</td></tr>
            <tr><td><k:stamp status="claimed" tilt="-3" size="small"/></td><td>Claim approved, handover pending</td><td class="num">${byStatus.claimed}</td></tr>
            <tr><td><k:stamp status="returned" tilt="-3" size="small"/></td><td>Back with the owner</td><td class="num">${byStatus.returned}</td></tr>
            <tr><td><k:stamp status="closed" tilt="-3" size="small"/></td><td>Closed by the person who posted it</td><td class="num">${byStatus.closed}</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <section aria-labelledby="newest-title" class="ledger-block">
      <h2 id="newest-title">Newest reports</h2>
      <c:choose>
        <c:when test="${empty newest}"><p class="muted">No reports yet. They will appear here as students post them.</p></c:when>
        <c:otherwise>
          <div class="table-box">
            <table class="table table--wide">
              <caption class="visually-hidden">Newest reports</caption>
              <thead><tr><th scope="col">Code</th><th scope="col">Item</th><th scope="col">Kind</th><th scope="col">Stamp</th><th scope="col">Place</th><th scope="col">Posted by</th><th scope="col">Date</th></tr></thead>
              <tbody>
                <c:forEach var="it" items="${newest}">
                  <tr>
                    <td class="num"><a href="${ctx}/items/${it.id}">${it.code}</a></td>
                    <td><c:out value="${it.title}"/></td>
                    <td>${it.kind == 'lost' ? 'Lost' : 'Found'}</td>
                    <td class="nowrap"><k:stamp status="${it.stamp}" tilt="-2" size="small"/></td>
                    <td><c:out value="${it.locationName}"/></td>
                    <td><c:out value="${it.reporterName}"/></td>
                    <td class="num">${it.dateShort}</td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </section>
  </div>
</k:layout>
