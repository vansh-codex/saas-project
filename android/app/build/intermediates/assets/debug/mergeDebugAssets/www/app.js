const repositoryScreen = document.querySelector("#repository-screen");
const pullsScreen = document.querySelector("#pulls-screen");
const popover = document.querySelector("#popover");
const pullDialog = document.querySelector("#pull-dialog");
const pullForm = document.querySelector("#pull-form");
const emptyState = document.querySelector("#empty-state");
const initialEmptyState = emptyState.innerHTML;
const pullRequests = [];
let toastTimer;

function showScreen(screen) {
  repositoryScreen.classList.toggle("is-active", screen === "repository");
  pullsScreen.classList.toggle("is-active", screen === "pulls");
  hidePopover();
}

function showToast(message) {
  let toast = document.querySelector(".toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.className = "toast";
    toast.setAttribute("role", "status");
    document.body.append(toast);
  }
  toast.textContent = message;
  toast.classList.add("visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove("visible"), 2200);
}

function hidePopover() {
  popover.hidden = true;
  popover.replaceChildren();
}

function openPopover(anchor, title, options, onSelect) {
  popover.replaceChildren();
  options.forEach((option) => {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = option;
    button.addEventListener("click", () => {
      onSelect(option);
      hidePopover();
    });
    popover.append(button);
  });
  const rect = anchor.getBoundingClientRect();
  popover.style.top = `${Math.min(rect.bottom + 8, window.innerHeight - 220)}px`;
  popover.style.left = `${Math.max(12, Math.min(rect.left, window.innerWidth - 220))}px`;
  popover.setAttribute("aria-label", title);
  popover.hidden = false;
}

document.querySelector('[data-view="pull-requests"]').addEventListener("click", () => showScreen("pulls"));
document.querySelector("#back-to-repo").addEventListener("click", () => showScreen("repository"));

document.querySelector("#star-button").addEventListener("click", (event) => {
  const button = event.currentTarget;
  const starred = button.classList.toggle("is-starred");
  button.setAttribute("aria-label", starred ? "Unstar repository" : "Star repository");
  document.querySelector("#star-count").textContent = starred ? "0" : "1";
});

document.querySelector("#watch-button").addEventListener("click", (event) => {
  const watching = event.currentTarget.classList.toggle("active");
  showToast(watching ? "You are now watching this repository" : "Repository notifications turned off");
});

document.querySelector("#add-to-list").addEventListener("click", (event) => {
  openPopover(event.currentTarget, "Add to list", ["Create a new list", "Favorites"], (item) => showToast(`${item} selected`));
});

document.querySelector("#repo-add").addEventListener("click", (event) => {
  openPopover(event.currentTarget, "Create new", ["New issue", "New pull request", "New repository"], (item) => {
    if (item === "New pull request") pullDialog.showModal();
    else showToast(`${item} is ready to create`);
  });
});

document.querySelector("#more-toggle").addEventListener("click", (event) => {
  openPopover(event.currentTarget, "More repository options", ["Projects", "Packages", "Wiki", "Security"], (item) => showToast(`${item} opened`));
});

document.querySelector("#change-branch").addEventListener("click", (event) => {
  openPopover(event.currentTarget, "Change branch", ["main", "develop", "feature/update"], (branch) => {
    document.querySelector(".branch-row code").textContent = branch;
  });
});

document.querySelectorAll(".filter-chip").forEach((chip) => {
  chip.addEventListener("click", () => {
    const filter = chip.dataset.filter;
    const values = {
      Status: ["Open", "Closed", "All"],
      Label: ["Any label", "bug", "enhancement"],
      Author: ["Anyone", "You"],
      Assignee: ["Anyone", "Assigned to you"],
      Milestone: ["Any milestone", "No milestone"],
    };
    openPopover(chip, `${filter} filter`, values[filter], (value) => {
      const label = filter === "Status" ? value : value === "Anyone" || value === "Any label" || value === "Any milestone" ? filter : value;
      chip.firstChild.textContent = `${label} `;
    });
  });
});

document.querySelector("#new-pull").addEventListener("click", () => pullDialog.showModal());

pullForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const formData = new FormData(pullForm);
  const title = String(formData.get("title") || "").trim();
  if (!title) return;
  pullRequests.unshift({
    title,
    branch: String(formData.get("branch") || "main"),
    description: String(formData.get("description") || "").trim(),
  });
  pullForm.reset();
  pullDialog.close();
  renderPullRequests();
  showToast("Pull request created");
});

pullForm.querySelector(".dialog-close").addEventListener("click", () => pullDialog.close());

function renderPullRequests(query = "") {
  const matching = pullRequests.filter((request) => request.title.toLowerCase().includes(query.toLowerCase()));
  if (!matching.length) {
    emptyState.innerHTML = initialEmptyState;
    emptyState.querySelector("h2").textContent = pullRequests.length && query
      ? "No pull requests match your search."
      : pullRequests.length
        ? "There are no more pull requests."
        : "There aren't any pull requests.";
    return;
  }

  emptyState.innerHTML = `<div class="pull-list">${matching.map((request) => `
    <article class="pull-card">
      <span class="pull-open-mark" aria-hidden="true">↗</span>
      <div><h2>${escapeHtml(request.title)}</h2><p>#${pullRequests.indexOf(request) + 1} opened just now · ${escapeHtml(request.branch)}</p></div>
    </article>`).join("")}</div>`;
}

function escapeHtml(value) {
  return value.replace(/[&<>"']/g, (character) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;",
  })[character]);
}

const searchBox = document.querySelector("#search-box");
document.querySelector("#search-toggle").addEventListener("click", () => {
  searchBox.hidden = !searchBox.hidden;
  if (!searchBox.hidden) document.querySelector("#pull-search").focus();
});
document.querySelector("#search-close").addEventListener("click", () => {
  searchBox.hidden = true;
  document.querySelector("#pull-search").value = "";
  renderPullRequests();
});
document.querySelector("#pull-search").addEventListener("input", (event) => renderPullRequests(event.currentTarget.value));

document.querySelectorAll(".menu-row[data-view]").forEach((row) => {
  if (row.dataset.view !== "pull-requests") {
    row.addEventListener("click", () => showToast(`${row.querySelector("span:nth-child(2)").textContent} opened`));
  }
});

document.querySelectorAll(".detail-row, .readme-row .text-action").forEach((button) => {
  button.addEventListener("click", () => showToast("This section is coming soon"));
});

document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => {
    document.querySelectorAll(".nav-item").forEach((navItem) => navItem.classList.remove("selected"));
    item.classList.add("selected");
    if (item.dataset.nav !== "Home") showToast(`${item.dataset.nav} is coming soon`);
    else showScreen("repository");
  });
});

document.addEventListener("click", (event) => {
  if (!popover.hidden && !popover.contains(event.target) && !event.target.closest("#repo-add, #add-to-list, #more-toggle, #change-branch, .filter-chip")) {
    hidePopover();
  }
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") hidePopover();
});
