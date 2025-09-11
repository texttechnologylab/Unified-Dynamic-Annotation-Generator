export default class CorpusFilter {
  constructor() {
    this.filter = {
      temp: {},
      applied: {},
    };

    const fileFilter = document.querySelector(".dv-file-filter");
    this.checkboxAll = fileFilter.querySelector(".dv-checkbox-all");
    this.checkboxes = fileFilter.querySelectorAll(".dv-checkbox");
    this.selectionInfo = fileFilter.querySelector(".dv-selection-info");

    const dateFilter = document.querySelector(".dv-date-filter");
    this.dateInputs = dateFilter.querySelectorAll(".dv-date-input");

    this.applyButton = document.querySelector("#btn-apply-filter");
  }

  init() {
    // Initialize file checkboxes
    this.checkboxAll.addEventListener("change", (event) => {
      this.checkboxes.forEach((input) => {
        input.checked = event.target.checked;
      });

      this.updateFileSelection();
    });
    this.checkboxes.forEach((input) => {
      input.addEventListener("change", () => this.updateFileSelection());
    });

    // Initialize date inputs
    this.dateInputs[0].addEventListener("change", (event) => {
      this.filter.temp.date.min = event.target.value;
    });
    this.dateInputs[1].addEventListener("change", (event) => {
      this.filter.temp.date.max = event.target.value;
    });

    // Initialize apply button
    this.applyButton.addEventListener("click", () => {
      this.filter.applied = this.filter.temp;
      console.log(this.filter);
    });

    // Initialize filter
    this.updateFileSelection();
    this.filter.temp.date = {};
  }

  updateFileSelection() {
    const checked = Array.from(this.checkboxes).filter((cb) => cb.checked);

    this.selectionInfo.textContent = `${checked.length} of ${this.checkboxes.length} selected`;
    this.checkboxAll.checked = checked.length === this.checkboxes.length;

    this.filter.temp.files = checked.map((cb) => cb.value);
  }
}

export const corpusFilter = new CorpusFilter();
