import {
  Dropdown,
  Collapse,
} from "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/+esm";

function init() {
  // Initialize sidepanels
  document.querySelectorAll("[data-dv-toggle='sidepanel']").forEach((node) => {
    node.addEventListener("click", () => {
      const target = document.querySelector(node.dataset.dvTarget);
      target.classList.toggle("show");
    });
  });

  document.querySelectorAll("[data-dv-dismiss='sidepanel']").forEach((node) => {
    node.addEventListener("click", () => {
      const target = node.closest(".dv-sidepanel");
      target.classList.remove("show");
    });
  });

  // Initialize dropdowns
  document.querySelectorAll("[data-bs-toggle='dropdown']").forEach((node) => {
    new Dropdown(node);
  });

  // Initialize accordions
  document.querySelectorAll("[data-dv-toggle='accordion']").forEach((node) => {
    const target = document.querySelector(node.dataset.dvTarget);
    const collapse = new Collapse(target, { toggle: false });
    const chevron = node.querySelector(".bi-chevron-down");

    node.addEventListener("click", () => {
      collapse.toggle();
    });
    target.addEventListener("show.bs.collapse", () => {
      chevron.classList.add("rotate");
    });
    target.addEventListener("hide.bs.collapse", () => {
      chevron.classList.remove("rotate");
    });
  });
}

export default { init };
