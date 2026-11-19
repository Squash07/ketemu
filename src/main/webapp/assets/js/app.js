/* Ketemu: small progressive enhancements. Every page works without this file. */
(function () {
  "use strict";

  document.documentElement.classList.add("js");
  document.querySelectorAll("[data-hide-with-js]").forEach(function (el) { el.hidden = true; });

  /* Toasts: dismiss button, and success messages leave on their own after a while. */
  document.querySelectorAll("[data-toast]").forEach(function (toast) {
    function leave() {
      toast.classList.add("is-leaving");
      setTimeout(function () { toast.remove(); }, 220);
    }
    var close = toast.querySelector("[data-toast-close]");
    if (close) close.addEventListener("click", leave);
    if (!toast.classList.contains("toast--error")) setTimeout(leave, 7000);
  });

  /* Confirm before destructive actions: <form data-confirm="Question" data-confirm-text="Details"> */
  var dialog = null;
  function confirmDialog(form) {
    if (!dialog) {
      dialog = document.createElement("dialog");
      dialog.className = "confirm";
      dialog.innerHTML =
        '<div class="confirm__body"><h2 class="confirm__title"></h2><p class="confirm__text"></p>' +
        '<div class="actions"><button type="button" class="btn btn--dark" data-yes></button>' +
        '<button type="button" class="btn btn--outline" data-no>Keep it</button></div></div>';
      document.body.appendChild(dialog);
      dialog.querySelector("[data-no]").addEventListener("click", function () { dialog.close(); });
    }
    dialog.querySelector(".confirm__title").textContent = form.getAttribute("data-confirm");
    dialog.querySelector(".confirm__text").textContent = form.getAttribute("data-confirm-text") || "This cannot be undone.";
    var yes = dialog.querySelector("[data-yes]");
    var submitter = form.querySelector("[type=submit]");
    yes.textContent = submitter ? submitter.textContent.trim() : "Confirm";
    yes.onclick = function () {
      dialog.close();
      form.dataset.confirmed = "yes";
      if (form.requestSubmit) form.requestSubmit(); else form.submit();
    };
    if (typeof dialog.showModal === "function") {
      dialog.showModal();
      dialog.querySelector("[data-no]").focus();
      return true;
    }
    return false;
  }
  document.addEventListener("submit", function (e) {
    var form = e.target;
    if (!form.matches("form[data-confirm]") || form.dataset.confirmed === "yes") return;
    e.preventDefault();
    if (!confirmDialog(form) && window.confirm(form.getAttribute("data-confirm"))) {
      form.dataset.confirmed = "yes";
      form.submit();
    }
  });

  /* Board filters submit as soon as a select changes. */
  document.querySelectorAll("form[data-autosubmit] select").forEach(function (sel) {
    sel.addEventListener("change", function () { sel.form.submit(); });
  });

  /* Search as you type on the hero tag: refresh the board without leaving the page. */
  var live = document.querySelector("form[data-live-search]");
  var results = document.getElementById("board-results");
  if (live && results && window.fetch && window.DOMParser) {
    var input = live.querySelector("input[name=q]");
    var timer = null;
    var last = input.value;
    function refresh(q) {
      var url = new URL(window.location.href);
      url.hash = "";
      url.searchParams.delete("page");
      if (q) url.searchParams.set("q", q); else url.searchParams.delete("q");
      fetch(url.toString(), { headers: { "X-Requested-With": "fetch" } })
        .then(function (r) { return r.text(); })
        .then(function (html) {
          var doc = new DOMParser().parseFromString(html, "text/html");
          var fresh = doc.getElementById("board-results");
          if (fresh) results.innerHTML = fresh.innerHTML;
          var seg = document.querySelector(".seg"), freshSeg = doc.querySelector(".seg");
          if (seg && freshSeg) seg.innerHTML = freshSeg.innerHTML;
          var filters = document.querySelector(".board-filters"), freshFilters = doc.querySelector(".board-filters");
          if (filters && freshFilters) {
            var hidden = filters.querySelector("input[name=q]");
            if (q && !hidden) { hidden = document.createElement("input"); hidden.type = "hidden"; hidden.name = "q"; filters.prepend(hidden); }
            if (hidden) { if (q) hidden.value = q; else hidden.remove(); }
          }
          history.replaceState(null, "", url.pathname + url.search);
        })
        .catch(function () { /* the normal form submit still works */ });
    }
    input.addEventListener("input", function () {
      clearTimeout(timer);
      timer = setTimeout(function () {
        var q = input.value.trim();
        if (q !== last) { last = q; refresh(q); }
      }, 250);
    });
    live.addEventListener("submit", function (e) {
      e.preventDefault();
      last = input.value.trim();
      refresh(last);
      document.getElementById("board").scrollIntoView({ behavior: "smooth" });
    });
    live.querySelectorAll("[data-chip]").forEach(function (chip) {
      chip.addEventListener("click", function (e) {
        e.preventDefault();
        input.value = chip.getAttribute("data-chip");
        last = input.value;
        refresh(last);
        input.focus();
      });
    });
  }

  /* Report form: the "held at" post only applies to found items. */
  var kindInputs = document.querySelectorAll("input[name=kind]");
  var heldAt = document.querySelector("[data-found-only]");
  if (kindInputs.length && heldAt) {
    function sync() {
      var checked = document.querySelector("input[name=kind]:checked");
      heldAt.hidden = !(checked && checked.value === "found");
    }
    kindInputs.forEach(function (k) { k.addEventListener("change", sync); });
    sync();
  }

  /* Photo preview before upload, with a size and type check. */
  document.querySelectorAll("input[type=file][data-preview]").forEach(function (input) {
    var box = document.getElementById(input.getAttribute("data-preview"));
    var max = parseInt(input.getAttribute("data-max") || "3", 10);
    var note = input.closest(".field") ? input.closest(".field").querySelector(".field__hint") : null;
    var original = note ? note.textContent : "";
    input.addEventListener("change", function () {
      if (!box) return;
      box.innerHTML = "";
      var files = Array.prototype.slice.call(input.files || []);
      var problem = "";
      if (files.length > max) problem = "Pick at most " + max + " photo" + (max === 1 ? "" : "s") + ".";
      files.slice(0, max).forEach(function (f) {
        if (!/^image\/(jpeg|png|webp)$/.test(f.type)) { problem = f.name + " is not a JPG, PNG or WebP image."; return; }
        if (f.size > 1048576) { problem = f.name + " is larger than 1 MB. Resize it and try again."; return; }
        var fig = document.createElement("figure");
        var img = document.createElement("img");
        img.alt = "";
        img.src = URL.createObjectURL(f);
        img.onload = function () { URL.revokeObjectURL(img.src); };
        var cap = document.createElement("figcaption");
        cap.textContent = f.name;
        fig.appendChild(img);
        fig.appendChild(cap);
        box.appendChild(fig);
      });
      if (note) {
        note.textContent = problem || original;
        note.classList.toggle("field__error", !!problem);
      }
    });
  });

  /* Inline edit toggles (comments, watchlist notes). */
  document.querySelectorAll("[data-toggle]").forEach(function (btn) {
    var target = document.getElementById(btn.getAttribute("data-toggle"));
    if (!target) return;
    btn.setAttribute("aria-controls", target.id);
    btn.setAttribute("aria-expanded", target.hidden ? "false" : "true");
    btn.addEventListener("click", function () {
      target.hidden = !target.hidden;
      btn.setAttribute("aria-expanded", target.hidden ? "false" : "true");
      if (!target.hidden) {
        var field = target.querySelector("textarea, input:not([type=hidden])");
        if (field) field.focus();
      }
    });
  });
})();
