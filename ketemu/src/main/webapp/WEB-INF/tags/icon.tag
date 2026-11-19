<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="px" value="${empty size ? 48 : size}"/>
<c:choose>
  <c:when test="${name == 'wallet'}"><c:set var="d" value="M10 20 h44 v30 h-44 z M10 28 h44 M40 34 h14 v10 h-14 z"/></c:when>
  <c:when test="${name == 'umbrella'}"><c:set var="d" value="M8 32 A24 24 0 0 1 56 32 Z M32 32 v18 a5 5 0 0 1 -10 0"/></c:when>
  <c:when test="${name == 'key'}"><c:set var="d" value="M11 32 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0 M29 32 h26 M47 32 v7 M53 32 v5"/></c:when>
  <c:when test="${name == 'tumbler'}"><c:set var="d" value="M22 12 h20 l-2 44 h-16 z M22 20 h20"/></c:when>
  <c:when test="${name == 'earbuds'}"><c:set var="d" value="M26 22 h12 a12 12 0 0 1 0 24 h-12 a12 12 0 0 1 0 -24 z M14 34 h36"/></c:when>
  <c:when test="${name == 'calc'}"><c:set var="d" value="M18 10 h28 v44 h-28 z M22 14 h20 v10 h-20 z M24 32 h2 M31 32 h2 M38 32 h2 M24 40 h2 M31 40 h2 M38 40 h2 M24 48 h2 M31 48 h2 M38 48 h2"/></c:when>
  <c:when test="${name == 'charger'}"><c:set var="d" value="M12 22 h24 v18 h-24 z M18 40 v6 M30 40 v6 M36 31 h8 a8 8 0 0 1 8 8 v15"/></c:when>
  <c:when test="${name == 'card'}"><c:set var="d" value="M10 18 h44 v28 h-44 z M17 30 a5 5 0 1 0 10 0 a5 5 0 1 0 -10 0 M32 26 h16 M32 32 h12 M17 40 h30"/></c:when>
  <c:when test="${name == 'bag'}"><c:set var="d" value="M17 24 h30 l3 30 h-36 z M25 24 v-5 a7 7 0 0 1 14 0 v5"/></c:when>
  <c:when test="${name == 'book'}"><c:set var="d" value="M32 18 c-6 -4 -12 -5 -20 -4 v34 c8 -1 14 0 20 4 c6 -4 12 -5 20 -4 v-34 c-8 -1 -14 0 -20 4 z M32 18 v34"/></c:when>
  <c:when test="${name == 'shirt'}"><c:set var="d" value="M24 12 l-12 8 l5 10 l5 -3 v25 h20 v-25 l5 3 l5 -10 l-12 -8 c-2 4 -14 4 -16 0 z"/></c:when>
  <c:otherwise><c:set var="d" value="M12 22 l20 -10 l20 10 v22 l-20 10 l-20 -10 z M12 22 l20 10 l20 -10 M32 32 v22"/></c:otherwise>
</c:choose>
<svg class="icon" width="${px}" height="${px}" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" <c:choose><c:when test="${empty label}">aria-hidden="true"</c:when><c:otherwise>role="img" aria-label="${label}"</c:otherwise></c:choose>><path d="${d}" vector-effect="non-scaling-stroke"/></svg>
