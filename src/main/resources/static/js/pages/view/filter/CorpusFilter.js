import CheckboxSearch from "./CheckboxSearch.js";
import DateRange from "./DateRange.js";

export default class CorpusFilter {
  constructor(selector) {
    this.root = document.querySelector(selector);

    this.filter = {};
    this.components = {};
  }

  init() {
    this.root.querySelectorAll("[data-dv-filter]").forEach((node) => {
      const id = node.id;
      const type = node.dataset.dvFilter;

      if (type === "checkbox") {
        this.components[id] = new CheckboxSearch(id);
        this.filter[id] = [];
      } else if (type === "date") {
        this.components[id] = new DateRange(id);
        this.filter[id] = {};
      }
    });
  }

  apply() {
    for (const [key, Component] of Object.entries(this.components)) {
      this.filter[key] = Component.get();
    }
  }
}

export const corpusFilter = new CorpusFilter(".dv-corpus-filter");
