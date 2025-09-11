export default class CorpusFilter {
  constructor() {
    this.filter = {};

    this.fileFilter = document.querySelector(".dv-file-filter");

    this.checkboxAll = this.fileFilter.querySelector(".dv-checkbox-all");
    this.checkboxes = this.fileFilter.querySelectorAll(".dv-checkbox");
    this.selectionInfo = this.fileFilter.querySelector(".dv-selection-info");
  }

  init() {
    this.checkboxAll.addEventListener("change", (event) => {
      this.checkboxes.forEach((input) => {
        input.checked = event.target.checked;
      });

      this.updateSelection();
    });

    this.checkboxes.forEach((input) => {
      input.addEventListener("change", this.updateSelection);
    });

    this.updateSelection();
  }

  updateSelection() {
    const checked = Array.from(this.checkboxes).filter((cb) => cb.checked);

    this.selectionInfo.textContent = `${checked.length} of ${this.checkboxes.length} selected`;
    this.checkboxAll.checked = checked.length === this.checkboxes.length;

    const values = checked.map((cb) => cb.value);
    console.log(values);
  }
}

export const corpusFilter = new CorpusFilter();
