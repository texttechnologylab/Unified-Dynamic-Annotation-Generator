import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";
import Modal from "../../shared/classes/Modal.js";
import { randomId } from "../../shared/modules/utils.js";
import defaults from "./defaults.js";
import accordions from "../../shared/modules/accordions.js";

export default class Editor {
  constructor() {
    this.modal = new Modal(document.querySelector(".dv-modal").parentElement);
    this.templates = {
      newWidget: document.querySelector("#new-widget-template"),
      textWidget: document.querySelector("#text-placeholder"),
      imageWidget: document.querySelector("#image-placeholder"),
      chartWidget: document.querySelector("#d3-chart-placeholder"),
    };

    this.input = document.querySelector("#identifier-input");
    this.sources;
    this.derivedGenerators;
    this.grid;
  }

  init(config) {
    accordions.init();

    // Create accepted widgets
    const container = document.querySelector(".dv-widgets-container");
    defaults.forEach((widget) => {
      const element = this.createAcceptedWidget(widget.icon, widget.title);
      container.append(element);
    });

    // Initialize gridstack
    this.initGrid();

    // Load existing data
    this.sources = config.sources || [];
    this.derivedGenerators = config.derivedGenerators || [];

    if (config.widgets) {
      this.grid.load(config.widgets);
      this.grid.engine.nodes.forEach((item) => {
        if (!["Text", "Image"].includes(item.type)) {
          this.initChart(item.el, item);
        }

        // Show charts after gridstack animation is finished
        setTimeout(
          () => item.el.querySelector(".hide").classList.remove("hide"),
          300
        );
      });
    }

    // Initialize buttons
    document
      .querySelector("#cancel-button")
      .addEventListener("click", () => window.open("/", "_self"));

    document
      .querySelector("#save-button")
      .addEventListener("click", () => this.onSave());
  }

  initGrid() {
    this.grid = GridStack.init({
      minRow: 6,
      float: true,
      acceptWidgets: ".dv-widget-draggable",
    });

    GridStack.setupDragIn(
      ".dv-widget-draggable",
      { helper: "clone" },
      defaults
    );

    this.grid.on("added", (event, items) => {
      items.forEach((item) => {
        item.id = randomId(item.type);

        item.el.classList.remove("dv-widget-draggable");
        item.el.querySelector("i").replaceWith(this.createNewGridItem(item));
      });
    });
  }

  async onSave() {
    const pipelines = await fetch("/api/pipelines").then((response) =>
      response.json()
    );

    if (this.input.value.trim() === "") {
      this.modal.alert(
        "Missing Identifier",
        "Please provide an identifier for the pipeline."
      );
    } else if (pipelines.includes(this.input.value)) {
      this.modal.confirm(
        `Overwrite "${this.input.value}"`,
        "This pipeline already exists. Do you want to overwrite it?",
        () => this.sendConfig("PUT")
      );
    } else {
      this.sendConfig("POST");
    }
  }

  sendConfig(method) {
    const config = {
      id: this.input.value,
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

  createAcceptedWidget(icon, title) {
    const element = this.templates.newWidget.content.cloneNode(true);
    const i = element.querySelector("i");
    const span = element.querySelector("span");

    i.className = icon;
    span.textContent = title;
    span.title = title;

    return element;
  }

  createNewGridItem(item) {
    if (item.type === "Text") {
      return this.templates.textWidget.content.cloneNode(true);
    } else if (item.type === "Image") {
      return this.templates.imageWidget.content.cloneNode(true);
    } else {
      const element = this.templates.chartWidget.content.cloneNode(true);
      this.initChart(element, item);
      return element;
    }
  }

  initChart(element, item) {
    const span = element.querySelector("span");
    const buttons = element.querySelectorAll("button");
    const i = element.querySelector(".dv-chart-area").querySelector("i");

    i.className = item.icon;
    buttons[0].addEventListener("click", () =>
      this.modal.prompt(
        "Options",
        JSON.stringify(item.options),
        (value) => (item.options = JSON.parse(value))
      )
    );
    buttons[1].addEventListener("click", () => this.grid.removeWidget(item.el));
    span.textContent = item.title;
  }
}
