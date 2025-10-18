import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";
import Modal from "../../shared/classes/Modal.js";
import { randomId } from "../../shared/modules/utils.js";
import accordions from "../../shared/modules/accordions.js";
import getter from "./getter.js";

const defaults = Object.values(getter).map((Handler) => Handler.defaults);

export default class Editor {
  constructor() {
    this.modal = new Modal(document.querySelector(".dv-modal").parentElement);
    this.templates = {
      newWidget: document.querySelector("#new-widget-template"),
      textWidget: document.querySelector("#static-text-template"),
      imageWidget: document.querySelector("#static-image-template"),
      chartWidget: document.querySelector("#default-chart-template"),
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
        const Handler = getter[item.type];
        new Handler(item.el, item).init(this.modal, this.grid);

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
      .addEventListener("click", () => this.validate());
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
        item.el.querySelector("i").replaceWith(this.createGridItemWidget(item));
      });
    });
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

  createGridItemWidget(item) {
    const element = this.cloneTemplate(item.type);
    const Handler = getter[item.type];

    new Handler(element, item).init(this.modal, this.grid);

    return element;
  }

  cloneTemplate(type) {
    if (type === "StaticText") {
      return this.templates.textWidget.content.cloneNode(true);
    } else if (type === "StaticImage") {
      return this.templates.imageWidget.content.cloneNode(true);
    } else {
      return this.templates.chartWidget.content.cloneNode(true);
    }
  }

  async validate() {
    const pipelines = await fetch("/api/pipelines").then((response) =>
      response.json()
    );
    const config = {
      id: this.input.value,
      sources: this.sources,
      derivedGenerators: this.derivedGenerators,
      widgets: this.grid.save(false),
    };
    const missing = config.widgets.filter(
      (widget) => widget?.generator?.id.trim() === ""
    );

    if (config.id.trim() === "") {
      this.modal.alert(
        "Missing Identifier",
        "Please provide an identifier for the pipeline."
      );
    } else if (missing.length > 0) {
      this.modal.alert(
        "Missing Generators",
        "The following widgets have no generator assigned: " +
          missing.map((w) => w.title).join(", ")
      );
    } else if (pipelines.includes(this.input.value)) {
      this.modal.confirm(
        `Overwrite "${this.input.value}"`,
        "This pipeline already exists. Do you want to overwrite it?",
        () => this.saveConfig("PUT", config)
      );
    } else {
      this.saveConfig("POST", config);
    }
  }

  saveConfig(method, config) {
    const options = {
      method,
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(config),
    };

    fetch("/api/pipelines", options).then(() => window.open("/", "_self"));
  }
}
