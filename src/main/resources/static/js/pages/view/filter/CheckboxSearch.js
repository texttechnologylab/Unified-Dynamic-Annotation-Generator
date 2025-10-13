export default class CheckboxSearch {
  constructor(id) {
    this.id = id;
    this.templates = {
      result: document.querySelector("#result-template"),
      checkbox: document.querySelector("#checkbox-template"),
    };

    const root = document.getElementById(id);
    this.input = root.querySelector(".dv-autocomplete-input");
    this.results = root.querySelector(".dv-dropdown");
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
      this.results.classList.add("show");
    });

    this.input.addEventListener("blur", () => {
      this.results.classList.remove("show");
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
    fetch(`/api/${this.id}/documents?q=${value}`)
      .then((response) => response.json())
      .then((data) => {
        const filtered = data.filter((d) => !this.addedIds.includes(d));

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

      result.querySelector("span").textContent = item;
      result.addEventListener("click", () => this.addCheckbox(item));

      this.results.appendChild(result);
    }
  }

  addCheckbox(item) {
    this.input.blur();

    const checkbox = this.createFromTemplate("checkbox");

    checkbox.querySelector("input").value = item;
    checkbox.querySelector("span").textContent = item;
    checkbox.querySelector("button").addEventListener("click", () => {
      this.checkboxes.removeChild(checkbox);
      this.addedIds = this.addedIds.filter((id) => id !== item);
    });

    this.checkboxes.appendChild(checkbox);
    this.addedIds.push(item);
  }
}
