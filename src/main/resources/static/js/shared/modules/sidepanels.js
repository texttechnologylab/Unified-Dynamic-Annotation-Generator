function init() {
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
}

export default { init };
