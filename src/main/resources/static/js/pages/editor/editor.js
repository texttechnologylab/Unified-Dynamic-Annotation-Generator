import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";
import Modal from "../../shared/classes/Modal.js";
import { randomId } from "../../shared/modules/utils.js";
import defaults from "./defaults.js";

const newWidget = document.querySelector("#new-widget-template");
const textPlaceholder = document.querySelector("#text-placeholder");
const imagePlaceholder = document.querySelector("#image-placeholder");
const d3ChartPlaceholder = document.querySelector("#d3-chart-placeholder");
const input = document.querySelector("#identifier-input");
const modal = new Modal(document.querySelector(".dv-modal").parentElement);
let sources = [];
let derivedGenerators = [];

function init(json) {
  const container = document.querySelector(".dv-widgets-container");

  // Create all widgets
  defaults.forEach((widget) => {
    const element = createNewWidget(widget.icon, widget.title);

    container.append(element);
  });

  // Create grid
  const grid = GridStack.init({
    minRow: 6,
    float: true,
    acceptWidgets: ".dv-widget-draggable",
  });
  GridStack.setupDragIn(".dv-widget-draggable", { helper: "clone" }, defaults);

  if (json.visualizations) {
    grid.load(json.visualizations);
  }

  grid.on("added", (event, items) => {
    items.forEach((item) => {
      item.id = randomId(item.type);

      item.el.classList.remove("dv-widget-draggable");
      item.el.querySelector("i").replaceWith(createGridItem(grid, item));
    });
  });

  document
    .querySelector("#sources-button")
    .addEventListener("click", () =>
      modal.prompt(
        "Sources",
        JSON.stringify(sources, null, 2),
        (value) => (sources = JSON.parse(value))
      )
    );
  document
    .querySelector("#generators-button")
    .addEventListener("click", () =>
      modal.prompt(
        "DerivedGenerators",
        JSON.stringify(derivedGenerators, null, 2),
        (value) => (derivedGenerators = JSON.parse(value))
      )
    );

  document.querySelector("#save-button").addEventListener("click", () => {
    const config = {
      id: input.value,
      sources,
      derivedGenerators,
      visualizations: grid.save(false),
    };

    console.log(config);
  });
}

function openModal() {
  modal.form(
    "Options",
    [
      {
        label: "Title",
        type: "text",
        value: "My ",
      },
      {
        label: "Select a generator",
        type: "select",
        options: [],
        value: "",
      },
      {
        label: "Orientation",
        type: "select",
        options: ["horizontal", "vertical"],
        value: "vertical",
      },
    ],
    (title, generator, orientation) =>
      console.log(title, generator, orientation)
  );
}

function createNewWidget(icon, title) {
  const element = newWidget.content.cloneNode(true);
  const i = element.querySelector("i");
  const span = element.querySelector("span");

  i.className = icon;
  span.textContent = title;
  span.title = title;

  return element;
}

function createGridItem(grid, item) {
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

export default { init };
