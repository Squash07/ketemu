<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="k" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<k:layout title="Campus lost and found" active="board">
  <section class="hero wrap" aria-labelledby="hero-title">
    <div class="hero__text">
      <h1 id="hero-title" class="hero__title">Lost it on campus? Someone probably handed it in.</h1>
      <p class="hero__lede">Ketemu is the shared lost-property board for ITS. Post what you lost or found, prove an item is yours, and pick it up at an SKK security post.</p>
      <div class="actions">
        <a class="btn btn--primary btn--big" href="${ctx}/items/new?kind=lost">I lost something</a>
        <a class="btn btn--outline btn--big" href="${ctx}/items/new?kind=found">I found something</a>
      </div>
      <p class="hero__note">
        <c:choose>
          <c:when test="${returnedThisMonth == 1}">1 item went back to its owner this month.</c:when>
          <c:when test="${returnedThisMonth > 1}">${returnedThisMonth} items went back to their owners this month.</c:when>
          <c:otherwise>Every returned item gets stamped here, so the board shows what is still waiting.</c:otherwise>
        </c:choose>
      </p>
    </div>
    <div class="hero__hang">
      <div class="hook" aria-hidden="true"></div>
      <div class="swing">
        <div class="swing__string" aria-hidden="true"></div>
        <form class="hero-tag" method="get" action="${ctx}/" role="search" data-live-search>
          <span class="hero-tag__eyelet" aria-hidden="true"></span>
          <label class="hero-tag__label" for="hero-q">What are you looking for?</label>
          <input class="pen hero-tag__input" id="hero-q" name="q" type="search" value="<c:out value='${filter.q}'/>" placeholder="Wallet, KTM, umbrella" autocomplete="off" maxlength="80">
          <div class="hero-tag__chips">
            <c:forEach var="chip" items="${['Wallet','KTM','Key','Charger']}">
              <a class="chip" href="${ctx}/?q=${chip}#board" data-chip="${chip}">${chip}</a>
            </c:forEach>
          </div>
          <span class="hero-tag__hint">Searches the board below as you type.</span>
          <button class="visually-hidden" type="submit">Search</button>
        </form>
      </div>
    </div>
  </section>

  <section id="board" class="board-section" aria-labelledby="board-title">
    <div class="wrap">
      <div class="board-bar">
        <h2 id="board-title">The board</h2>
        <nav class="seg" aria-label="Show">
          <a href="${ctx}/${segAll}#board" ${empty filter.kind ? 'aria-current="true"' : ''}>All <span class="num">${counts.all}</span></a>
          <a href="${ctx}/${segLost}#board" ${filter.kind == 'lost' ? 'aria-current="true"' : ''}>Lost <span class="num">${counts.lost}</span></a>
          <a href="${ctx}/${segFound}#board" ${filter.kind == 'found' ? 'aria-current="true"' : ''}>Found <span class="num">${counts.found}</span></a>
        </nav>
        <form class="board-filters" method="get" action="${ctx}/#board" data-autosubmit>
          <c:if test="${not empty filter.kind}"><input type="hidden" name="kind" value="${filter.kind}"></c:if>
          <c:if test="${not empty filter.q}"><input type="hidden" name="q" value="<c:out value='${filter.q}'/>"></c:if>
          <label class="inline-field"><span>Category</span>
            <select class="select" name="category">
              <option value="">All</option>
              <c:forEach var="cat" items="${categories}">
                <option value="${cat.id}" ${filter.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
              </c:forEach>
            </select>
          </label>
          <label class="inline-field"><span>Place</span>
            <select class="select" name="place">
              <option value="">Anywhere</option>
              <c:forEach var="loc" items="${locations}">
                <option value="${loc.id}" ${filter.locationId == loc.id ? 'selected' : ''}><c:out value="${loc.name}"/></option>
              </c:forEach>
            </select>
          </label>
          <button class="btn btn--outline" type="submit" data-hide-with-js>Apply</button>
        </form>
      </div>

      <div id="board-results" aria-live="polite">
        <c:if test="${filtered}">
          <p class="board-summary">
            <c:choose>
              <c:when test="${paging.total == 1}">1 tag</c:when>
              <c:otherwise>${paging.total} tags</c:otherwise>
            </c:choose>
            <c:if test="${not empty filter.q}"> match &lsquo;<c:out value="${filter.q}"/>&rsquo;</c:if>.
            <a href="${ctx}/#board">Clear filters</a>
          </p>
        </c:if>
        <c:choose>
          <c:when test="${empty items}">
            <div class="empty">
              <div class="empty__tag" aria-hidden="true"></div>
              <c:choose>
                <c:when test="${not empty filter.q}">
                  <h3>Nothing matches &lsquo;<c:out value="${filter.q}"/>&rsquo;.</h3>
                  <p>Post it as lost and we&rsquo;ll tell you when someone reports a match.</p>
                </c:when>
                <c:when test="${filtered}">
                  <h3>No tags with these filters.</h3>
                  <p>Try another category or place, or post what you are looking for.</p>
                </c:when>
                <c:otherwise>
                  <h3>The board is empty.</h3>
                  <p>Nothing has been handed in yet. Post the first item.</p>
                </c:otherwise>
              </c:choose>
              <div class="actions">
                <a class="btn btn--primary" href="${ctx}/items/new?kind=lost<c:if test='${not empty filter.q}'>&amp;title=<c:out value="${filter.q}"/></c:if>">Post a lost item</a>
                <c:if test="${filtered}"><a class="btn btn--outline" href="${ctx}/#board">Clear filters</a></c:if>
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <ul class="board">
              <c:forEach var="it" items="${items}">
                <li class="board__slot"><k:tag item="${it}"/></li>
              </c:forEach>
            </ul>
            <k:pager paging="${paging}" base="${ctx}/${pageBase}"/>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </section>

  <section class="how" aria-labelledby="how-title">
    <div class="wrap how__inner">
      <h2 id="how-title">How a handover works</h2>
      <ol class="how__steps">
        <li><span class="how__n">1</span><b>Post the item</b><span>Say what it is, where and when. The finder adds which SKK post is holding it.</span></li>
        <li><span class="how__n">2</span><b>File a claim</b><span>Describe something only the owner would know: a sticker, the lock screen, what&rsquo;s inside.</span></li>
        <li><span class="how__n">3</span><b>Meet at a post</b><span>When the finder approves, your claim ticket shows the post to meet at.</span></li>
        <li><span class="how__n">4</span><b>Stamp it returned</b><span>The finder marks it returned, and the tag gets its last stamp.</span></li>
      </ol>
    </div>
  </section>
</k:layout>
