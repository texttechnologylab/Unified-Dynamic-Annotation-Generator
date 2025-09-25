import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";
import Modal from "../../shared/classes/Modal.js";
import { randomId } from "../../shared/modules/utils.js";

const newWidget = document.querySelector("#new-widget-template");
const textPlaceholder = document.querySelector("#text-placeholder");
const imagePlaceholder = document.querySelector("#image-placeholder");
const d3ChartPlaceholder = document.querySelector("#d3-chart-placeholder");

function init() {
  const modal = new Modal(document.querySelector(".dv-modal").parentElement);
  const container = document.querySelector(".dv-widgets-container");

  const widgets = [
    {
      type: "Text",
      title: "Text",
      options: {},
      icon: "bi bi-fonts",
      content: "Text",
    },
    {
      type: "Image",
      title: "Image",
      options: {},
      icon: "bi bi-image",
      content: "Image",
    },
    {
      type: "BarChart",
      title: "Bar Chart",
      generator: {},
      options: {},
      icon: "bi bi-bar-chart",
      content: "Bar Chart",
      minW: 3,
      minH: 2,
    },
    {
      type: "PieChart",
      title: "Pie Chart",
      generator: {},
      options: {},
      icon: "bi bi-pie-chart",
      content: "Pie Chart",
      minW: 2,
      minH: 2,
    },
    {
      type: "LineChart",
      title: "Line Chart",
      generator: {},
      options: {},
      icon: "bi bi-graph-up",
      content: "Line Chart",
      minW: 3,
      minH: 2,
    },
    {
      type: "HighlightText",
      title: "Highlight Text",
      generator: {},
      options: {},
      icon: "bi bi-card-text",
      content: "Highlight Text",
      minW: 2,
      minH: 2,
    },
    {
      type: "Network2D",
      title: "Network 2D",
      generator: {},
      options: {},
      icon: "bi bi-diagram-3",
      content: "Network 2D",
      minW: 3,
      minH: 2,
    },
    {
      type: "Map2D",
      title: "Map 2D",
      generator: {},
      options: {},
      icon: "bi bi-map",
      content: "Map 2D",
      minW: 3,
      minH: 2,
    },
  ];

  // Create all widgets
  widgets.forEach((widget) => {
    const element = createNewWidget(widget.icon, widget.title);

    container.append(element);
  });

  // Create grid
  const grid = GridStack.init({
    minRow: 6,
    float: true,
    acceptWidgets: ".dv-widget-draggable",
  });
  GridStack.setupDragIn(".dv-widget-draggable", { helper: "clone" }, widgets);

  // Open modal after widget was added to grid
  grid.on("added", (event, items) => {
    items.forEach((item) => {
      item.id = randomId(item.type);

      item.el
        .querySelector(".grid-stack-item-content")
        .replaceChildren(createGridItem(item));

      // modal.form(
      //   item.title + " Settings",
      //   [
      //     {
      //       label: "Title",
      //       type: "text",
      //       value: "My " + item.title,
      //     },
      //     {
      //       label: "Select a generator",
      //       type: "select",
      //       options: [],
      //       value: "",
      //     },
      //     {
      //       label: "Orientation",
      //       type: "select",
      //       options: ["horizontal", "vertical"],
      //       value: "vertical",
      //     },
      //   ],
      //   (title, generator, orientation) =>
      //     console.log(title, generator, orientation)
      // );
    });
    console.log(grid.save(false));
  });
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

function createGridItem(item) {
  if (item.type === "Text") {
    return textPlaceholder.content.cloneNode(true);
  } else if (item.type === "Image") {
    return imagePlaceholder.content.cloneNode(true);
  } else {
    const element = d3ChartPlaceholder.content.cloneNode(true);
    const span = element.querySelector("span");
    const i = element.querySelector(".dv-chart-area>i");

    span.textContent = item.title;
    // i.className = item.icon;

    return element;
  }
}

export default { init };
