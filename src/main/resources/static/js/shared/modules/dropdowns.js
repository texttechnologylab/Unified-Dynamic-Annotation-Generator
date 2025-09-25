import { Dropdown } from "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/+esm";

function init() {
  document.querySelectorAll("[data-bs-toggle='dropdown']").forEach((node) => {
    new Dropdown(node);
  });
}

export default { init };
