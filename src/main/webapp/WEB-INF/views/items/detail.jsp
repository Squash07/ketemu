<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="here" value="${ctx}/items/${item.id}"/>
<c:if test="${not empty sessionScope.commentError}">
  <c:set var="commentError" value="${sessionScope.commentError}"/>
  <c:set var="commentBody" value="${sessionScope.commentDraft}"/>
  <c:remove var="commentError" scope="session"/>
  <c:remove var="commentDraft" scope="session"/>
</c:if>
<k:layout title="${item.code} ${item.title}">
  <div class="wrap">
    <p class="crumb"><a href="${ctx}/#board">Back to the board</a></p>

    <div class="grid item">
      <div class="item__tagcol">
        <div class="hang">
          <span class="hang__string" aria-hidden="true"></span>
          <div class="tag tag--big" style="--tilt:${item.tilt}deg">
            <span class="tag__eyelet" aria-hidden="true"></span>
            <div class="tag__well tag__well--big">
              <c:choose>
                <c:when test="${not empty currentPhoto}">
                  <img src="${ctx}/photo/${currentPhoto.id}" alt="Photo of ${fn:escapeXml(item.title)}" width="360" height="260">
                </c:when>
                <c:otherwise><k:icon name="${item.icon}" size="96" label="${item.categoryName}"/></c:otherwise>
              </c:choose>
            </div>
            <c:if test="${fn:length(photos) > 1}">
              <ul class="thumbs" aria-label="Photos">
                <c:forEach var="ph" items="${photos}" varStatus="st">
                  <li>
                    <a href="${here}?photo=${ph.id}" ${currentPhoto.id == ph.id ? 'aria-current="true"' : ''}>
                      <img src="${ctx}/photo/${ph.id}" alt="Photo ${st.count}" width="64" height="48" loading="lazy">
                    </a>
                  </li>
                </c:forEach>
              </ul>
            </c:if>
            <span class="tag__meta"><span>${item.code}</span><span>${item.dateShort}</span></span>
            <span class="tag__title tag__title--big"><c:out value="${item.title}"/></span>
            <span class="tag__stamp tag__stamp--big"><k:stamp status="${item.stamp}" tilt="${item.stampTilt}" press="${flashStamp}" size="large"/></span>
          </div>
        </div>
      </div>

      <div class="item__info">
        <h1 class="item__title"><c:out value="${item.title}"/></h1>
        <dl class="facts item__facts">
          <dt>${item.found ? 'Found' : 'Lost'}</dt><dd class="num">${item.dateLong}</dd>
          <dt>Where</dt><dd><c:out value="${item.place}"/></dd>
          <dt>Category</dt><dd><c:out value="${item.categoryName}"/></dd>
          <c:if test="${item.found}">
            <dt>Held at</dt><dd><c:out value="${empty item.heldAtName ? 'The finder still has it' : item.heldAtName}"/></dd>
          </c:if>
          <dt>Posted by</dt>
          <dd>
            <span class="mini-profile">
              <strong><c:out value="${item.reporterName}"/></strong>
              <span class="muted">${reporterStats[0]} item${reporterStats[0] == 1 ? '' : 's'} posted, ${reporterStats[1]} returned. Posted ${item.posted}.</span>
            </span>
          </dd>
        </dl>

        <c:if test="${not empty item.description}">
          <blockquote class="pen-note"><c:out value="${item.description}"/></blockquote>
        </c:if>

        <div class="actions item__actions">
          <c:choose>
            <c:when test="${isReporter}">
              <c:if test="${item.found and item.status == 'claimed'}">
                <form method="post" action="${here}/returned" class="inline"><k:csrf/><button class="btn btn--primary" type="submit">Mark returned</button></form>
              </c:if>
              <c:if test="${item.lost and item.open}">
                <form method="post" action="${here}/recovered" class="inline"><k:csrf/><button class="btn btn--primary" type="submit">I got it back</button></form>
              </c:if>
              <a class="btn btn--outline" href="${here}/edit">Edit report</a>
              <c:if test="${item.open}">
                <form method="post" action="${here}/close" class="inline" data-confirm="Close this report?" data-confirm-text="It comes off the board. You can reopen it later from this page.">
                  <k:csrf/><button class="btn btn--quiet" type="submit">Close report</button>
                </form>
              </c:if>
              <c:if test="${item.status == 'closed'}">
                <form method="post" action="${here}/reopen" class="inline"><k:csrf/><button class="btn btn--outline" type="submit">Reopen report</button></form>
              </c:if>
            </c:when>
            <c:otherwise>
              <c:if test="${item.found and item.open and empty claimBlock and empty myClaim}">
                <a class="btn btn--primary" href="#claim">Claim this item</a>
              </c:if>
              <c:choose>
                <c:when test="${empty me}">
                  <a class="btn btn--outline" href="${ctx}/login?next=${fn:escapeXml('/items/')}${item.id}">Save to watchlist</a>
                </c:when>
                <c:when test="${watching}">
                  <form method="post" action="${ctx}/watchlist/remove" class="inline">
                    <k:csrf/><input type="hidden" name="itemId" value="${item.id}"><input type="hidden" name="back" value="/items/${item.id}">
                    <button class="btn btn--outline" type="submit">Remove from watchlist</button>
                  </form>
                </c:when>
                <c:otherwise>
                  <form method="post" action="${ctx}/watchlist/add" class="inline">
                    <k:csrf/><input type="hidden" name="itemId" value="${item.id}">
                    <button class="btn btn--outline" type="submit">Save to watchlist</button>
                  </form>
                </c:otherwise>
              </c:choose>
            </c:otherwise>
          </c:choose>
          <c:if test="${isReporter or me.admin}">
            <form method="post" action="${here}/delete" class="inline" data-confirm="Delete ${item.code}?" data-confirm-text="The report, its photos, claims and comments are removed for good.">
              <k:csrf/><button class="btn btn--danger" type="submit">Delete report</button>
            </form>
          </c:if>
        </div>
      </div>
    </div>

    <div class="grid item__lower">
      <section class="item__claims" id="claim" aria-labelledby="claim-title">
        <c:choose>
          <c:when test="${item.found and isReporter}">
            <h2 id="claim-title">Claims on this item</h2>
            <c:choose>
              <c:when test="${empty incoming}">
                <p class="muted">No claims yet. When someone claims it, you will see their proof here and get a notification.</p>
              </c:when>
              <c:otherwise>
                <ol class="claim-list">
                  <c:forEach var="cl" items="${incoming}">
                    <li class="claim-row claim-row--${cl.status}">
                      <div class="claim-row__head">
                        <a class="claim-row__no num" href="${ctx}/claims/${cl.id}">${cl.number}</a>
                        <k:stamp status="${cl.status}" tilt="-3" size="small"/>
                      </div>
                      <p><strong><c:out value="${cl.claimantName}"/></strong> <span class="muted">filed ${cl.filed}<c:if test="${not empty cl.meetLocationName}">, wants to meet at <c:out value="${cl.meetLocationName}"/></c:if>.</span></p>
                      <blockquote class="pen-note pen-note--small"><c:out value="${cl.proof}"/></blockquote>
                      <c:if test="${cl.pending and item.open}">
                        <div class="actions">
                          <form method="post" action="${ctx}/claims/${cl.id}/approve" class="inline"><k:csrf/><button class="btn btn--primary" type="submit">Approve claim</button></form>
                          <form method="post" action="${ctx}/claims/${cl.id}/reject" class="inline" data-confirm="Reject this claim?" data-confirm-text="${fn:escapeXml(cl.claimantName)} will be told the claim was not approved.">
                            <k:csrf/><button class="btn btn--danger" type="submit">Reject claim</button>
                          </form>
                        </div>
                      </c:if>
                    </li>
                  </c:forEach>
                </ol>
              </c:otherwise>
            </c:choose>
          </c:when>
          <c:when test="${item.found}">
            <h2 id="claim-title">Your claim</h2>
            <c:choose>
              <c:when test="${empty me}">
                <p class="muted">Is this yours? Sign in to tell the finder something only the owner would know.</p>
                <p><a class="btn btn--primary" href="${ctx}/login?next=/items/${item.id}%23claim">Sign in to claim</a></p>
              </c:when>
              <c:when test="${not empty myClaim}">
                <k:ticket claim="${myClaim}" compact="true"/>
              </c:when>
              <c:when test="${not empty claimBlock}">
                <p class="muted"><c:out value="${claimBlock}"/></p>
              </c:when>
              <c:otherwise>
                <form class="form" method="post" action="${here}/claim" novalidate>
                  <k:csrf/>
                  <k:penfield name="proof" label="Tell the finder something only the owner would know" type="textarea" rows="3" value="${claimProof}" error="${claimErrors.proof}" maxlength="1000" hint="Only the finder sees this." required="true"/>
                  <div class="field">
                    <label class="field__label" for="f-meet">Where you&rsquo;d like to pick it up</label>
                    <c:set var="meetDefault" value="${empty claimMeet ? item.heldAtId : claimMeet}"/>
                    <select class="select" id="f-meet" name="meetLocationId">
                      <option value="">Decide with the finder later</option>
                      <c:forEach var="post" items="${posts}">
                        <option value="${post.id}" ${meetDefault == post.id ? 'selected' : ''}><c:out value="${post.name}"/></option>
                      </c:forEach>
                    </select>
                  </div>
                  <div class="actions"><button class="btn btn--primary" type="submit">File claim</button></div>
                </form>
              </c:otherwise>
            </c:choose>
          </c:when>
          <c:otherwise>
            <h2 id="claim-title">Seen it?</h2>
            <c:choose>
              <c:when test="${isReporter}">
                <p class="muted">When someone spots it they will comment here, and you get a notification. If you find it yourself, choose I got it back.</p>
              </c:when>
              <c:otherwise>
                <p class="muted">Leave a sighting in the comments so ${fn:escapeXml(item.reporterFirstName)} gets a notification. If you picked it up, hand it to an SKK post and post it as found.</p>
                <p><a class="btn btn--outline" href="${ctx}/items/new?kind=found">I found something</a></p>
              </c:otherwise>
            </c:choose>
          </c:otherwise>
        </c:choose>
      </section>

      <section class="item__comments" aria-labelledby="comments-title">
        <h2 id="comments-title">Sightings and comments</h2>
        <c:choose>
          <c:when test="${empty comments}">
            <p class="muted">No comments yet. Seen something like it? Say where and when.</p>
          </c:when>
          <c:otherwise>
            <ol class="comments">
              <c:forEach var="cm" items="${comments}">
                <li class="comment" id="comment-${cm.id}">
                  <p class="comment__meta"><strong>${cm.authorId == me.id ? 'You' : fn:escapeXml(cm.authorName)}</strong>, <span class="num">${cm.when}</span><c:if test="${cm.edited}"> (edited)</c:if></p>
                  <p class="comment__body"><c:out value="${cm.body}"/></p>
                  <c:if test="${cm.authorId == me.id or me.admin}">
                    <div class="actions comment__actions">
                      <c:if test="${cm.authorId == me.id}">
                        <button class="btn btn--link" type="button" data-toggle="edit-${cm.id}">Edit</button>
                      </c:if>
                      <form method="post" action="${ctx}/comments/${cm.id}/delete" class="inline" data-confirm="Delete this comment?" data-confirm-text="It disappears from the thread for everyone.">
                        <k:csrf/><button class="btn btn--danger" type="submit">Delete</button>
                      </form>
                    </div>
                    <c:if test="${cm.authorId == me.id}">
                      <form class="form comment__edit" id="edit-${cm.id}" method="post" action="${ctx}/comments/${cm.id}/edit" hidden>
                        <k:csrf/>
                        <k:penfield name="body" id="edit-body-${cm.id}" label="Edit your comment" type="textarea" rows="2" value="${cm.body}" maxlength="1000" required="true"/>
                        <div class="actions"><button class="btn btn--dark" type="submit">Save comment</button></div>
                      </form>
                    </c:if>
                  </c:if>
                </li>
              </c:forEach>
            </ol>
          </c:otherwise>
        </c:choose>
        <c:choose>
          <c:when test="${not empty me}">
            <form class="form comments__new" method="post" action="${ctx}/comments" novalidate>
              <k:csrf/>
              <input type="hidden" name="itemId" value="${item.id}">
              <k:penfield name="body" id="new-comment" label="Add a sighting or comment" type="textarea" rows="2" value="${commentBody}" error="${commentError}" maxlength="1000" placeholder="I think I saw this at..." required="true"/>
              <div class="actions"><button class="btn btn--dark" type="submit">Post comment</button></div>
            </form>
          </c:when>
          <c:otherwise>
            <p><a href="${ctx}/login?next=/items/${item.id}">Sign in</a> to add a sighting or comment.</p>
          </c:otherwise>
        </c:choose>
      </section>
    </div>
  </div>
</k:layout>
