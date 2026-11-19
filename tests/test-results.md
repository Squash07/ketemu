Run against http://localhost:8080 on 2026-11-19T02:34:48.887Z: 74 of 74 passed.

| No | Feature | Operation | Steps | Expected result | Actual result | Result |
|---|---|---|---|---|---|---|
| 1 | Accounts | Create (invalid) | Submit the register form empty | Inline errors under each required field, nothing saved | 5 inline errors, e.g. "Enter your NRP so the finder knows who is claiming." | Pass |
| 2 | Accounts | Create (invalid) | Register with two different passwords | Error under "Password again" | The two passwords are different. Type the same one twice. | Pass |
| 3 | Accounts | Create (invalid) | Register with an email that already exists (rani@ketemu.test) | Error: account already exists | There is already an account with this email. Sign in instead. | Pass |
| 4 | Accounts | Create | Register with valid NRP, name, email and password | Account created, signed in, toast "Account created" | Account created. You can post an item or file a claim now. | Pass |
| 5 | Accounts | Logout | Click Sign out | Session ends, toast "Signed out." | Signed out. | Pass |
| 6 | Accounts | Login (invalid) | Sign in with a wrong password | Error under the password field, still signed out | That email and password do not match. Check for typos, or create an account if you are new. | Pass |
| 7 | Accounts | Login + session fixation | Sign in with the right password and compare the session cookie before and after | Signed in, JSESSIONID changes after login | cookie changed: true | Pass |
| 8 | Accounts | Read | Open /profile | Profile shows name, NRP, email and the user's posts | heading "Test Student" | Pass |
| 9 | Accounts | Update (invalid) | Edit profile with phone "abc" | Inline error under Phone | Use digits only, for example 081234567890. | Pass |
| 10 | Accounts | Update | Edit profile: change name and phone | Toast "Profile saved.", new name shown | Profile saved. / Test Student Edited | Pass |
| 11 | Accounts | Update (invalid) | Change password with a wrong current password | Error under Current password | That is not your current password. Try again. | Pass |
| 12 | Accounts | Update | Change password with the right current password | Toast "Password changed." | Password changed. | Pass |
| 13 | Items | Create (invalid) | Submit the report form with a 2-letter title and no category or place | Inline errors, typed title kept | 3 errors, title kept "ab" | Pass |
| 14 | Items | Create | Post a lost item with title, category, place, date and description | Redirect to the new item page, toast "Posted. Your tag K-xxxx is on the board." | Posted. Your tag K-0412 is on the board. | Pass |
| 15 | Items | Read | Search the board for the new title | The new tag is the only match | 1 tag(s) found | Pass |
| 16 | Items | Read | Filter the board by Lost | Only tags with a Lost (or later) stamp | 6 tags, stamps: Lost, Returned | Pass |
| 17 | Items | Read (empty) | Search for a word nobody posted | Empty state with a "Post a lost item" action | Nothing matches ‘zzqqxx’. | Pass |
| 18 | Items | Update | Edit own report: change the title | Toast "Changes saved.", new title shown | Changes saved. | Pass |
| 19 | Items | Update (auth) | Open /items/{id}/edit of an item posted by someone else | 403 page "That is behind the desk." | HTTP 403 | Pass |
| 20 | Items | Delete (auth) | POST /items/407/delete (someone else's item) with a valid CSRF token | Refused with 403, item still exists | HTTP 403, item still there: true | Pass |
| 21 | Items | Update (status) | Close own report, then reopen it | Toasts "Report closed..." then "Report reopened..." | Report closed. It no longer shows on the board. / Report reopened. It is back on the board. | Pass |
| 22 | Photos | Create (invalid) | Upload a text file renamed to .png | Error "is not a JPG, PNG or WebP image." | notes.png is not a JPG, PNG or WebP image. | Pass |
| 23 | Photos | Create (invalid) | Upload a 1.2 MB image | Refused: "larger than 1 MB", nothing saved | One of the photos is larger than 1 MB, so nothing was saved. Resize it and try again. | Pass |
| 24 | Photos | Create | Upload 3 valid PNG photos on edit | Saved, gallery shows 3 thumbnails | 3 thumbnails | Pass |
| 25 | Photos | Create (invalid) | Try to add a 4th photo | Error "This item already has 3 photos" | This item already has 3 photos. Remove one before adding another. | Pass |
| 26 | Photos | Read | GET /photo/{id}, then repeat with If-None-Match | 200 with image type, ETag and Cache-Control, then 304 | 200 image/png, ETag "p1-1795080879", public, max-age=86400, revalidate 304 | Pass |
| 27 | Photos | Update | Make the second photo the cover | Toast "Cover photo changed." | Cover photo changed. | Pass |
| 28 | Photos | Update | Replace the first photo with another file | Toast "Photo replaced." | Photo replaced. | Pass |
| 29 | Photos | Delete | Remove one photo (confirm in the dialog) | Toast "Photo removed.", 2 photos left | Photo removed. 2 left | Pass |
| 30 | Claims | Create (invalid) | File a claim on K-0409 with proof "short" | Inline error asking for more detail | Add a little more detail so the finder can be sure it is yours. | Pass |
| 31 | Claims | Create | File a claim on K-0409 (found, open, not own) with proof and a pickup post | Ticket page, toast "Claim filed. Your ticket is K-0409-01." | Claim filed. Your ticket is K-0409-01. | Pass |
| 32 | Claims | Create (rule) | Bima POSTs a claim on his own item K-0403 | Refused with 403 "You posted this item" | HTTP 403 | Pass |
| 33 | Claims | Create (rule) | POST a claim on a lost item (K-0405) | Refused, error message, no claim created | HTTP 302 redirect back with message "Only found items can be claimed" | Pass |
| 34 | Claims | Read | Open My claims | Ticket stubs for Bima's claims | 2 tickets | Pass |
| 35 | Claims | Read (auth) | Ayu opens Bima's claim ticket | 403 "This claim ticket belongs to someone else." | HTTP 403 | Pass |
| 36 | Claims | Update (claimant) | Ayu edits the proof on her pending claim K-0403-01 | Toast "Claim updated." | Claim updated. | Pass |
| 37 | Claims | Create | Ayu files a second claim on K-0409 | Ticket K-0409-02 created | Claim filed. Your ticket is K-0409-02. | Pass |
| 38 | Claims | Update (finder rejects) | Bima (finder of K-0403) rejects Ayu's claim | Toast "Claim rejected.", Ayu notified | Claim rejected. | Pass |
| 39 | Claims | Update (finder approves) | Rani (finder of K-0409) approves Bima's claim | Item becomes Claimed, Ayu's pending claim is rejected automatically | claims: Approved, Not approved; tag: Claimed | Pass |
| 40 | Claims | Update (mark returned) | Rani clicks Mark returned | Item stamped Returned with the press animation, toast "Marked returned." | Marked returned. stamp: Returned, press animation: true | Pass |
| 41 | Claims | Delete (cancel) | Ayu files a claim on K-0408, then cancels it | Toast "Claim cancelled.", status Cancelled | Claim cancelled. | Pass |
| 42 | Comments | Create (invalid) | Post an empty comment | Inline error "Write something first" | Write something first, like where and when you saw it. | Pass |
| 43 | Comments | Create | Post a sighting on K-0402 | Toast "Comment posted.", comment at the end of the thread | Comment posted. | Pass |
| 44 | Comments | Update | Edit own comment | Toast "Comment saved.", text updated, marked (edited) | Comment saved. | Pass |
| 45 | Comments | Update (auth) | POST an edit to Bima's comment (id 4) as Ayu | Refused with 403 | HTTP 403 | Pass |
| 46 | Comments | Delete | Delete own comment (confirm in the dialog) | Toast "Comment deleted." | Comment deleted. | Pass |
| 47 | Watchlist | Create | Save K-0410 to the watchlist | Toast "Saved to watchlist..." | Saved to watchlist. We'll tell you when K-0410 changes. | Pass |
| 48 | Watchlist | Read | Open /watchlist | K-0410 is listed with its tag | 2 saved items, K-0410 present | Pass |
| 49 | Watchlist | Update (invalid) | Save a note longer than 140 characters (sent directly) | Refused: "Keep the note under 140 characters" | Keep the note under 140 characters. It was not saved. | Pass |
| 50 | Watchlist | Update | Add a private note to K-0410 | Toast "Note saved.", note shown | Note saved. | Pass |
| 51 | Watchlist | Delete | Remove K-0410 from the watchlist | Toast "Removed from watchlist." | Removed from watchlist. | Pass |
| 52 | Categories | Read (auth) | A student opens /admin/categories | 403 page | HTTP 403 | Pass |
| 53 | Categories | Read | Admin opens /admin/categories | Table with 8 seeded categories and item counts | 8 rows | Pass |
| 54 | Categories | Create (invalid) | Add a category named "Electronics" (already exists) | Inline error "There is already a category called Electronics." | There is already a category called Electronics. | Pass |
| 55 | Categories | Create | Add "Sports gear" with the bag icon | Toast "Category added." | Category added. | Pass |
| 56 | Categories | Update | Rename it and change the icon | Toast "Category saved." | Category saved. | Pass |
| 57 | Categories | Delete (rule) | Delete "Electronics", which items still use | Blocked: "3 items use Electronics. Move them to another category first." | 3 items use Electronics. Move them to another category first. | Pass |
| 58 | Categories | Delete | Delete the unused new category | Toast "Category deleted." | Category deleted. | Pass |
| 59 | Locations | Read | Admin opens /admin/locations | Table with 10 seeded locations, 3 SKK posts | 10 rows, 3 SKK posts | Pass |
| 60 | Locations | Create | Add "Gedung Pascasarjana" as an SKK post | Toast "Location added." | Location added. | Pass |
| 61 | Locations | Update | Edit it and switch off "This is an SKK post" | Toast "Location saved.", SKK post column shows No | Location saved. SKK post: No | Pass |
| 62 | Locations | Delete (rule) | Delete "Kantin Pusat", where items were reported | Blocked with a message to move the items first | 3 items were reported at Kantin Pusat. Move them to another location first. | Pass |
| 63 | Locations | Delete | Delete the unused new location | Toast "Location deleted." | Location deleted. | Pass |
| 64 | Notifications | Read | Bima opens /notifications after his claim was approved | Notification "Your claim K-0409-01 was approved..." with an unread dot | 4 notifications, newest: "Unread: K-0409 Grey umbrella, wooden handle was marked returned. Glad it found its way back." | Pass |
| 65 | Notifications | Update / Delete | Mark all as read, then Clear all | Toasts "Marked all as read." and "Notifications cleared." | Marked all as read. / Notifications cleared. | Pass |
| 66 | Admin users | Update | Admin changes Ayu to admin and back to student | Two toasts "Role changed..." | Role changed. Ayu Lestari is now a desk admin. / Role changed. Ayu Lestari is now a student. | Pass |
| 67 | Security | CSRF | POST /profile/edit without the _csrf token | Refused with 403 "This form expired" | HTTP 403 | Pass |
| 68 | Security | XSS | Post an item titled &lt;script&gt;alert(1)&lt;/script&gt; | Shown as text, no script element is created | title rendered as text: "&lt;script&gt;alert(1)&lt;/script&gt; xss test", alert fired: false | Pass |
| 69 | Security | SQL injection | Search the board for ' OR 1=1 -- | Treated as plain text: empty result, no error | HTTP 200, 0 tags | Pass |
| 70 | Security | Authentication | A guest opens /claims | Redirected to /login?next=/claims | /login?next=%2Fclaims | Pass |
| 71 | Security | Authorization | A student opens /admin | 403 page "That is behind the desk." | HTTP 403 | Pass |
| 72 | Items | Delete | Delete own report (confirm in the dialog) | Toast "Report deleted. K-xxxx is off the board." | Report deleted. K-0412 is off the board. | Pass |
| 73 | Accounts | Delete (invalid) | Delete account with a wrong password | Error "That is not your password. Your account was not deleted." | That is not your password. Your account was not deleted. | Pass |
| 74 | Accounts | Delete | Delete account with the right password | Account and its items removed, toast "Account deleted..." | Account deleted. Your items, claims and comments were removed with it. | Pass |
