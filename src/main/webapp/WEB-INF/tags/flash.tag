<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty sessionScope.flash}">
  <div class="toast toast--${sessionScope.flash.type}" role="${sessionScope.flash.type == 'error' ? 'alert' : 'status'}" data-toast>
    <p class="toast__text"><c:out value="${sessionScope.flash.message}"/></p>
    <button type="button" class="toast__close" data-toast-close aria-label="Dismiss message">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" aria-hidden="true"><path d="M6 6 L18 18 M18 6 L6 18"/></svg>
    </button>
  </div>
  <c:set var="flashStamp" scope="request" value="${sessionScope.flash.stamp}"/>
  <c:remove var="flash" scope="session"/>
</c:if>
