export default class CheckboxSearch {
  constructor(id) {
    this.id = id;
    this.templates = {
      result: document.querySelector("#result-template"),
      checkbox: document.querySelector("#checkbox-template"),
    };

    const root = document.getElementById(id);
    this.input = root.querySelector(".dv-autocomplete-input");
    this.results = root.querySelector(".dv-autocomplete-results");
    this.checkboxes = root.querySelector(".dv-filter-checkboxes");
    this.addedIds = [];

    let timeout = null;
    this.input.addEventListener("input", () => {
      clearTimeout(timeout);
      timeout = setTimeout(() => this.autocomplete(this.input.value), 300);
    });

    this.results.addEventListener("mousedown", (event) => {
      event.preventDefault();
    });

    this.input.addEventListener("focus", () => {
      this.autocomplete(this.input.value);
      this.results.classList.remove("dv-hidden");
    });

    this.input.addEventListener("blur", () => {
      this.results.classList.add("dv-hidden");
      this.results.innerHTML = "";
    });
  }

  get() {
    const nodes = this.checkboxes.querySelectorAll(".dv-check-input");
    const checked = Array.from(nodes).filter((cb) => cb.checked);
    const values = checked.map((cb) => cb.value);

    return values;
  }

  createFromTemplate(key) {
    return this.templates[key].content.cloneNode(true).firstElementChild;
  }

  autocomplete(value) {
    fetch(`/data-${this.id}.json`)
      .then((response) => response.json())
      .then((data) => {
        const filtered = data.filter(
          (d) => d.name.includes(value) && !this.addedIds.includes(d.id)
        );

        if (filtered.length > 0) {
          this.updateResults(filtered.slice(0, 5));
        } else {
          this.results.innerHTML = "No matches found";
        }
      });
  }

  updateResults(items) {
    this.results.innerHTML = "";

    for (const item of items) {
      const result = this.createFromTemplate("result");

      result.querySelector("span").textContent = item.name;
      result.addEventListener("click", () => this.addCheckbox(item));

      this.results.appendChild(result);
    }
  }

  addCheckbox(item) {
    this.input.blur();

    const checkbox = this.createFromTemplate("checkbox");

    checkbox.querySelector("input").value = item.id;
    checkbox.querySelector("span").textContent = item.name;
    checkbox.querySelector("button").addEventListener("click", () => {
      this.checkboxes.removeChild(checkbox);
      this.addedIds = this.addedIds.filter((id) => id !== item.id);
    });

    this.checkboxes.appendChild(checkbox);
    this.addedIds.push(item.id);
  }
}
