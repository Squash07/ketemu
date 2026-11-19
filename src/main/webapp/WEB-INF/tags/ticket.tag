<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ attribute name="claim" required="true" type="id.ketemu.model.Claim" %>
<%@ attribute name="compact" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<article class="ticket${compact ? ' ticket--compact' : ''} ticket--${claim.status}">
  <div class="ticket__main">
    <p class="ticket__kicker">Claim ticket</p>
    <c:choose>
      <c:when test="${compact}"><h2 class="ticket__number"><a href="${pageContext.request.contextPath}/claims/${claim.id}">${claim.number}</a></h2></c:when>
      <c:otherwise><h1 class="ticket__number">${claim.number}</h1></c:otherwise>
    </c:choose>
    <p class="ticket__item"><c:out value="${claim.itemTitle}"/></p>
    <p class="ticket__status"><k:stamp status="${claim.status}" tilt="-3" size="small"/> <span>${claim.statusLine}</span></p>
    <jsp:doBody/>
  </div>
  <div class="ticket__stub" aria-label="Stub for claim ${claim.number}">
    <span class="ticket__keep">Keep</span>
    <span class="ticket__stubno">${claim.stub}</span>
    <span class="ticket__stubcode">${claim.itemCode}</span>
  </div>
</article>
