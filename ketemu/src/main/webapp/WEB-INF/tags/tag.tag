<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="item" required="true" type="id.ketemu.model.Item" %>
<%@ attribute name="press" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<a class="tag" href="${pageContext.request.contextPath}/items/${item.id}" style="--tilt:${item.tilt}deg">
  <span class="tag__eyelet" aria-hidden="true"></span>
  <span class="tag__well">
    <c:choose>
      <c:when test="${not empty item.coverPhotoId}"><img src="${pageContext.request.contextPath}/photo/${item.coverPhotoId}" alt="" loading="lazy" width="220" height="120"></c:when>
      <c:otherwise><k:icon name="${item.icon}"/></c:otherwise>
    </c:choose>
  </span>
  <span class="tag__meta"><span>${item.code}</span><span>${item.dateShort}</span></span>
  <span class="tag__title"><c:out value="${item.title}"/></span>
  <span class="tag__place"><c:out value="${item.place}"/></span>
  <span class="tag__stamp"><k:stamp status="${item.stamp}" tilt="${item.stampTilt}" press="${press}"/></span>
</a>
