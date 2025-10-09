import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";
import Modal from "../../shared/classes/Modal.js";
import { randomId } from "../../shared/modules/utils.js";
import defaults from "./defaults.js";
import accordions from "../../shared/modules/accordions.js";

const newWidget = document.querySelector("#new-widget-template");
const textPlaceholder = document.querySelector("#text-placeholder");
const imagePlaceholder = document.querySelector("#image-placeholder");
const d3ChartPlaceholder = document.querySelector("#d3-chart-placeholder");
const input = document.querySelector("#identifier-input");
const modal = new Modal(document.querySelector(".dv-modal").parentElement);

export default class Editor {
  constructor(config = {}) {
    accordions.init();

    this.sources = config.sources || [];
    this.derivedGenerators = config.derivedGenerators || [];
    this.grid = this.createGrid(config.widgets || []);
  }

  init() {
    const container = document.querySelector(".dv-widgets-container");

    // Create all widgets
    defaults.forEach((widget) => {
      const element = this.createNewWidget(widget.icon, widget.title);

      container.append(element);
    });

    // Create grid
    this.createGrid();

    document
      .querySelector("#save-button")
      .addEventListener("click", () => this.onSave());
  }

  async onSave() {
    const pipelines = await fetch("/api/pipelines").then((response) =>
      response.json()
    );

    if (input.value.trim() === "") {
      modal.alert(
        "Missing Identifier",
        "Please provide an identifier for the pipeline."
      );
    } else if (pipelines.includes(input.value)) {
      modal.confirm(
        `Overwrite "${input.value}"`,
        "This pipeline already exists. Do you want to overwrite it?",
        () => this.sendConfig("UPDATE")
      );
    } else {
      this.sendConfig("PUT");
    }
  }

  sendConfig(method) {
    const config = {
      id: input.value,
      sources: this.sources,
      derivedGenerators: this.derivedGenerators,
      widgets: this.grid.save(false),
    };

    const options = {
      method,
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(config),
    };

    fetch("/api/pipelines", options).then(() => window.open("/", "_self"));
  }

  createGrid(widgets) {
    const grid = GridStack.init({
      minRow: 6,
      float: true,
      acceptWidgets: ".dv-widget-draggable",
    });
    GridStack.setupDragIn(
      ".dv-widget-draggable",
      { helper: "clone" },
      defaults
    );

    if (widgets) {
      grid.load(widgets);
    }

    grid.on("added", (event, items) => {
      items.forEach((item) => {
        item.id = randomId(item.type);

        item.el.classList.remove("dv-widget-draggable");
        item.el.querySelector("i").replaceWith(this.createGridItem(grid, item));
      });
    });

    return grid;
  }

  createNewWidget(icon, title) {
    const element = newWidget.content.cloneNode(true);
    const i = element.querySelector("i");
    const span = element.querySelector("span");

    i.className = icon;
    span.textContent = title;
    span.title = title;

    return element;
  }

  createGridItem(grid, item) {
    if (item.type === "Text") {
      return textPlaceholder.content.cloneNode(true);
    } else if (item.type === "Image") {
      return imagePlaceholder.content.cloneNode(true);
    } else {
      const element = d3ChartPlaceholder.content.cloneNode(true);
      const span = element.querySelector("span");
      const buttons = element.querySelectorAll("button");

      buttons[0].addEventListener("click", () =>
        modal.prompt(
          "Options",
          JSON.stringify(item.options),
          (value) => (item.options = JSON.parse(value))
        )
      );
      buttons[1].addEventListener("click", () => grid.removeWidget(item.el));
      span.textContent = item.title;

      return element;
    }
  }
}
