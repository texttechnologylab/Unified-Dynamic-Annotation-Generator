const fileFilter = document.querySelector(".dv-file-filter");

const checkboxAll = fileFilter.querySelector(".dv-checkbox-all");
const checkboxes = fileFilter.querySelectorAll(".dv-checkbox");
const selectionInfo = fileFilter.querySelector(".dv-selection-info");

function init() {
  checkboxAll.addEventListener("change", (event) => {
    checkboxes.forEach((input) => {
      input.checked = event.target.checked;
    });

    updateSelection();
  });

  checkboxes.forEach((input) => {
    input.addEventListener("change", updateSelection);
  });

  updateSelection();
}

function updateSelection() {
  const checked = Array.from(checkboxes).filter((cb) => cb.checked);

  selectionInfo.textContent = `${checked.length} of ${checkboxes.length} selected`;
  checkboxAll.checked = checked.length === checkboxes.length;

  const values = checked.map((cb) => cb.value);
  console.log(values);
}

export default { init };
