<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Edit claim ${claim.number}" active="claims">
  <div class="wrap auth">
    <div class="auth__text">
      <p><a href="${ctx}/claims/${claim.id}">Back to ticket ${claim.number}</a></p>
      <h1>Edit your proof</h1>
      <p>You can change the proof and the pickup post until the finder decides. They see only the latest version.</p>
    </div>
    <form class="panel form auth__form" method="post" action="${ctx}/claims/${claim.id}/edit" novalidate>
      <k:csrf/>
      <k:penfield name="proof" label="Tell the finder something only the owner would know" type="textarea" rows="4" value="${proof}" error="${errors.proof}" maxlength="1000" required="true"/>
      <div class="field">
        <label class="field__label" for="f-meet">Where you&rsquo;d like to pick it up</label>
        <select class="select" id="f-meet" name="meetLocationId">
          <option value="">Decide with the finder later</option>
          <c:forEach var="post" items="${posts}">
            <option value="${post.id}" ${meet == post.id ? 'selected' : ''}><c:out value="${post.name}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="actions">
        <button class="btn btn--primary" type="submit">Save claim</button>
        <a class="btn btn--quiet" href="${ctx}/claims/${claim.id}">Cancel</a>
      </div>
    </form>
  </div>
</k:layout>
