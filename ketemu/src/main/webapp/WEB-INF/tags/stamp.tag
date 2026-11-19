<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="status" required="true" %>
<%@ attribute name="tilt" required="false" %>
<%@ attribute name="press" required="false" type="java.lang.Boolean" %>
<%@ attribute name="size" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="word" value="${status == 'lost' ? 'Lost' : status == 'found' ? 'Found' : status == 'claimed' ? 'Claimed' : status == 'returned' ? 'Returned' : status == 'closed' ? 'Closed' : status == 'pending' ? 'Pending' : status == 'approved' ? 'Approved' : status == 'rejected' ? 'Not approved' : status == 'cancelled' ? 'Cancelled' : status}"/>
<span class="stamp stamp--${status}${press ? ' stamp--press' : ''}${empty size ? '' : ' stamp--'.concat(size)}" style="--tilt:${empty tilt ? -6 : tilt}deg">${word}</span>
