function init() {
  document.querySelectorAll("[data-toggle='sidepanel']").forEach((element) => {
    element.addEventListener("click", () => {
      const target = document.querySelector(element.dataset.target);
      target.classList.toggle("show");
    });
  });

  document.querySelectorAll("[data-dismiss='sidepanel']").forEach((element) => {
    element.addEventListener("click", () => {
      const target = element.closest(".dv-sidepanel");
      target.classList.remove("show");
    });
  });
}

export default { init };
