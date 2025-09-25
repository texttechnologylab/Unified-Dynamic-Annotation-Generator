import { Collapse } from "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/+esm";

function init() {
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
