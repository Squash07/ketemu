<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="isFound" value="${form.kind == 'found'}"/>
<k:layout title="${editing ? 'Edit '.concat(form.code) : 'Report an item'}" active="report">
  <div class="wrap">
    <div class="page-head">
      <div class="page-head__text">
        <c:if test="${editing}"><p><a href="${ctx}/items/${form.id}">Back to ${form.code}</a></p></c:if>
        <h1>
          <c:choose>
            <c:when test="${editing}">Edit ${form.code}</c:when>
            <c:otherwise>Report an item</c:otherwise>
          </c:choose>
        </h1>
        <p>
          <c:choose>
            <c:when test="${editing}">Update the details on the tag. Anyone watching this item gets a note that it changed.</c:when>
            <c:otherwise>Write it the way you would on a paper tag: what it is, where, and when. It takes under a minute.</c:otherwise>
          </c:choose>
        </p>
      </div>
    </div>

    <div class="grid item-form">
      <form class="panel form item-form__main" method="post" enctype="multipart/form-data"
            action="${ctx}/items/${editing ? form.id.toString().concat('/edit') : 'new'}" novalidate>
        <k:csrf/>
        <c:choose>
          <c:when test="${editing}">
            <p class="item-form__kind">This is a <strong>${form.kind}</strong> item report. <k:stamp status="${form.kind}" tilt="-4" size="small"/></p>
          </c:when>
          <c:otherwise>
            <fieldset class="field${not empty errors.kind ? ' field--error' : ''}">
              <legend class="field__label">What happened?</legend>
              <div class="choice">
                <input type="radio" id="kind-lost" name="kind" value="lost" ${form.kind == 'lost' ? 'checked' : ''}><label for="kind-lost">I lost it</label>
                <input type="radio" id="kind-found" name="kind" value="found" ${form.kind == 'found' ? 'checked' : ''}><label for="kind-found">I found it</label>
              </div>
              <c:if test="${not empty errors.kind}"><p class="field__error">${errors.kind}</p></c:if>
            </fieldset>
          </c:otherwise>
        </c:choose>

        <k:penfield name="title" label="What is it?" value="${form.title}" error="${errors.title}" maxlength="80" hint="A short name people will recognise, like Grey umbrella, wooden handle." required="true"/>

        <div class="form__row">
          <div class="field${not empty errors.categoryId ? ' field--error' : ''}">
            <label class="field__label" for="f-categoryId">Category</label>
            <select class="select" id="f-categoryId" name="categoryId" required>
              <option value="">Choose a category</option>
              <c:forEach var="cat" items="${categories}">
                <option value="${cat.id}" ${form.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
              </c:forEach>
            </select>
            <c:if test="${not empty errors.categoryId}"><p class="field__error">${errors.categoryId}</p></c:if>
          </div>
          <k:penfield name="eventDate" label="${isFound ? 'When did you find it?' : 'When did you lose it?'}" type="date" value="${form.dateIso}" error="${errors.eventDate}" required="true"/>
        </div>

        <div class="form__row">
          <div class="field${not empty errors.locationId ? ' field--error' : ''}">
            <label class="field__label" for="f-locationId">Where?</label>
            <select class="select" id="f-locationId" name="locationId" required>
              <option value="">Choose a place on campus</option>
              <c:forEach var="loc" items="${locations}">
                <option value="${loc.id}" ${form.locationId == loc.id ? 'selected' : ''}><c:out value="${loc.name}"/></option>
              </c:forEach>
            </select>
            <c:if test="${not empty errors.locationId}"><p class="field__error">${errors.locationId}</p></c:if>
          </div>
          <k:penfield name="locationDetail" label="Exact spot (optional)" value="${form.locationDetail}" error="${errors.locationDetail}" maxlength="120" placeholder="floor 2, near the stairs"/>
        </div>

        <div class="field${not empty errors.heldAtId ? ' field--error' : ''}" data-found-only ${isFound ? '' : 'hidden'}>
          <label class="field__label" for="f-heldAtId">Which SKK post is holding it?</label>
          <select class="select" id="f-heldAtId" name="heldAtId">
            <option value="">I still have it</option>
            <c:forEach var="post" items="${posts}">
              <option value="${post.id}" ${form.heldAtId == post.id ? 'selected' : ''}><c:out value="${post.name}"/></option>
            </c:forEach>
          </select>
          <c:choose>
            <c:when test="${not empty errors.heldAtId}"><p class="field__error">${errors.heldAtId}</p></c:when>
            <c:otherwise><p class="field__hint">Handing it to a post means the owner can pick it up even when you are busy.</p></c:otherwise>
          </c:choose>
        </div>

        <k:penfield name="description" label="${isFound ? 'Note for the owner' : 'Describe it'}" type="textarea" rows="4" value="${form.description}" error="${errors.description}" maxlength="2000" hint="Colour, brand, stickers, what was inside. For found items, keep one detail back so you can check a claim."/>

        <div class="field${not empty errors.photos ? ' field--error' : ''}">
          <label class="field__label" for="f-photos">Photos (optional)</label>
          <input class="file" id="f-photos" name="photos" type="file" accept="image/jpeg,image/png,image/webp" multiple data-preview="photo-preview" data-max="${editing ? 3 - fn:length(photos) : 3}">
          <c:choose>
            <c:when test="${not empty errors.photos}"><p class="field__error">${errors.photos}</p></c:when>
            <c:otherwise><p class="field__hint">Up to 3 photos, JPG, PNG or WebP, 1 MB each.<c:if test="${editing}"> This item has ${fn:length(photos)} now.</c:if></p></c:otherwise>
          </c:choose>
          <div class="preview" id="photo-preview" aria-live="polite"></div>
        </div>

        <div class="actions">
          <button class="btn btn--primary" type="submit">${editing ? 'Save changes' : 'Post item'}</button>
          <a class="btn btn--quiet" href="${editing ? ctx.concat('/items/').concat(form.id) : ctx.concat('/')}">Cancel</a>
        </div>
      </form>

      <aside class="item-form__aside" aria-label="Tips">
        <div class="note-tag">
          <span class="tag__eyelet" aria-hidden="true"></span>
          <p class="note-tag__title">A good tag says</p>
          <ul>
            <li>what it is, in plain words</li>
            <li>the building and the spot</li>
            <li>the day it happened</li>
          </ul>
          <p class="note-tag__small">Never post ID numbers, card numbers or phone lock codes. Ask the claimant for them instead.</p>
        </div>
      </aside>
    </div>

    <c:if test="${editing}">
      <section id="photos" class="section photo-admin" aria-labelledby="photos-title">
        <h2 id="photos-title">Photos on this tag</h2>
        <c:choose>
          <c:when test="${empty photos}">
            <p class="muted">No photos yet. Add up to 3 with the form above. The tag shows the category icon until then.</p>
          </c:when>
          <c:otherwise>
            <ul class="photo-admin__list">
              <c:forEach var="ph" items="${photos}" varStatus="st">
                <li class="photo-admin__item">
                  <img src="${ctx}/photo/${ph.id}" alt="Photo ${st.count} of ${form.title}" width="240" height="180" loading="lazy">
                  <p class="photo-admin__label">${st.first ? 'Cover photo' : 'Photo '.concat(st.count)}</p>
                  <div class="actions">
                    <c:if test="${not st.first}">
                      <form method="post" action="${ctx}/photos/${ph.id}/cover" class="inline"><k:csrf/><button class="btn btn--link" type="submit">Make cover</button></form>
                    </c:if>
                    <form method="post" action="${ctx}/photos/${ph.id}/delete" class="inline" data-confirm="Remove this photo?" data-confirm-text="It is deleted from the tag straight away.">
                      <k:csrf/><button class="btn btn--danger" type="submit">Remove photo</button>
                    </form>
                  </div>
                  <form method="post" action="${ctx}/photos/${ph.id}/replace" enctype="multipart/form-data" class="photo-admin__replace">
                    <k:csrf/>
                    <label class="field__label" for="rep-${ph.id}">Replace with</label>
                    <input class="file" id="rep-${ph.id}" name="photo" type="file" accept="image/jpeg,image/png,image/webp" required>
                    <button class="btn btn--outline" type="submit">Replace photo</button>
                  </form>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </section>
    </c:if>
  </div>
</k:layout>
