<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="paging" required="true" type="id.ketemu.util.Paging" %>
<%@ attribute name="base" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${paging.pages > 1}">
  <c:set var="sep" value="${base.contains('?') ? '&' : '?'}"/>
  <nav class="pager" aria-label="Pages">
    <c:choose>
      <c:when test="${paging.hasPrev}"><a class="btn btn--outline" href="${base}${sep}page=${paging.prev}" rel="prev">Previous</a></c:when>
      <c:otherwise><span class="btn btn--outline is-disabled" aria-hidden="true">Previous</span></c:otherwise>
    </c:choose>
    <span class="pager__info">Page ${paging.page} of ${paging.pages}</span>
    <c:choose>
      <c:when test="${paging.hasNext}"><a class="btn btn--outline" href="${base}${sep}page=${paging.next}" rel="next">Next</a></c:when>
      <c:otherwise><span class="btn btn--outline is-disabled" aria-hidden="true">Next</span></c:otherwise>
    </c:choose>
  </nav>
</c:if>
